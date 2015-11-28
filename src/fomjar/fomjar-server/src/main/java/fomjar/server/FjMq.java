package fomjar.server;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

public class FjMq {
	
	private static final Logger logger = Logger.getLogger(FjMq.class);
	private Queue<FjMsg> queue;
	
	public FjMq() {
		queue = new LinkedList<FjMsg>();
	}
	
	public synchronized void offer(FjMsg msg) {
		if (null == msg) {
			logger.error("there is no need to offer a null message");
			return;
		}
		queue.offer(msg);
		logger.debug("offered a new message: " + msg);
		logger.debug("there are " + size() + " messages in this queue");
		notify();
	}
	
	public synchronized FjMsg poll() {
		FjMsg msg = null;
		while (null == (msg = queue.poll())) {
			logger.debug("there is no message now, wait");
			try {wait();}
			catch (InterruptedException e) {logger.error("wait for message failed", e);}
		}
		logger.debug("polled a message: " + msg);
		logger.debug("there are " + size() + " messages in this queue");
		return msg;
	}
	
	public int size() {
		return queue.size();
	}
	
}
