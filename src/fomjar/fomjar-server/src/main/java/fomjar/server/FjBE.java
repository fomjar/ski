package fomjar.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 业务执行器，处理某一个业务流
 * 
 * @author fomja
 */
public abstract class FjBE {
	
	private static final Logger logger = Logger.getLogger(FjBE.class);
	
	public static void dispatch(FjBE[] bes, FjMsg msg) {
		if (null == bes) {
			logger.error("no available business executor to dispatch");
			return;
		}
		if (!FjServerToolkit.isLegalMsg(msg)) {
			logger.error("can not dispatch illegal message, discard: " + msg);
			return;
		}
		FjJsonMsg jmsg = (FjJsonMsg) msg;
		String sid = jmsg.json().getString("sid");
		for (FjBE be : bes) {
			if (be.hasSession(sid)) {
				FjBE.FjSCB scb = be.getSession(sid);
				scb.nextPhase();
				boolean end = false;
				try {end = be.execute(scb, jmsg);}
				catch (Exception e) {logger.error("error occurs when execute be for this message: " + jmsg, e);}
				if (end) {
					be.closeSession(sid);
					logger.info("session " + sid + " closed");
				}
			}
		}
	}
	
	/**
	 * 会话控制块，用于记录会话状态
	 * 
	 * @author fomja
	 */
	public static class FjSCB {
		
		private String sid;
		private int phase = -1;
		private Map<String, String> data;
		
		private FjSCB() {data = new HashMap<String, String>();}
		
		public String sid() {return sid;}
		
		public void nextPhase() {++phase;}
		
		public int currPhase() {return phase;}
		
		public String getData(String key) {return data.get(key);}
		
		public Map<String, String> getData() {return data;}
		
		public void putData(String key, String value) {data.put(key, value);}
		
	}
	
	private Map<String, FjSCB> scbs;
	private String serverName;
	
	public FjBE() {
		scbs = new HashMap<String, FjSCB>();
	}
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public boolean hasSession(String sid) {
		return scbs.containsKey(sid);
	}
	
	public FjSCB getSession(String sid) {
		return scbs.get(sid);
	}
	
	public FjSCB closeSession(String sid) {
		if (!scbs.containsKey(sid)) {
			logger.error("session not found: " + sid);
			return null;
		}
		logger.debug("close session: " + sid);
		FjSCB scb = scbs.remove(sid);
		scb.putData("time.close", String.valueOf(System.currentTimeMillis()));
		return scb;
	}
	
	public FjSCB openSession(String sid) {
		logger.debug("open session: " + sid);
		FjSCB scb = new FjSCB();
		scb.sid = sid;
		scb.putData("time.open", String.valueOf(System.currentTimeMillis()));
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
	public abstract boolean execute(FjSCB scb, FjJsonMsg msg);
	
}
