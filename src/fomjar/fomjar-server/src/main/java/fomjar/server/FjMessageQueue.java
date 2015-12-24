package fomjar.server;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

public class FjMessageQueue {
	
	private static final Logger logger = Logger.getLogger(FjMessageQueue.class);
	private Queue<FjMessage> msgs;
	private Map<FjMessage, SocketChannel> conns;
	
	public FjMessageQueue() {
		msgs = new LinkedList<FjMessage>();
		conns = new HashMap<FjMessage, SocketChannel>();
	}
	
	public void offer(FjMessage msg) {
		offer(msg, null);
	}
	
	public synchronized void offer(FjMessage msg, SocketChannel conn) {
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
	
	public synchronized FjMessage poll() {
		FjMessage msg = null;
		while (null == (msg = msgs.poll())) {
			logger.debug("there is no message now, wait");
			try {wait();}
			catch (InterruptedException e) {logger.error("wait for message failed", e);}
		}
		logger.debug("polled a message: " + msg);
		logger.debug("there are " + size() + " messages in this queue");
		return msg;
	}
	
	public SocketChannel pollConnection(FjMessage msg) {
		return conns.remove(msg);
	}
	
	public int size() {
		return msgs.size();
	}
	
}
