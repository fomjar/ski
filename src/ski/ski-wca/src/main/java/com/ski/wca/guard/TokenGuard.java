package com.ski.wca.guard;

import org.apache.log4j.Logger;

import com.ski.wca.WechatInterface;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjJsonMessage;
import fomjar.util.FjLoopTask;

public class TokenGuard extends FjLoopTask {
	
	private static TokenGuard instance = null;
	public static TokenGuard getInstance() {
		if (null == instance) instance = new TokenGuard();
		
		return instance;
	}
	
	private static final Logger logger = Logger.getLogger(TokenGuard.class);
	
	private String serverName;
	private String token;
	
	private TokenGuard() {
	}
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public void start() {
		if (isRun()) {
			logger.warn("token-guard has already started");
			return;
		}
		new Thread(this, "token-guard").start();
	}
	
	public String getToken() {
		return token;
	}
	
	@Override
	public void perform() {
		token = null;
		long defaultInterval = Long.parseLong(FjServerToolkit.getServerConfig("wca.token.reload-interval"));
		FjJsonMessage token_msg = WechatInterface.token(getServerName(), FjServerToolkit.getServerConfig("wca.appid"), FjServerToolkit.getServerConfig("wca.secret"));
		if (null == token_msg || !token_msg.json().containsKey("access_token") || !token_msg.json().containsKey("expires_in")) {
			logger.error("get wechat access token failed: " + token_msg);
			setNextRetryInterval(defaultInterval);
			return;
		}
		token = token_msg.json().getString("access_token");
		logger.info("get wechat access token successfully: " + token_msg);
		setNextRetryInterval(token_msg.json().getInt("expires_in"));
	}
	
	public void setNextRetryInterval(long seconds) {
		logger.debug("will try again after " + seconds + " seconds");
		setInterval(seconds * 1000);
	}
	
}
