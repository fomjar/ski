package com.ski.cdb;

import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjISIS;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CdbTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(CdbTask.class);
    private static Connection conn = null;

    private static final class InstInfo {
        public int          inst    = FjISIS.INST_SYS_UNKNOWN_INST;
        public JSONObject   args    = null;
        public String       mode    = null;
        public int          out     = 0;
        public String       sql_ori = null;
        public String       sql_use = null;
        public int          code    = CommonDefinition.CODE.CODE_SYS_SUCCESS;
        public String       desc    = null;
    }

    @Override
    public void initialize(FjServer server) {}

    @Override
    public void destroy(FjServer server) {}
    
    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }
        
        FjDscpMessage req = (FjDscpMessage) msg;
        InstInfo inst = new InstInfo();
        inst.inst = req.inst();
        inst.args = req.argsToJsonObject();
        logger.info(String.format("INSTRUCTION - %s:%s:0x%08X", req.fs(), req.sid(), req.inst()));
        
        if (!checkConnection()) {
            response(server.name(), req, inst);
            return;
        }
        
        getInstInfo(conn, inst);
        if (CommonDefinition.CODE.CODE_SYS_SUCCESS != inst.code) {
            logger.error("get instruction info failed: " + inst.desc);
            response(server.name(), req, inst);
            return;
        }
        generateSql(inst);
        executeSql(conn, inst);
        response(server.name(), req, inst);
    }
    
    private static void response(String server, FjDscpMessage req, InstInfo inst) {
        FjDscpMessage rsp = new FjDscpMessage();
        rsp.json().put("fs",   server);
        rsp.json().put("ts",   req.fs());
        rsp.json().put("sid",  req.sid());
        rsp.json().put("inst", req.inst());
        JSONObject args = new JSONObject();
        args.put("code", inst.code);
        args.put("desc", inst.desc);
        rsp.json().put("args", args);
        FjServerToolkit.getAnySender().send(rsp);
        logger.debug("response message: " + rsp);
    }
    
    private static boolean checkConnection() {
        if (null != conn) {
            try {if (conn.isValid(3)) return true;}
            catch (SQLException e) {logger.warn("check database connection failed", e);}
            try {conn.close();}
            catch (SQLException e) {}
        }
        try {
            Class.forName(FjServerToolkit.getServerConfig("db.driver"));
            conn = DriverManager.getConnection(FjServerToolkit.getServerConfig("db.url"));
        } catch (ClassNotFoundException e) {
            logger.error("can not find database driver", e);
            return false;
        } catch (SQLException e) {
            logger.error("open database connection failed", e);
            return false;
        }
        try {if (conn.isValid(3)) return true;}
        catch (SQLException e) {}
        logger.error("open database connection failed");
        return false;
    }

    
    private static void getInstInfo(Connection conn, InstInfo inst) {
        Statement st = null;
        try {
            st = conn.createStatement();
            ResultSet rs = st.executeQuery("select c_mode, i_out, c_sql from tbl_instruction where i_inst = " + inst.inst);
            if (!rs.next()) {
                inst.code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_INST;
                inst.desc = "instruction is not registered: " + inst.inst;
                logger.error(inst.desc);
                return;
            }
            inst.mode = rs.getString(1);
            inst.out = rs.getInt(2);
            inst.sql_ori = rs.getString(3);
            
            if ("sp".equalsIgnoreCase(inst.mode) && 2 > inst.out) {
                inst.code = CommonDefinition.CODE.CODE_DB_OPERATE_FAILED;
                inst.desc = "invalid store procedure, out parameter count must be larger than 1: " + inst.sql_ori;
                logger.error(inst.desc);
                return;
            }
        } catch (SQLException e) {
            inst.code = CommonDefinition.CODE.CODE_DB_OPERATE_FAILED;
            inst.desc = e.getMessage();
            logger.error("failed to get instruction info: " + inst.inst, e);
        } finally {
            try {if (null != st) st.close();}
            catch (SQLException e) {e.printStackTrace();}
        }
    }
    
    private static void generateSql(InstInfo inst) {
        logger.debug(String.format("inst(%d) sql-ori: %s", inst.inst, inst.sql_ori));
        String sql_use = inst.sql_ori;
        @SuppressWarnings("unchecked")
        Iterator<String> ki = inst.args.keys();
        while (ki.hasNext()) {
            String k = ki.next();
            String v = inst.args.getString(k);
            sql_use = sql_use.replace("$" + k, v);
        }
        sql_use = sql_use.replaceAll("[\\'|\\\"]*\\$\\w+[\\'|\\\"]*", "null");
        inst.sql_use = sql_use;
        logger.debug(String.format("inst(%d) sql-use: %s", inst.inst, sql_use));
    }

    private static void executeSql(Connection conn, InstInfo inst) {
        switch (inst.mode.toLowerCase()) {
        case "sp": executeSp(conn, inst); break;
        case "st": executeSt(conn, inst); break;
        default:
            inst.code = CommonDefinition.CODE.CODE_DB_OPERATE_FAILED;
            inst.desc = "instruction execute mode is not supported: " + inst.mode;
            break;
        }
    }
    
    private static void executeSt(Connection conn, InstInfo inst) {
        Statement st = null;
        try {
            st = conn.createStatement();
            if (0 < inst.out) {
                ResultSet rs = st.executeQuery(inst.sql_use);
                
                List<String> desc = new LinkedList<String>();
                while (rs.next())
                    for (int i = 1; i <= inst.out; i++) desc.add(rs.getString(i));
                inst.desc = JSONArray.fromObject(desc).toString();
            } else {
                st.execute(inst.sql_use);
            }
            inst.code = CommonDefinition.CODE.CODE_SYS_SUCCESS;
        } catch (SQLException e) {
            inst.code = CommonDefinition.CODE.CODE_DB_OPERATE_FAILED;
            inst.desc = e.getMessage();
            logger.error(String.format("failed to execute statement, inst: %d, mode: %s, out: %d, sql: %s", inst.inst, inst.mode, inst.out, inst.sql_use), e);
        } finally {
            try {if (null != st) st.close();}
            catch (SQLException e) {e.printStackTrace();}
        }
    }
    
    private static void executeSp(Connection conn, InstInfo inst) {
        CallableStatement st = null;
        try {
            st = conn.prepareCall("call " + inst.sql_use + ";");                
            st.registerOutParameter(1, Types.INTEGER);
            st.registerOutParameter(2, Types.VARCHAR);
            for (int i = 3; i <= inst.out; i++) st.registerOutParameter(i, Types.VARCHAR); 
            st.execute();
            
            inst.code = st.getInt(1);
            List<String> desc = new LinkedList<String>();
            for (int i = 2; i <= inst.out; i++) {
                byte[] data = st.getBytes(i);
                if (null == data) desc.add(null);
                else desc.add(new String(data, Charset.forName("utf-8")));
            }
            inst.desc = JSONArray.fromObject(desc).toString();
        } catch (SQLException e) {
            inst.code = CommonDefinition.CODE.CODE_DB_OPERATE_FAILED;
            inst.desc = e.getMessage();
            logger.error(String.format("failed to execute store procedure, inst: %d, mode: %s, out: %d, sql: %s", inst.inst, inst.mode, inst.out, inst.sql_use), e);
        } finally {
            try {if (null != st) st.close();}
            catch (SQLException e) {}
        }
    }

}
