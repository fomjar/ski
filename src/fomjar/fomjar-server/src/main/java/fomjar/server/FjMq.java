package fomjar.server;

import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

public class FjMq {
	
	private static final Logger logger = Logger.getLogger(FjMq.class);
	private Queue<FjMsg> msgs;
	private Map<FjMsg, Socket> conns;
	
	public FjMq() {
		msgs = new LinkedList<FjMsg>();
		conns = new HashMap<FjMsg, Socket>();
	}
	
	public void offer(FjMsg msg) {
		offer(msg, null);
	}
	
	public synchronized void offer(FjMsg msg, Socket conn) {
		if (null == msg) {
			logger.error("there is no need to offer a null message");
			return;
		}
		if (null != conn) conns.put(msg, conn);
		msgs.offer(msg);
		logger.debug("offered a new message: " + msg);
		logger.debug("there are " + size() + " messages in this queue");
		notify();
	}
	
	public synchronized FjMsg poll() {
		FjMsg msg = null;
		while (null == (msg = msgs.poll())) {
			logger.debug("there is no message now, wait");
			try {wait();}
			catch (InterruptedException e) {logger.error("wait for message failed", e);}
		}
		logger.debug("polled a message: " + msg);
		logger.debug("there are " + size() + " messages in this queue");
		return msg;
	}
	
	public Socket pollConnection(FjMsg msg) {
		return conns.remove(msg);
	}
	
	public int size() {
		return msgs.size();
	}
	
}
