package com.wtcrm.cdb;

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

import fomjar.server.FjJsonMsg;
import fomjar.server.FjMsg;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjToolkit;

public class CDBTask implements FjServerTask {
	
	private static final int CODE_SUCCESS                = 0x00000000;
	private static final int CODE_INCORRECT_ARGUMENT     = 0x00001001;
	private static final int CODE_DB_ABNORMAL            = 0x00001002;
	private static final int CODE_CMD_NOT_REGISTERED     = 0x00001003;
	private static final int CODE_CMD_MOD_INVALID        = 0x00001004;
	private static final int CODE_EXEC_CMD_FAILED        = 0x00001005;
	private static final int CODE_EXEC_CMD_PARTLY_FAILED = 0x00001005;
	
	private static final Logger logger = Logger.getLogger(CDBTask.class);
	private static Connection conn = null;
	
	private static boolean initConn() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(FjToolkit.getServerConfig("db.url"));
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
	}
	
	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (!(msg instanceof FjJsonMsg)
				|| !((FjJsonMsg) msg).json().containsKey("fs")
				|| !((FjJsonMsg) msg).json().containsKey("ts")
				|| !((FjJsonMsg) msg).json().containsKey("sid")) {
			logger.error("message not come from wtcrm server, discard: " + msg);
			return;
		}
		FjJsonMsg req = (FjJsonMsg) msg;
		if (!req.json().containsKey("cdb-cmd") || !req.json().containsKey("cdb-arg")) {
			logger.error("invalid cdb request, request does not contain \"cdb-cmd\": " + req);
			response(server, req, CODE_INCORRECT_ARGUMENT, JSONArray.fromObject("[\"invalid cdb request, must contain cdb-cmd and cdb-arg parameter\"]"));
			return;
		}
		if (null == conn) {
			if (!initConn()) {
				logger.error("init databse connection failed, server works abnormally");
				response(server, req, CODE_DB_ABNORMAL, JSONArray.fromObject("[\"db state abnormal\"]"));
				return;
			}
		}
		CdbCmdInfo cci = new CdbCmdInfo();
		cci.cmd = req.json().getString("cdb-cmd");
		if (!getCmdInfo(cci)) {
			response(server, req, CODE_CMD_NOT_REGISTERED, JSONArray.fromObject("[\"cdb-cmd is not registered\"]"));
			return;
		}
		Object arg_obj = req.json().get("cdb-arg");
		if (arg_obj instanceof JSONObject) {
			JSONObject arg = (JSONObject) arg_obj;
			generateSql(cci, arg);
			if (executeSql(cci)) response(server, req, CODE_SUCCESS, JSONArray.fromObject(cci.result));
			else response(server, req, CODE_EXEC_CMD_FAILED, JSONArray.fromObject("[\"cmd(" + cci.cmd + ") execute sql failed: " + cci.sql_use + "\"]"));
		} else if (arg_obj instanceof JSONArray) {
			boolean isSuccess = true;
			JSONArray args = (JSONArray) arg_obj;
			JSONArray result = new JSONArray();
			for (Object each_arg : args) {
				JSONObject arg = (JSONObject) each_arg;
				generateSql(cci, arg);
				if (!executeSql(cci)) {
					logger.error("cmd(" + cci.cmd + ") execute sql failed: " + cci.sql_use);
					isSuccess = false;
				}
				result.add(cci.result);
			}
			response(server, req, isSuccess ? CODE_SUCCESS : CODE_EXEC_CMD_PARTLY_FAILED, result);
		} else {
			logger.error("invalid cdb-arg object: " + arg_obj);
			response(server, req, CODE_CMD_MOD_INVALID, JSONArray.fromObject("[\"invalid cdb-arg object\"]"));
		}
	}
	
	private static void response(FjServer server, FjJsonMsg req, int cdb_code, JSONArray cdb_desc) {
		FjJsonMsg rsp = new FjJsonMsg();
		rsp.json().put("fs", server.name());
		rsp.json().put("ts", req.json().getString("fs"));
		rsp.json().put("sid", req.json().getString("sid"));
		rsp.json().put("cdb-code", cdb_code);
		rsp.json().put("cdb-desc", cdb_desc);
		FjToolkit.getSender(server.name()).send(rsp);
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
			return false;
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
		return true;
	}
	
	private static void generateSql(CdbCmdInfo cci, JSONObject arg) {
		logger.info("cmd(" + cci.cmd +  ") sql-ori: " + cci.sql_ori);
		if (null == arg) return;
		
		cci.sql_use = cci.sql_ori;
		@SuppressWarnings("unchecked")
		Iterator<String> i = arg.keys();
		while (i.hasNext()) {
			String k = i.next();
			String v = arg.getString(k);
			cci.sql_use = cci.sql_use.replace("$" + k, v);
		}
		logger.info("cmd(" + cci.cmd +  ") sql-use: " + cci.sql_use);
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
			return true;
		} catch (SQLException e) {
			logger.error("failed to process statement, cmd: " + cci.cmd + ", mode: " + cci.mod + ", sql: " + cci.sql_use + ", out: " + cci.out, e);
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
		return false;
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
			return true;
		} catch (SQLException e) {
			logger.error("failed to process store procedure, cmd: " + cci.cmd + ", mode: " + cci.mod+ ", sql: " + cci.sql_use + ", out: " + cci.out, e);
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
		return false;
	}
}
