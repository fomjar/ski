package com.ski.wca.guard;

import org.apache.log4j.Logger;

import com.ski.wca.WechatInterface;
import com.ski.wca.WechatInterface.WechatPermissionDeniedException;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjJsonMessage;
import fomjar.util.FjLoopTask;

public class CustomServiceGuard extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(CustomServiceGuard.class);
	
	public CustomServiceGuard() {
		setDelay(10 * 1000L);
	}
	
	public void start() {
		if (isRun()) {
			logger.warn("customservice-guard has already started");
			return;
		}
		new Thread(this, "customservice-guard").start();
	}
	
	@Override
	public void perform() {
		String kfaccount = FjServerToolkit.getServerConfig("wca.customservice.account");
		FjJsonMessage rsp = null;
		try {
			FjJsonMessage cur_kfaccount = WechatInterface.customServiceGet();
			if (cur_kfaccount.json().containsKey("kf_list")) rsp = WechatInterface.customServiceUpdate(kfaccount);
			else rsp = WechatInterface.customServiceAdd(kfaccount);
		} catch (WechatPermissionDeniedException e) {logger.error(e);}
		if (0 == rsp.json().getInt("errcode")) logger.info("custom service update success");
		else logger.error("custom service update failed: " + rsp);
		setNextRetryInterval(Long.parseLong(FjServerToolkit.getServerConfig("wca.customservice.reload-interval")));
	}
	
	public void setNextRetryInterval(long seconds) {
		logger.debug("will try again after " + seconds + " seconds");
		setInterval(seconds * 1000);
	}
}
