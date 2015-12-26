package fomjar.server;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

public class FjMessageQueue {
	
	private static final Logger logger = Logger.getLogger(FjMessageQueue.class);
	private Queue<FjMessageWrapper> wrappers;
	
	public FjMessageQueue() {
		wrappers = new LinkedList<FjMessageWrapper>();
	}
	
	public synchronized void offer(FjMessageWrapper wrapper) {
		if (null == wrapper) throw new NullPointerException();
		
		wrappers.offer(wrapper);
		logger.debug("offered a new message: " + wrapper.message());
		logger.debug("there are " + size() + " messages in this queue");
		notify();
	}
	
	public synchronized FjMessageWrapper poll() {
		FjMessageWrapper wrapper = null;
		while (null == (wrapper = wrappers.poll())) {
			logger.debug("there is no message now, wait");
			try {wait();}
			catch (InterruptedException e) {logger.error("wait for message failed", e);}
		}
		logger.debug("polled a message: " + wrapper.message());
		logger.debug("there are " + size() + " messages in this queue");
		return wrapper;
	}
	
	public int size() {
		return wrappers.size();
	}
	
}
