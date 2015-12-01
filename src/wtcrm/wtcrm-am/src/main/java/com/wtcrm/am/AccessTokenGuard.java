package com.wtcrm.am;

import org.apache.log4j.Logger;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjLoopTask;
import fomjar.server.FjMsg;
import fomjar.server.FjSender;
import fomjar.server.FjToolkit;

public class AccessTokenGuard {

	private static AccessTokenGuard instance = null;
	public static AccessTokenGuard getInstance() {
		if (null == instance) instance = new AccessTokenGuard();
		return instance;
	}
	
	private static final Logger logger = Logger.getLogger(AccessTokenGuard.class);
	
	private WCTokenTask wctask;
	
	private AccessTokenGuard() {
		wctask = new WCTokenTask();
	}
	
	public void start() {
		Thread wcThread = new Thread(wctask);
		wcThread.setName("wc-token-guard");
		wcThread.start();
	}
	
	public void stop() {
		wctask.close();
	}
	
	public String getWcToken() {
		return wctask.token;
	}
	
	private class WCTokenTask extends FjLoopTask {
		private static final String TEMPLATE = "https://api.weixin.qq.com/cgi-bin/token?grant_type=%s&appid=%s&secret=%s";
		
		public String token;
		
		@Override
		public void perform() {
			token = null;
			long defaultRetry = Long.parseLong(FjToolkit.getServerConfig("wc.retry"));
			String url = String.format(TEMPLATE, FjToolkit.getServerConfig("wc.grant"), FjToolkit.getServerConfig("wc.appid"), FjToolkit.getServerConfig("wc.secret"));
			logger.debug("try to get wechat access token");
			FjMsg msg = FjSender.sendHttpRequest("GET", url, null);
			if (!(msg instanceof FjJsonMsg)) {
				logger.error("invalid reponse message when get wechat access token: " + msg);
				setNextRetry(defaultRetry);
				return;
			}
			FjJsonMsg rsp = (FjJsonMsg) msg;
			if (!rsp.json().containsKey("access_token") || !rsp.json().containsKey("expires_in")) {
				logger.error("failed to get wechat access token, error response: " + msg);
				setNextRetry(defaultRetry);
				return;
			}
			token = rsp.json().getString("access_token");
			setNextRetry(Long.parseLong(rsp.json().getString("expires_in")) * 1000);
			logger.info("got wechat access token successfully: " + rsp);
		}
		
		public void setNextRetry(long milliSeconds) {
			logger.info("will try again after " + milliSeconds + " milli seconds");
			setInterval(milliSeconds);
		}
	}
}
