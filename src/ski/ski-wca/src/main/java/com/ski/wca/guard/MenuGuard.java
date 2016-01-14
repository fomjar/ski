package com.ski.wca.guard;

import org.apache.log4j.Logger;

import com.ski.wca.WechatInterface;
import com.ski.wca.WechatInterface.WechatPermissionDeniedException;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjJsonMessage;
import fomjar.util.FjLoopTask;

public class MenuGuard extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(MenuGuard.class);
	
	public MenuGuard() {
		setDelay(10 * 1000L);
	}
	
	public void start() {
		if (isRun()) {
			logger.warn("menu-guard has already started");
			return;
		}
		new Thread(this, "menu-guard").start();
	}
	
	@Override
	public void perform() {
		setNextRetryInterval(Long.parseLong(FjServerToolkit.getServerConfig("wca.menu.reload.interval")));
		
		boolean menu_switch = "on".equalsIgnoreCase(FjServerToolkit.getServerConfig("wca.menu.reload.switch"));
		if (!menu_switch) return;
		
		String  menu_content = FjServerToolkit.getServerConfig("wca.menu.content");
		FjJsonMessage rsp = null;
		try {
			if (null == menu_content || 0 == menu_content.length()) rsp = WechatInterface.menuDelete();
			else rsp = WechatInterface.menuCreate(menu_content);
		} catch (WechatPermissionDeniedException e) {logger.error(e);}
		if (0 == rsp.json().getInt("errcode")) logger.info("menu update success");
		else logger.error("menu update failed: " + rsp);
	}
	
	public void setNextRetryInterval(long seconds) {
		logger.debug("will try again after " + seconds + " seconds");
		setInterval(seconds * 1000);
	}
}
