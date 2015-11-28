package fomjar.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class FjServer extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(FjServer.class);
	private String name;
	private FjMq mq;
	private FjConnectionCache cache;
	private List<FjServerTask> tasks;
	
	public FjServer(String name, FjMq mq) {
		this.name = name;
		this.mq = mq;
		cache = new FjConnectionCache();
		tasks = new LinkedList<FjServerTask>();
	}
	
	public String name() {
		return name;
	}
	
	public FjMq mq() {
		return mq;
	}
	
	public FjConnectionCache cache() {
		return cache;
	}

	public void addServerTask(FjServerTask task) {
		if (null == task) throw new NullPointerException();
		synchronized (tasks) {tasks.add(task);}
	}

	@Override
	public void perform() {
		FjMsg msg = null;
		
		while (null == (msg = mq.poll()));
		
		synchronized (tasks) {
			for (FjServerTask task : tasks) {
				try {task.onMsg(this, msg);}
				catch (Exception e) {logger.error("error occurs on message: " + msg, e);}
			}
		}
		
		if (!msg.isSending() && !cache().has(msg)) {
			try {if (null != msg.conn()) msg.conn().close();}
			catch (IOException e) {e.printStackTrace();}
		}
	}
	
	public static interface FjServerTask {
		void onMsg(FjServer server, FjMsg msg);
	}

}
