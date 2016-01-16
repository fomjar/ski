package com.ski.cdb;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.ski.common.DSCP;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjMessage;

public class CDBTask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(CDBTask.class);
	private static Connection conn = null;
	
	private static boolean initConn() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(FjServerToolkit.getServerConfig("db.url"));
			return true;
		} catch (ClassNotFoundException e) {
			logger.error("load database driver failed", e);
		} catch (SQLException e) {
			logger.error("open database connection failed", e);
		}
		return false;
	}
	
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
			logger.error("illegal message, discard: " + msg);
			return;
		}
		FjDscpMessage req = (FjDscpMessage) msg;
		if (null == conn) {
			if (!initConn()) {
				logger.error("init databse connection failed, server works abnormally");
				response(server.name(), req, DSCP.CMD.ERROR_DB_STATE_ABNORMAL, "{'error':'db state abnormal'}");
				return;
			}
		}
		CdbCmdInfo cci = new CdbCmdInfo();
		cci.cmd = req.cmd();
		cci.arg = (JSON) req.arg();
		cci.err = null;
		logger.info(String.format("COMMAND - 0x%s", Integer.toHexString(cci.cmd)));
		
		getCmdInfo(conn, cci);
		if (null != cci.err) {
			logger.error("failed to get command info: " + cci.err);
			response(server.name(), req, DSCP.CMD.ERROR_SYSTEM_ILLEGAL_COMMAND, "{'error':\"" + cci.err + "\"}");
			return;
		}
		generateSql(cci);
		executeSql(conn, cci);
		if (null == cci.err) response(server.name(), req, req.cmd(), String.format("{'result':%s}", JSONArray.fromObject(cci.result)));
		else {
			logger.error("execute sql failed: " + cci.err);
			response(server.name(), req, DSCP.CMD.ERROR_DB_OPERATE_FAILED, String.format("{'error':\"" + cci.err + "\"}"));
		}
	}
	
	private static void response(String serverName, FjDscpMessage req, int cmd, String arg) {
		FjDscpMessage rsp = new FjDscpMessage();
		rsp.json().put("fs",  serverName);
		rsp.json().put("ts",  req.fs());
		rsp.json().put("sid", req.sid());
		rsp.json().put("cmd", cmd);
		rsp.json().put("arg", arg);
		FjServerToolkit.getSender(serverName).send(rsp);
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
		BinaryOperator<Object> func_reduce = (result, entry)->{
			if (result instanceof Map.Entry<?, ?>) { // first iterator
				Object k0 = ((Map.Entry<Object, Object>) result).getKey();
				Object v0 = ((Map.Entry<Object, Object>) result).getValue();
				result = cci.sql_ori.replace("$" + k0.toString(), v0.toString());
			}
			Object k = ((Map.Entry<Object, Object>) entry).getKey();
			Object v = ((Map.Entry<Object, Object>) entry).getValue();
			result = result.toString().replace("$" + k.toString(), v.toString());
			return result;
		};
		
		if (!cci.arg.isArray()) { // single argument: JSONObject
			JSONObject arg_obj = (JSONObject) cci.arg;
			String sql_use = (String) arg_obj.entrySet()
					.stream()
					.reduce(func_reduce)
					.get();
			sql_use = sql_use.replaceAll("\\$\\w+", "null");
			cci.sql_use = new LinkedList<String>();
			cci.sql_use.add(sql_use);
			logger.debug(String.format("cmd(%d) sql-use: %s", cci.cmd, sql_use));
		} else { // JSONArray
			JSONArray arg_arr = (JSONArray) cci.arg;
			cci.sql_use = (List<String>) arg_arr
					.stream()
					.map((arg)->{
						JSONObject arg_obj = (JSONObject) arg;
						String sql_use = (String) arg_obj.entrySet()
								.stream()
								.reduce(func_reduce)
								.get();
						sql_use = sql_use.replaceAll("\\$\\w+", "null");
						logger.debug(String.format("cmd(%d) sql-use: %s", cci.cmd, sql_use));
						return sql_use;
					})
					.collect(Collectors.toList());
		}
	}

	private static void executeSql(Connection conn, CdbCmdInfo cci) {
		if (cci.mod.equalsIgnoreCase("st")) executeSt(conn, cci);
		if (cci.mod.equalsIgnoreCase("sp")) executeSp(conn, cci);
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
	            logger.error(String.format("failed to process statement, cmd: %s, mode: %s, sql: %s, out: %s", cci.cmd, cci.mod, cci.sql_use, cci.out), e);
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
                    for (int i = 1; i <= cci.out; i++) result1.add(st.getString(i));
                    cci.result.add(result1);
                }
            } catch (SQLException e) {
                logger.error(String.format("failed to process store procedure, cmd: %s, mode: %s, sql: %s, out: %s", cci.cmd, cci.mod, cci.sql_use, cci.out), e);
                cci.err = e.getMessage();
            } finally {
                try {if (null != st) st.close();}
                catch (SQLException e) {e.printStackTrace();}
            }
        });
	}
}
