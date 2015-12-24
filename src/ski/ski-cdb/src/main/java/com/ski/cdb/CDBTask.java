package com.ski.cdb;

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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import fomjar.server.FjJsonMessage;
import fomjar.server.FjMessage;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;

public class CDBTask implements FjServerTask {
	
	private static final int CODE_SUCCESS                   = 0x00000000;
	private static final int CODE_DB_ABNORMAL               = 0x00001002;
	private static final int CODE_CMD_NOT_REGISTERED        = 0x00001003;
	private static final int CODE_CMD_MOD_INVALID           = 0x00001004;
	private static final int CODE_EXEC_CMD_FAILED        	= 0x00001005;
	private static final int CODE_EXEC_CMD_PARTLY_SUCCESS   = 0x00001006;
	private static final int CODE_ILLEGAL_MESSAGE           = 0xfffffffe;
	
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
		public String       cmd     = null;
		public String       mod     = null;
		public int          out     = 0;
		public String       sql_ori = null;
		public String       sql_use = null;
		public List<String> result  = null;
		public String       err     = null;
	}
	
	@Override
	public void onMsg(FjServer server, FjMessage msg) {
		if (!FjServerToolkit.isLegalRequest(msg)) {
			logger.error("illegal request: " + msg);
			if (FjServerToolkit.isLegalMsg(msg)) response(server, (FjJsonMessage) msg, CODE_ILLEGAL_MESSAGE, JSONObject.fromObject("{'error':'illegal request'}"));
			return;
		}
		FjJsonMessage req = (FjJsonMessage) msg;
		if (null == conn) {
			if (!initConn()) {
				logger.error("init databse connection failed, server works abnormally");
				response(server, req, CODE_DB_ABNORMAL, JSONObject.fromObject("{'error':'db state abnormal'}"));
				return;
			}
		}
		CdbCmdInfo cci = new CdbCmdInfo();
		cci.cmd = req.json().getString("cmd");
		if (!getCmdInfo(cci)) {
			response(server, req, CODE_CMD_NOT_REGISTERED, JSONObject.fromObject("{'error':'cmd is not registered: " + cci.err + "'}"));
			return;
		}
		Object arg_obj = req.json().get("arg");
		if (arg_obj instanceof JSONObject) {
			JSONObject arg = (JSONObject) arg_obj;
			generateSql(cci, arg);
			if (executeSql(cci)) response(server, req, CODE_SUCCESS, JSONObject.fromObject(String.format("{'array':%s}", JSONArray.fromObject(cci.result))));
			else response(server, req, CODE_EXEC_CMD_FAILED, JSONObject.fromObject(String.format("{'error':'cmd(%s) execute sql(%s) failed: %s'}", cci.cmd, cci.sql_use, cci.err)));
		} else if (arg_obj instanceof JSONArray) {
			boolean isSuccess = true;
			JSONArray args = (JSONArray) arg_obj;
			JSONArray result = new JSONArray();
			for (Object each_arg : args) {
				JSONObject arg = (JSONObject) each_arg;
				generateSql(cci, arg);
				if (!executeSql(cci)) {
					logger.error(String.format("cmd(%s) execute sql(%s) failed: %s", cci.cmd, cci.sql_use, cci.err));
					isSuccess = false;
				}
				result.add(cci.result);
			}
			response(server, req, isSuccess ? CODE_SUCCESS : CODE_EXEC_CMD_PARTLY_SUCCESS, JSONObject.fromObject(String.format("{'array':%s}", JSONArray.fromObject(result))));
		} else {
			logger.error("invalid arg object: " + arg_obj);
			response(server, req, CODE_CMD_MOD_INVALID, JSONObject.fromObject("{'error':'invalid arg object'}"));
		}
	}
	
	private static void response(FjServer server, FjJsonMessage req, int code, JSONObject desc) {
		FjJsonMessage rsp = new FjJsonMessage();
		rsp.json().put("fs", server.name());
		rsp.json().put("ts", req.json().getString("fs"));
		rsp.json().put("sid", req.json().getString("sid"));
		rsp.json().put("code", code);
		rsp.json().put("desc", desc);
		FjServerToolkit.getSender(server.name()).send(rsp);
	}
	
	private boolean getCmdInfo(CdbCmdInfo cci) {
		Statement st = null;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select c_mod, i_out, c_sql from tbl_cmd_map where c_cmd = '" + cci.cmd + "'");
			if (!rs.next()) {
				logger.error("cmd is not registered: " + cci.cmd);
				return false;
			}
			cci.mod = rs.getString(1);
			cci.out = rs.getInt(2);
			cci.sql_ori = rs.getString(3);
		} catch (SQLException e) {
			logger.error("failed to get cmd info: " + cci.cmd, e);
			cci.err = e.getMessage();
			return false;
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
		return true;
	}
	
	private static void generateSql(CdbCmdInfo cci, JSONObject arg) {
		logger.info(String.format("cmd(%s) sql-ori: %s", cci.cmd, cci.sql_ori));
		if (null == arg) return;
		
		cci.sql_use = cci.sql_ori;
		@SuppressWarnings("unchecked")
		Iterator<String> i = arg.keys();
		while (i.hasNext()) {
			String k = i.next();
			String v = arg.getString(k);
			cci.sql_use = cci.sql_use.replace("$" + k, v);
		}
		logger.info(String.format("cmd(%s) sql-use: %s", cci.cmd, cci.sql_use));
	}
	
	private boolean executeSql(CdbCmdInfo cci) {
		if (cci.mod.equalsIgnoreCase("st")) return executeSt(cci);
		if (cci.mod.equalsIgnoreCase("sp")) return executeSp(cci);
		logger.error("invalid command mode: " + cci.mod);
		return false;
	}
	
	private boolean executeSt(CdbCmdInfo cci) {
		if (null == cci.result) cci.result = new LinkedList<String>();
		else cci.result.clear();
		
		Statement st = null;
		try {
			st = conn.createStatement();
			if (0 < cci.out) {
				ResultSet rs = st.executeQuery(cci.sql_use);
				while (rs.next()) {
					for (int i = 1; i <= cci.out; i++)
						cci.result.add(rs.getString(i));
				}
			} else {
				st.execute(cci.sql_use);
			}
		} catch (SQLException e) {
			logger.error(String.format("failed to process statement, cmd: %s, mode: %s, sql: %s, out: %s", cci.cmd, cci.mod, cci.sql_use, cci.out), e);
			cci.err = e.getMessage();
			return false;
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
		return true;
	}
	
	private boolean executeSp(CdbCmdInfo cci) {
		if (null == cci.result) cci.result = new LinkedList<String>();
		else cci.result.clear();
		
		CallableStatement st = null;
		try {
			st = conn.prepareCall("call " + cci.sql_use + ";");
			if (0 < cci.out) {
				for (int i = 1; i <= cci.out; i++) st.registerOutParameter(i, Types.VARCHAR); 
			}
			st.execute();
			for (int i = 1; i <= cci.out; i++) cci.result.add(st.getString(i));
		} catch (SQLException e) {
			logger.error(String.format("failed to process store procedure, cmd: %s, mode: %s, sql: %s, out: %s", cci.cmd, cci.mod, cci.sql_use, cci.out), e);
			cci.err = e.getMessage();
			return false;
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
		return true;
	}
}
