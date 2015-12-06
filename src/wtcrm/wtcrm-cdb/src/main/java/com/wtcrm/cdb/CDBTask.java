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
	
	private static final int CODE_SUCCESS            = 0x00000000;
	private static final int CODE_INCORRECT_ARGUMENT = 0x00001001;
	private static final int CODE_DB_ABNORMAL        = 0x00001002;
	private static final int CODE_CMD_NOT_REGISTERED = 0x00001003;
	private static final int CODE_CMD_MOD_INVALID    = 0x00001004;
	private static final int CODE_EXEC_CMD_FAILED    = 0x00001005;
	
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
	
	private String       cdb_cmd;
	private JSONObject   cdb_arg;
	private String       mod;
	private int          outcount;
	private String       sql;
	private List<String> outparam = new LinkedList<String>();

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (!(msg instanceof FjJsonMsg)
				|| !((FjJsonMsg) msg).json().containsKey("fs")
				|| !((FjJsonMsg) msg).json().containsKey("ts")) {
			logger.error("message not come from wtcrm server, discard: " + msg);
			return;
		}
		FjJsonMsg req = (FjJsonMsg) msg;
		if (!req.json().containsKey("cdb-cmd") || !req.json().containsKey("cdb-arg")) {
			logger.error("invalid cdb request, request does not contain \"cdb-cmd\": " + req);
			response(server, req, CODE_INCORRECT_ARGUMENT, JSONArray.fromObject("[\"invalid cdb request\"]"));
			return;
		}
		if (null == conn) {
			if (!initConn()) {
				logger.error("init databse connection failed, server works abnormally");
				response(server, req, CODE_DB_ABNORMAL, JSONArray.fromObject("[\"db state abnormal\"]"));
				return;
			}
		}
		cdb_cmd = req.json().getString("cdb-cmd");
		cdb_arg = req.json().getJSONObject("cdb-arg");
		if (!getCmdMap()) {
			response(server, req, CODE_CMD_NOT_REGISTERED, JSONArray.fromObject("[\"cdb-cmd is not registered\"]"));
			return;
		}
		processSql();
		if (mod.equalsIgnoreCase("st")) {
			if (!executeSt()) {
				response(server, req, CODE_EXEC_CMD_FAILED, JSONArray.fromObject("[\"execute statement failed\"]"));
				return;
			}
		} else if (mod.equalsIgnoreCase("sp")) {
			if (!executeSp()) {
				response(server, req, CODE_EXEC_CMD_FAILED, JSONArray.fromObject("[\"execute storeprocedure failed\"]"));
				return;
			}
		} else {
			logger.error("invalid command mode: " + mod);
			response(server, req, CODE_CMD_MOD_INVALID, JSONArray.fromObject("[\"cdb-cmd mode is invalid: " + mod + "\"]"));
			return;
		}
		response(server, req, CODE_SUCCESS, JSONArray.fromObject(outparam));
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
	
	private boolean getCmdMap() {
		Statement st = null;
		try {
			mod = sql = null;
			outcount = 0;
			outparam.clear();
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select c_mod, i_out, c_sql from tbl_cmd_map where c_cmd = '" + cdb_cmd + "'");
			if (!rs.next()) {
				logger.error("cmd is not registered: " + cdb_cmd);
				return false;
			}
			mod = rs.getString(1);
			outcount = rs.getInt(2);
			sql = rs.getString(3);
		} catch (SQLException e) {
			logger.error("failed to get map of cmd: " + cdb_cmd, e);
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
		return true;
	}
	
	private void processSql() {
		logger.info("cmd: " + cdb_cmd +  " sql before preprocess: " + sql);
		if (null == cdb_arg) return;
		@SuppressWarnings("unchecked")
		Iterator<String> i = cdb_arg.keys();
		while (i.hasNext()) {
			String k = i.next();
			String v = cdb_arg.getString(k);
			sql = sql.replace(k, v);
		}
		logger.info("cmd: " + cdb_cmd +  " sql after preprocess: " + sql);
	}
	
	private boolean executeSt() {
		Statement st = null;
		try {
			st = conn.createStatement();
			if (0 < outcount) {
				ResultSet rs = st.executeQuery(sql);
				while (rs.next()) {
					for (int i = 1; i <= outcount; i++)
						outparam.add(rs.getString(i));
				}
			} else {
				st.execute(sql);
			}
			return true;
		} catch (SQLException e) {
			logger.error("failed to process statement, cmd: " + cdb_cmd + ", mode: " + mod + ", sql: " + sql + ", out: " + outcount);
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
		return false;
	}
	
	private boolean executeSp() {
		CallableStatement st = null;
		try {
			st = conn.prepareCall("call " + sql + ";");
			if (0 < outcount) {
				for (int i = 1; i <= outcount; i++) st.registerOutParameter(i, Types.VARCHAR); 
			}
			st.execute();
			for (int i = 1; i <= outcount; i++) outparam.add(st.getString(i));
			return true;
		} catch (SQLException e) {
			logger.error("failed to process statement, cmd: " + cdb_cmd + ", mode: " + mod + ", sql: " + sql + ", out: " + outcount);
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
		return false;
	}
}
