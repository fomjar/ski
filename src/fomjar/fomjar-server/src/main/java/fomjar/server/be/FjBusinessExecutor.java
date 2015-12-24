package fomjar.server.be;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fomjar.server.FjJsonMessage;
import fomjar.server.FjMessage;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;

/**
 * 业务执行器，处理某一个业务流
 * 
 * @author fomja
 */
public abstract class FjBusinessExecutor {
	
	private static final Logger logger = Logger.getLogger(FjBusinessExecutor.class);
	
	public static void dispatch(FjBusinessExecutor[] bes, FjMessage msg) throws SessionNotOpenException {
		if (null == bes) {
			logger.error("no available business executor to dispatch");
			return;
		}
		if (!FjServerToolkit.isLegalMsg(msg)) {
			logger.error("can not dispatch illegal message, discard: " + msg);
			return;
		}
		FjJsonMessage jmsg = (FjJsonMessage) msg;
		String sid = jmsg.json().getString("sid");
		for (FjBusinessExecutor be : bes) {
			if (be.containSession(sid)) {
				FjBusinessExecutor.FjSCB scb = be.getSession(sid);
				scb.nextPhase();
				try {be.execute(scb, jmsg);}
				catch (Exception e) {logger.error("error occurs when execute BE for message: " + jmsg, e);}
				if (scb.isEnd()) {
					be.closeSession(sid);
					logger.info("session " + sid + " closed");
				}
				return;
			}
		}
		logger.error("dispatch message failed for no BE is responsible for this message: " + jmsg);
		throw new SessionNotOpenException(sid);
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
		private boolean end = false;
		
		private FjSCB() {data = new HashMap<String, String>();}
		
		public  String  sid() {return sid;}
		private void    nextPhase() {++phase;}
		public  int     currPhase() {return phase;}
		public  String  getData(String key) {return data.get(key);}
		public  Map<String, String> getData() {return data;}
		public  void    putData(String key, String value) {data.put(key, value);}
		public  void    end() {end = true;};
		private boolean isEnd() {return end;}
	}
	
	private Map<String, FjSCB> scbs;
	private FjServer server;
	
	public FjBusinessExecutor(FjServer server) {
		if (null == server) throw new NullPointerException();
		
		this.server = server;
		scbs = new HashMap<String, FjSCB>();
	}
	
	public FjServer getServer() {
		return server;
	}
	
	private boolean containSession(String sid) {
		return scbs.containsKey(sid);
	}
	
	private FjSCB getSession(String sid) {
		return scbs.get(sid);
	}
	
	private FjSCB closeSession(String sid) {
		if (!scbs.containsKey(sid)) {
			logger.error("session not found: " + sid);
			return null;
		}
		logger.info("close session: " + sid);
		FjSCB scb = scbs.remove(sid);
		scb.putData("time.close", String.valueOf(System.currentTimeMillis()));
		return scb;
	}
	
	public FjSCB openSession(String sid) {
		logger.info("open session: " + sid);
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
	public abstract void execute(FjSCB scb, FjJsonMessage msg);
	
}
