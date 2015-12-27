package com.ski.wca;

import java.nio.channels.SocketChannel;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDSCPMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjMessage;

public class WCATask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(WCATask.class);
	
	
	public WCATask(String name) {
		TokenGuard.getInstance().setServerName(name);
		TokenGuard.getInstance().start();
	}
	
	@Override
	public void onMessage(FjServer server, FjMessageWrapper wrapper) {
		FjMessage msg = wrapper.message();
		if (msg instanceof FjDSCPMessage) {
			logger.info("message comes from SKI server");
			processSKI(server.name(), (FjDSCPMessage) msg);
		} else if (msg instanceof FjHttpRequest) {
			logger.info("message comes from wechat server: " + msg);
			processWechat(server.name(), wrapper);
			wrapper.attach("conn", null);
		} else {
			logger.error("invalid message, discard: " + msg);
		}
	}
	
	private void processSKI(String serverName, FjDSCPMessage rsp) {
		
	}
	
	private void processWechat(String serverName, FjMessageWrapper wrapper) {
		if (((FjHttpRequest) wrapper.message()).titleParams().containsKey("echostr")) {
			logger.info("response access message");
			WechatInterface.access(serverName, wrapper);
			return;
		}
		String msgType = ((FjHttpRequest) wrapper.message()).contentToJson().getString("MsgType");
		if ("text".equals(msgType)) {
			processWechatText(serverName, wrapper);
			return;
		}
		if ("event".equals(msgType)) {
			processWechatEvent(serverName, wrapper);
			return;
		}
	}
	
	private void processWechatText(String serverName, FjMessageWrapper wrapper) {
		JSONObject json = ((FjHttpRequest) wrapper.message()).contentToJson();
		String from     = json.getString("FromUserName");
		String to       = json.getString("ToUserName");
		String content  = json.getString("Content");
		WechatInterface.responseMessage(serverName, (SocketChannel) wrapper.attachment("conn"), from, to, "text", content);
		if ("menu_create".equals(content)) {
			logger.error(WechatInterface.menuCreate(serverName));
		}
		if ("menu_delete".equals(content)) {
			logger.error(WechatInterface.menuDelete(serverName));
		}
	}
	
	private void processWechatEvent(String serverName, FjMessageWrapper wrapper) {
		
	}
}
