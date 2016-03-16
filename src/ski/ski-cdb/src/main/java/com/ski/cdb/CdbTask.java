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
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjISIS;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CdbTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(CdbTask.class);
    private static Connection conn = null;

    private static final class InstInfo {
        public int                inst    = FjISIS.INST_SYS_UNKNOWN_INST;
        public JSON               args    = null;
        public String             mode    = null;
        public int                out     = 0;
        public String             sql_ori = null;
        public List<String>       sql_use = null;
        public List<List<String>> result  = null;
        public String             error   = null;
    }
    
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
        inst.args = req.argsToJson();
        {
            String inststring = Integer.toHexString(inst.inst);
            while (8 > inststring.length()) inststring = "0" + inststring;
            logger.info(String.format("INSTRUCTION - %s:%s:0x%s", req.fs(), req.sid(), inststring));
        }
        
        if (!checkConnection()) {
            response(server.name(), req, String.format("{'code':%d, 'desc':'database state abnormal'}", SkiCommon.CODE.CODE_DB_STATE_ABNORMAL));
            return;
        }
        
        inst.error = null;
        getInstInfo(conn, inst);
        if (null != inst.error) {
            logger.error("get instruction info failed: " + inst.error);
            response(server.name(), req, String.format("{'code':%d, 'desc':\"%s\"}", SkiCommon.CODE.CODE_SYS_ILLEGAL_INST, inst.error));
            return;
        }
        generateSql(inst);
        executeSql(conn, inst);
        if (null != inst.error) {
            logger.error("execute sql failed: " + inst.error);
            response(server.name(), req, String.format("{'code':%d, 'desc':\"%s\"}", SkiCommon.CODE.CODE_DB_OPERATE_FAILED, inst.error));
            return;
        }
        response(server.name(), req, String.format("{'code':%d, 'desc':%s}", SkiCommon.CODE.CODE_SYS_SUCCESS, JSONArray.fromObject(inst.result).toString()));
    }
    
    private static void response(String serverName, FjDscpMessage req, Object args) {
        FjDscpMessage rsp = new FjDscpMessage();
        rsp.json().put("fs",   serverName);
        rsp.json().put("ts",   req.fs());
        rsp.json().put("sid",  req.sid());
        rsp.json().put("inst", req.inst());
        rsp.json().put("args", args);
        FjServerToolkit.getSender(serverName).send(rsp);
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
                inst.error = "instruction is not registered: " + inst.inst;
                logger.error(inst.error);
                return;
            }
            inst.mode = rs.getString(1);
            inst.out = rs.getInt(2);
            inst.sql_ori = rs.getString(3);
        } catch (SQLException e) {
            logger.error("failed to get instruction info: " + inst.inst, e);
            inst.error = e.getMessage();
        } finally {
            try {if (null != st) st.close();}
            catch (SQLException e) {e.printStackTrace();}
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void generateSql(InstInfo inst) {
        logger.debug(String.format("inst(%d) sql-ori: %s", inst.inst, inst.sql_ori));
        if (!inst.args.isArray()) { // single argument: JSONObject
            JSONObject arg_obj = (JSONObject) inst.args;
            String sql_use = inst.sql_ori;
            Iterator<String> ki = arg_obj.keys();
            while (ki.hasNext()) {
                String k = ki.next();
                String v = arg_obj.getString(k);
                sql_use = sql_use.replace("$" + k, v);
            }
            sql_use = sql_use.replaceAll("[\\'|\\\"]*\\$\\w+[\\'|\\\"]*", "null");
            inst.sql_use = new LinkedList<String>();
            inst.sql_use.add(sql_use);
            logger.debug(String.format("inst(%d) sql-use: %s", inst.inst, sql_use));
        } else { // JSONArray
            JSONArray args_arr = (JSONArray) inst.args;
            inst.sql_use = (List<String>) args_arr
                    .stream()
                    .map((args)->{
                        JSONObject args_obj = (JSONObject) args;
                        String sql_use = inst.sql_ori;
                        Iterator<String> ki = args_obj.keys();
                        while (ki.hasNext()) {
                            String k = ki.next();
                            String v = args_obj.getString(k);
                            sql_use = sql_use.replace("$" + k, v);
                        }
                        sql_use = sql_use.replaceAll("\\$\\w+", "null");
                        logger.debug(String.format("inst(%d) sql-use: %s", inst.inst, sql_use));
                        return sql_use;
                    })
                    .collect(Collectors.toList());
        }
    }

    private static void executeSql(Connection conn, InstInfo inst) {
        switch (inst.mode.toLowerCase()) {
        case "sp": executeSp(conn, inst); break;
        case "st": executeSt(conn, inst); break;
        default: inst.error = "instruction execute mode is not supported: " + inst.mode; break;
        }
    }
    
    private static void executeSt(Connection conn, InstInfo inst) {
        if (null != inst.result) inst.result.clear();
        
        inst.sql_use.forEach((sql_use)->{
            Statement st = null;
            try {
                st = conn.createStatement();
                if (0 < inst.out) {
                    ResultSet rs = st.executeQuery(sql_use);
                    if (null == inst.result) inst.result = new LinkedList<List<String>>();
                    List<String> result1 = new LinkedList<String>();
                    while (rs.next())
                        for (int i = 1; i <= inst.out; i++) result1.add(rs.getString(i));
                    inst.result.add(result1);
                } else {
                    st.execute(sql_use);
                }
            } catch (SQLException e) {
                logger.error(String.format("failed to process statement, inst: %d, mode: %s, out: %d, sql: %s", inst.inst, inst.mode, inst.out, inst.sql_use), e);
                inst.error = e.getMessage();
            } finally {
                try {if (null != st) st.close();}
                catch (SQLException e) {e.printStackTrace();}
            }
        });
    }
    
    private static void executeSp(Connection conn, InstInfo inst) {
        if (null != inst.result) inst.result.clear();
        
        inst.sql_use.forEach((sql_use)->{
            CallableStatement st = null;
            try {
                st = conn.prepareCall("call " + sql_use + ";");
                if (0 < inst.out)
                    for (int i = 1; i <= inst.out; i++) st.registerOutParameter(i, Types.VARCHAR); 
                st.execute();
                if (0 < inst.out) {
                    if (null == inst.result) inst.result = new LinkedList<List<String>>();
                    List<String> result = new LinkedList<String>();
                    for (int i = 1; i <= inst.out; i++) {
                        byte[] data = st.getBytes(i);
                        if (null == data) result.add(null);
                        else result.add(new String(data, Charset.forName("utf-8")));
                    }
                    inst.result.add(result);
                }
            } catch (SQLException e) {
                logger.error(String.format("failed to process store procedure, inst: %d, mode: %s, out: %d, sql: %s", inst.inst, inst.mode, inst.out, inst.sql_use), e);
                inst.error = e.getMessage();
            } finally {
                try {if (null != st) st.close();}
                catch (SQLException e) {}
            }
        });
    }

}
