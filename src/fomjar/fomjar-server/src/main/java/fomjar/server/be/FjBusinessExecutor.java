package fomjar.server.be;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fomjar.server.FjServer;
import fomjar.server.msg.FjDSCPMessage;

/**
 * 业务执行器，处理某一个业务流
 * 
 * @author fomja
 */
public abstract class FjBusinessExecutor {
	
	private static final Logger logger = Logger.getLogger(FjBusinessExecutor.class);
	
	public static void dispatch(FjBusinessExecutor[] bes, FjDSCPMessage msg) throws SessionNotOpenException {
		if (null == bes) {
			logger.error("no available business executor to dispatch");
			return;
		}
		for (FjBusinessExecutor be : bes) {
			if (be.containSession(msg.sid())) {
				FjBusinessExecutor.FjSCB scb = be.getSession(msg.sid());
				try {be.execute(scb, msg);}
				catch (Exception e) {logger.error("error occurs when execute BE for message: " + msg, e);}
				if (scb.isEnd()) be.closeSession(msg.sid());
				return;
			}
		}
		logger.error("dispatch message failed for no BE is responsible for this message: " + msg);
		throw new SessionNotOpenException(msg.sid());
	}
	
	/**
	 * 会话控制块，用于记录会话状态
	 * 
	 * @author fomja
	 */
	public static class FjSCB {
		
		private String sid;
		private Map<String, Object> data;
		private boolean end = false;
		
		private FjSCB() {data = new HashMap<String, Object>();}
		
		public  String  sid() {return sid;}
		public  Object  get(String key)       {return data.get(key);}
		public  String  getString(String key) {return (String) data.get(key);}
		public  char    getChar(String key)   {return (char) data.get(key);}
		public  short   getShort(String key)  {return (short) data.get(key);}
		public  int     getInt(String key)    {return (int) data.get(key);}
		public  long    getLong(String key)   {return (long) data.get(key);}
		public  float   getFloat(String key)  {return (float) data.get(key);}
		public  double  getDouble(String key) {return (double) data.get(key);}
		public  Map<String, Object> getAll() {return data;}
		public  void    put(String key, Object value) {data.put(key, value);}
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
	
	public FjSCB closeSession(String sid) {
		if (!scbs.containsKey(sid)) {
			logger.error("session not found: " + sid);
			return null;
		}
		logger.info("close session: " + sid);
		FjSCB scb = scbs.remove(sid);
		scb.put("time.close", System.currentTimeMillis());
		return scb;
	}
	
	public FjSCB openSession(String sid) {
		logger.info("open session: " + sid);
		FjSCB scb = new FjSCB();
		scb.sid = sid;
		scb.put("time.open", System.currentTimeMillis());
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
	public abstract void execute(FjSCB scb, FjDSCPMessage msg);
	
}
