package fomjar.util;

import org.apache.log4j.Logger;

public abstract class FjLoopTask implements Runnable {

	private static final Logger logger = Logger.getLogger(FjLoopTask.class);
	private long delay;
	private long interval;
	private boolean isRun;
	
	public FjLoopTask() {
		this(0L, 0L);
	}
	
	public FjLoopTask(long interval) {
		this(0L, interval);
	}
	
	public FjLoopTask(long delay, long interval) {
		this.delay = delay;
		this.interval = interval;
		isRun = false;
	}
	
	/**
	 * @return milliseconds
	 */
	public long getDelay() {
		return delay;
	}
	
	/**
	 * @param delay in milliseconds
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}
	
	/**
	 * @return milliseconds
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * @param interval in millisecond
	 */
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
		try {Thread.sleep(getDelay());}
		catch (InterruptedException e) {logger.warn("thread sleep interupted", e);}
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
