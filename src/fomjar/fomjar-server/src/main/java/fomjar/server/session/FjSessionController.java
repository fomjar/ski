package fomjar.server.session;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fomjar.server.FjServer;
import fomjar.server.msg.FjDscpMessage;

/**
 * 业务执行器，处理某一个业务流
 * 
 * @author fomja
 */
public abstract class FjSessionController {
	
	private static final Logger logger = Logger.getLogger(FjSessionController.class);
	
	public static void dispatch(FjSessionController[] scs, FjDscpMessage msg) throws FjSessionNotOpenException {
		if (null == scs) {
			logger.error("no available business executor to dispatch");
			return;
		}
		for (FjSessionController sc : scs) {
			if (sc.containSession(msg.sid())) {
				FjSessionController.FjSCB scb = sc.getSession(msg.sid());
				try {sc.onSession(scb, msg);}
				catch (Exception e) {logger.error("error occurs when execute BE for message: " + msg, e);}
				if (scb.isEnd()) sc.closeSession(msg.sid());
				return;
			}
		}
		logger.error("dispatch message failed for no session controller is responsible for this message: " + msg);
		throw new FjSessionNotOpenException(msg.sid());
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
		public  byte    getByte(String key)   {return (byte) data.get(key);}
		public  char    getChar(String key)   {return (char) data.get(key);}
		public  short   getShort(String key)  {return (short) data.get(key);}
		public  int     getInt(String key)    {return (int) data.get(key);}
		public  long    getLong(String key)   {return (long) data.get(key);}
		public  float   getFloat(String key)  {return (float) data.get(key);}
		public  double  getDouble(String key) {return (double) data.get(key);}
		public  Map<String, Object> getAll()  {return data;}
		public  FjSCB   put(String key, Object value) {data.put(key, value); return this;}
		public  void    end() {end = true;};
		private boolean isEnd() {return end;}
	}
	
	private Map<String, FjSCB> scbs;
	private FjServer server;
	
	public FjSessionController(FjServer server) {
		if (null == server) throw new NullPointerException();
		
		this.server = server;
		scbs = new HashMap<String, FjSCB>();
	}
	
	public FjServer getServer() {return server;}
	
	private boolean containSession(String sid) {return scbs.containsKey(sid);}
	
	private FjSCB getSession(String sid) {return scbs.get(sid);}
	
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
	 * 执行具体会话
	 * 
	 * @param scb
	 * @param msg
	 */
	public abstract void onSession(FjSCB scb, FjDscpMessage msg);
	
}
