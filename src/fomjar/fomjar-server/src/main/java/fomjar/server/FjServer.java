package fomjar.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import fomjar.util.FjLoopTask;

public class FjServer extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(FjServer.class);
	private String name;
	private FjMessageQueue mq;
	private List<FjServerTask> tasks;
	
	public FjServer(String name, FjMessageQueue mq) {
		this.name = name;
		this.mq = mq;
		tasks = new LinkedList<FjServerTask>();
	}
	
	public String name() {
		return name;
	}
	
	public FjMessageQueue mq() {
		return mq;
	}
	
	public void addServerTask(FjServerTask task) {
		if (null == task) throw new NullPointerException();
		synchronized (tasks) {tasks.add(task);}
	}

	@Override
	public void perform() {
		FjMessage msg = null;
		
		while (null == (msg = mq.poll()));
		
		synchronized (tasks) {
			for (FjServerTask task : tasks) {
				try {task.onMsg(this, msg);}
				catch (Exception e) {logger.error("error occurs on message: " + msg, e);}
			}
		}
		
		try {
			SocketChannel conn = mq.pollConnection(msg);
			if (null != conn) conn.close();
		} catch (IOException e) {logger.warn("error occurs when close connection for message: " + msg);}
	}
	
	public static interface FjServerTask {
		void onMsg(FjServer server, FjMessage msg);
	}

}
