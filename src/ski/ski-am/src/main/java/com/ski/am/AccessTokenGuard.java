package com.ski.am;

import org.apache.log4j.Logger;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjJsonMessage;
import fomjar.server.msg.FjMessage;
import fomjar.util.FjLoopTask;

public class AccessTokenGuard extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(AccessTokenGuard.class);
	
	private static final String TEMPLATE = "https://api.weixin.qq.com/cgi-bin/token?grant_type=%s&appid=%s&secret=%s";
	
	private String serverName;
	public String token;
	
	public AccessTokenGuard(String serverName) {
		this.serverName = serverName;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public void start() {
		if (isRun()) {
			logger.warn("access-token-guard has already started");
			return;
		}
		new Thread(this, "access-token-guard").start();
	}
	
	public String getToken() {
		return token;
	}
	
	@Override
	public void perform() {
		token = null;
		long defaultInterval = Long.parseLong(FjServerToolkit.getServerConfig("wcam.reload-token-interval"));
		String url = String.format(TEMPLATE, FjServerToolkit.getServerConfig("wcam.grant"), FjServerToolkit.getServerConfig("wcam.appid"), FjServerToolkit.getServerConfig("wcam.secret"));
		logger.debug("try to get wechat access token");
		FjMessage msg = FjServerToolkit.getSender(getServerName()).sendHttpRequest("GET", url, null);
		if (!(msg instanceof FjJsonMessage)) {
			logger.error("invalid reponse message when get wechat access token: " + msg);
			setNextRetryInterval(defaultInterval);
			return;
		}
		FjJsonMessage rsp = (FjJsonMessage) msg;
		if (!rsp.json().containsKey("access_token") || !rsp.json().containsKey("expires_in")) {
			logger.error("failed to get wechat access token, error response: " + msg);
			setNextRetryInterval(defaultInterval);
			return;
		}
		token = rsp.json().getString("access_token");
		setNextRetryInterval(Long.parseLong(rsp.json().getString("expires_in")));
		logger.info("got wechat access token successfully: " + rsp);
	}
	
	public void setNextRetryInterval(long seconds) {
		logger.info("will try again after " + seconds + " seconds");
		setInterval(seconds * 1000);
	}
	
}
