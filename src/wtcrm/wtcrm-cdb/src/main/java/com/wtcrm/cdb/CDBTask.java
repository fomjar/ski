package com.wtcrm.cdb;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjMsg;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjToolkit;

public class CDBTask implements FjServerTask {
	
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
	
	private String       cmd;
	private JSONObject   arg;
	private String       mod;
	private int          outcount;
	private String       sql;
	private List<String> outparam = new LinkedList<String>();
	
	private void getCommandMap() {
		Statement st = null;
		try {
			mod = sql = null;
			outcount = 0;
			outparam.clear();
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select c_mod, i_out, c_sql from tbl_cmd_map where c_cmd = '" + cmd + "'");
			if (!rs.next()) {
				logger.error("cmd is not registered: " + cmd);
				return;
			}
			mod = rs.getString(1);
			outcount = rs.getInt(2);
			sql = rs.getString(3);
		} catch (SQLException e) {
			logger.error("failed to get map of cmd: " + cmd, e);
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
	}
	
	private void preprocessSql() {
		logger.info("cmd: " + cmd +  " sql before preprocess: " + sql);
		if (null == arg) return;
		@SuppressWarnings("unchecked")
		Iterator<String> i = arg.keys();
		while (i.hasNext()) {
			String k = i.next();
			String v = arg.getString(k);
			sql = sql.replace(k, v);
		}
		logger.info("cmd: " + cmd +  " sql after preprocess: " + sql);
	}
	
	private void processSt() {
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
		} catch (SQLException e) {
			logger.error("failed to process statement, cmd: " + cmd + ", mode: " + mod + ", sql: " + sql + ", out: " + outcount);
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
	}
	
	private void processSp() {
		CallableStatement st = null;
		try {
			st = conn.prepareCall("call " + sql + ";");
			if (0 < outcount) {
				for (int i = 1; i <= outcount; i++) st.registerOutParameter(i, Types.VARCHAR); 
			}
			st.execute();
			for (int i = 1; i <= outcount; i++) outparam.add(st.getString(i));
		} catch (SQLException e) {
			logger.error("failed to process statement, cmd: " + cmd + ", mode: " + mod + ", sql: " + sql + ", out: " + outcount);
		} finally {
			try {if (null != st) st.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
	}
	
	private void responseIfNeed(FjServer server, FjJsonMsg req) {
		if (0 == outcount) return;
		if (outcount != outparam.size()) {
			logger.error("failed to reply request: " + req + " for output param count is mismatch: " + outparam);
			return;
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("fs",  server.name());
		data.put("ts",  req.json().getString("ts"));
		data.put("sid", req.json().getString("sid"));
		data.put("rst", outparam);
		FjToolkit.getSender(server.name()).send(FjMsg.create(data.toString()));
	}

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (null == conn) {
			if (!initConn()) {
				logger.error("init databse connection failed, server works abnormally");
				return;
			}
		}
		if (!(msg instanceof FjJsonMsg)) {
			logger.error("invalid request: " + msg);
			return;
		}
		FjJsonMsg req = (FjJsonMsg) msg;
		if (!req.json().containsKey("cmd")) {
			logger.error("invalid cdb request: " + msg);
			return;
		}
		cmd = req.json().getString("cmd");
		arg = req.json().containsKey("arg") ? req.json().getJSONObject("arg") : null;
		getCommandMap();
		preprocessSql();
		if (mod.equalsIgnoreCase("st")) processSt();
		else if (mod.equalsIgnoreCase("sp")) processSp();
		else logger.error("invalid command mode: " + mod);
		responseIfNeed(server, req);
	}
}
