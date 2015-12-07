package com.wtcrm.fbbp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fomjar.server.FjJsonMsg;

public abstract class BE {
	
	private Map<String, List<FjJsonMsg>> sessions;
	private String serverName;
	
	public BE(String serverName) {
		this.serverName = serverName;
		sessions = new HashMap<String, List<FjJsonMsg>>();
	}
	
	public boolean match(FjJsonMsg msg) {
		return sessions.containsKey(msg.json().getString("sid"));
	}
	
	public List<FjJsonMsg> getSession(String sid) {
		if (null == sessions.get(sid)) sessions.put(sid, new ArrayList<FjJsonMsg>());
		return sessions.get(sid);
	}
	
	public List<FjJsonMsg> removeSession(String sid) {
		return sessions.remove(sid);
	}
	
	/**
	 * 执行具体业务
	 * 
	 * @param msg
	 * @param msgs_ago
	 * @return 业务全流程结束返回true，未结束返回false
	 */
	public abstract boolean execute(FjJsonMsg msg, List<FjJsonMsg> msgs_ago);
	
	public String getServerName() {
		return serverName;
	}
	
}
