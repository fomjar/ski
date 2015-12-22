package fomjar.util;

import org.apache.log4j.PropertyConfigurator;

public class FjLog {
	
	private static FjLogGuard guard = null;
	
	public static void startLogGuard(long intervalInSecond) {
		if (null == guard) guard = new FjLogGuard();
		long time = intervalInSecond;
		guard.setDelay(time * 1000);
		guard.setInterval(time * 1000L);
		guard.perform();
		if (!guard.isRun()) new Thread(guard, "fjlog-guard").start();
	}
	
	public static void loadLog() {
		if (null == guard) guard = new FjLogGuard();
		guard.perform();
	}
	
	public static FjLogGuard getLogGuard() {
		return guard;
	}
	
	public static class FjLogGuard extends FjLoopTask {
		@Override
		public void perform() {
			PropertyConfigurator.configure("conf/log4j.conf");
		}
	}
}
