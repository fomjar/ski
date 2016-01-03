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

import com.ski.common.DSCP;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpRequest;
import fomjar.server.msg.FjDscpResponse;
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
		public int          cmd     = DSCP.CMD.SYSTEM_UNKNOWN_COMMAND;
		public JSONObject   arg     = null;
		public String       mod     = null;
		public int          out     = 0;
		public String       sql_ori = null;
		public String       sql_use = null;
		public List<String> result  = null;
		public String       err     = null;
	}
	
	@Override
	public void onMessage(FjServer server, FjMessageWrapper wrapper) {
		FjMessage msg = wrapper.message();
		if (!(msg instanceof FjDscpRequest)) {
			logger.error("illegal request, discard: " + msg);
			return;
		}
		FjDscpRequest req = (FjDscpRequest) msg;
		if (null == conn) {
			if (!initConn()) {
				logger.error("init databse connection failed, server works abnormally");
				response(server.name(), req, DSCP.CODE.CDB_DB_STATE_ABNORMAL, "{'error':'db state abnormal'}");
				return;
			}
		}
		CdbCmdInfo cci = new CdbCmdInfo();
		cci.cmd = req.cmd();
		cci.arg = (JSONObject) req.arg();
		if (!getCmdInfo(cci)) {
			logger.error("command is not registered: " + cci.cmd);
			response(server.name(), req, DSCP.CODE.CDB_CMD_NOT_REGISTERED, "{'error':\"" + cci.err + "\"}");
			return;
		}
		generateSql(cci);
		if (executeSql(cci)) response(server.name(), req, DSCP.CODE.SYSTEM_SUCCESS, String.format("{'result':%s}", JSONArray.fromObject(cci.result)));
		else {
			logger.error(String.format("execute command(%d) failed for sql: %s", cci.cmd, cci.sql_use));
			response(server.name(), req, DSCP.CODE.CDB_EXECUTE_FAILED, String.format("{'error':\"" + cci.err + "\"}"));
		}
	}
	
	private static void response(String serverName, FjDscpRequest req, int code, String desc) {
		FjDscpResponse rsp = new FjDscpResponse();
		rsp.json().put("fs",   serverName);
		rsp.json().put("ts",   req.fs());
		rsp.json().put("sid",  req.sid());
		rsp.json().put("ssn",  req.ssn() + 1);
		rsp.json().put("code", code);
		rsp.json().put("desc", desc);
		FjServerToolkit.getSender(serverName).send(rsp);
	}
	
	private boolean getCmdInfo(CdbCmdInfo cci) {
		Statement st = null;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select c_mod, i_out, c_sql from tbl_cmd_map where i_cmd = " + cci.cmd + "");
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
	
	private static void generateSql(CdbCmdInfo cci) {
		logger.info(String.format("cmd(%d) sql-ori: %s", cci.cmd, cci.sql_ori));
		cci.sql_use = cci.sql_ori;
		@SuppressWarnings("unchecked")
		Iterator<String> i = cci.arg.keys();
		while (i.hasNext()) {
			String k = i.next();
			String v = cci.arg.getString(k);
			cci.sql_use = cci.sql_use.replace("$" + k, v);
		}
		logger.info(String.format("cmd(%d) sql-use: %s", cci.cmd, cci.sql_use));
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
