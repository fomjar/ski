package fomjar.server;

import org.apache.log4j.Logger;

public abstract class FjLoopTask implements Runnable {

	private static final Logger logger = Logger.getLogger(FjLoopTask.class);
	private long interval;
	private boolean isRun;
	
	public FjLoopTask() {
		this(0l);
	}
	
	public FjLoopTask(long interval) {
		this.interval = interval;
		isRun = false;
	}
	
	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public boolean isRun() {
		return isRun;
	}
	
	public void close()	 {
		isRun = false;
	}

	@Override
	public void run() {
		isRun = true;
		while(isRun) {
			long start = System.currentTimeMillis();
			try {perform();}
			catch (Exception e) {logger.error("perform task failed!", e);}
			long end = System.currentTimeMillis();
			long delta = end - start;
			if (delta >= getInterval()) Thread.yield();
			else {
				try {Thread.sleep(getInterval() - delta);}
				catch (InterruptedException e) {logger.warn("thread sleep interupted", e);}
			}
		}
	}
	
	public abstract void perform();
	
}
