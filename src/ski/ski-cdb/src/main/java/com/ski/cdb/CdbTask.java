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

import com.ski.common.DSCP;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjMessage;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CdbTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(CdbTask.class);
    private static Connection conn = null;

    private static final class CdbCmdInfo {
        public int                cmd     = DSCP.CMD.SYSTEM_UNKNOWN_COMMAND;
        public JSON               arg     = null;
        public String             mod     = null;
        public int                out     = 0;
        public String             sql_ori = null;
        public List<String>       sql_use = null;
        public List<List<String>> result  = null;
        public String             err     = null;
    }
    
    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }
        
        FjDscpMessage req = (FjDscpMessage) msg;
        CdbCmdInfo cci = new CdbCmdInfo();
        cci.cmd = req.cmd();
        cci.arg = req.argToJson();
        {
            String cmd = Integer.toHexString(cci.cmd);
            while (8 > cmd.length()) cmd = "0" + cmd;
            logger.info(String.format("COMMAND - %s:%s:0x%s", req.fs(), req.sid(), cmd));
        }
        
        if (!checkConnection()) {
            response(server.name(), req, String.format("{'code':%d, 'desc':'database state abnormal'}", DSCP.CODE.ERROR_DB_STATE_ABNORMAL));
            return;
        }
        
        cci.err = null;
        getCmdInfo(conn, cci);
        if (null != cci.err) {
            logger.error("get command info failed: " + cci.err);
            response(server.name(), req, String.format("{'code':%d, 'desc':\"%s\"}", DSCP.CODE.ERROR_SYSTEM_ILLEGAL_COMMAND, cci.err));
            return;
        }
        generateSql(cci);
        executeSql(conn, cci);
        if (null != cci.err) {
            logger.error("execute sql failed: " + cci.err);
            response(server.name(), req, String.format("{'code':%d, 'desc':\"%s\"}", DSCP.CODE.ERROR_DB_OPERATE_FAILED, cci.err));
            return;
        }
        response(server.name(), req, String.format("{'code':%d, 'desc':%s}", DSCP.CODE.ERROR_SYSTEM_SUCCESS, JSONArray.fromObject(cci.result).toString()));
    }
    
    private static void response(String serverName, FjDscpMessage req, Object arg) {
        FjDscpMessage rsp = new FjDscpMessage();
        rsp.json().put("fs",  serverName);
        rsp.json().put("ts",  req.fs());
        rsp.json().put("sid", req.sid());
        rsp.json().put("cmd", req.cmd());
        rsp.json().put("arg", arg);
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

    
    private static void getCmdInfo(Connection conn, CdbCmdInfo cci) {
        Statement st = null;
        try {
            st = conn.createStatement();
            ResultSet rs = st.executeQuery("select c_mod, i_out, c_sql from tbl_cmd_map where i_cmd = " + cci.cmd + "");
            if (!rs.next()) {
                cci.err = "command is not registered: " + cci.cmd;
                logger.error(cci.err);
                return;
            }
            cci.mod = rs.getString(1);
            cci.out = rs.getInt(2);
            cci.sql_ori = rs.getString(3);
        } catch (SQLException e) {
            logger.error("failed to get command info: " + cci.cmd, e);
            cci.err = e.getMessage();
        } finally {
            try {if (null != st) st.close();}
            catch (SQLException e) {e.printStackTrace();}
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void generateSql(CdbCmdInfo cci) {
        logger.debug(String.format("cmd(%d) sql-ori: %s", cci.cmd, cci.sql_ori));
        if (!cci.arg.isArray()) { // single argument: JSONObject
            JSONObject arg_obj = (JSONObject) cci.arg;
            String sql_use = cci.sql_ori;
            Iterator<String> ki = arg_obj.keys();
            while (ki.hasNext()) {
                String k = ki.next();
                String v = arg_obj.getString(k);
                sql_use = sql_use.replace("$" + k, v);
            }
            sql_use = sql_use.replaceAll("[\\'|\\\"]*\\$\\w+[\\'|\\\"]*", "null");
            cci.sql_use = new LinkedList<String>();
            cci.sql_use.add(sql_use);
            logger.debug(String.format("cmd(%d) sql-use: %s", cci.cmd, sql_use));
        } else { // JSONArray
            JSONArray arg_arr = (JSONArray) cci.arg;
            cci.sql_use = (List<String>) arg_arr
                    .stream()
                    .map((arg)->{
                        JSONObject arg_obj = (JSONObject) arg;
                        String sql_use = cci.sql_ori;
                        Iterator<String> ki = arg_obj.keys();
                        while (ki.hasNext()) {
                            String k = ki.next();
                            String v = arg_obj.getString(k);
                            sql_use = sql_use.replace("$" + k, v);
                        }
                        sql_use = sql_use.replaceAll("\\$\\w+", "null");
                        logger.debug(String.format("cmd(%d) sql-use: %s", cci.cmd, sql_use));
                        return sql_use;
                    })
                    .collect(Collectors.toList());
        }
    }

    private static void executeSql(Connection conn, CdbCmdInfo cci) {
        switch (cci.mod.toLowerCase()) {
        case "sp": executeSp(conn, cci); break;
        case "st": executeSt(conn, cci); break;
        default: cci.err = "command mod is not supported: " + cci.mod; break;
        }
    }
    
    private static void executeSt(Connection conn, CdbCmdInfo cci) {
        if (null != cci.result) cci.result.clear();
        
        cci.sql_use.forEach((sql_use)->{
            Statement st = null;
            try {
                st = conn.createStatement();
                if (0 < cci.out) {
                    ResultSet rs = st.executeQuery(sql_use);
                    if (null == cci.result) cci.result = new LinkedList<List<String>>();
                    List<String> result1 = new LinkedList<String>();
                    while (rs.next())
                        for (int i = 1; i <= cci.out; i++) result1.add(rs.getString(i));
                    cci.result.add(result1);
                } else {
                    st.execute(sql_use);
                }
            } catch (SQLException e) {
                logger.error(String.format("failed to process statement, cmd: %d, mod: %s, out: %d, sql: %s", cci.cmd, cci.mod, cci.out, cci.sql_use), e);
                cci.err = e.getMessage();
            } finally {
                try {if (null != st) st.close();}
                catch (SQLException e) {e.printStackTrace();}
            }
        });
    }
    
    private static void executeSp(Connection conn, CdbCmdInfo cci) {
        if (null != cci.result) cci.result.clear();
        
        cci.sql_use.forEach((sql_use)->{
            CallableStatement st = null;
            try {
                st = conn.prepareCall("call " + sql_use + ";");
                if (0 < cci.out)
                    for (int i = 1; i <= cci.out; i++) st.registerOutParameter(i, Types.VARCHAR); 
                st.execute();
                if (0 < cci.out) {
                    if (null == cci.result) cci.result = new LinkedList<List<String>>();
                    List<String> result1 = new LinkedList<String>();
                    for (int i = 1; i <= cci.out; i++) result1.add(new String(st.getBytes(i), Charset.forName("utf-8")));
                    cci.result.add(result1);
                }
            } catch (SQLException e) {
                logger.error(String.format("failed to process store procedure, cmd: %d, mod: %s, out: %d, sql: %s", cci.cmd, cci.mod, cci.out, cci.sql_use), e);
                cci.err = e.getMessage();
            } finally {
                try {if (null != st) st.close();}
                catch (SQLException e) {}
            }
        });
    }

}
