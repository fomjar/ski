package com.wtcrm.fbbp;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fomjar.server.FjJsonMsg;

public abstract class BE {
	
	private static final Logger logger = Logger.getLogger(BE.class);
	
	/**
	 * 会话控制块，用于记录会话状态
	 * 
	 * @author fomja
	 */
	public static class SCB {
		private String sid;
		private int phase = -1;
		private Map<String, String> data = new HashMap<String, String>();
		
		public String sid() {return sid;}
		       void   nextPhase() {++phase;}
		public int    currPhase() {return phase;}
		public String getData(String key) {return data.get(key);}
		public void   putData(String key, String value) {data.put(key, value);}
	}
	
	private Map<String, SCB> scbs;
	private String serverName;
	
	public BE() {
		scbs = new HashMap<String, SCB>();
	}
	
	void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public boolean hasSession(String sid) {
		return scbs.containsKey(sid);
	}
	
	public SCB getSession(String sid) {
		return scbs.get(sid);
	}
	
	public SCB closeSession(String sid) {
		logger.debug("close session: " + sid);
		return scbs.remove(sid);
	}
	
	public SCB openSession(String sid) {
		logger.debug("open session: " + sid);
		SCB scb = new SCB();
		scb.sid = sid;
		scbs.put(sid, scb);
		return scb;
	}
	
	/**
	 * 执行具体业务
	 * 
	 * @param msg
	 * @param scb
	 * @return 业务全流程结束返回true，未结束返回false
	 */
	public abstract boolean execute(SCB scb, FjJsonMsg msg);
	
}
