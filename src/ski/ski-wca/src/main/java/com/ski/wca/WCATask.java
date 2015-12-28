package com.ski.wca;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.ski.wca.guard.MenuGuard;
import com.ski.wca.guard.TokenGuard;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjSender;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjMessage;

public class WCATask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(WCATask.class);
	
	public WCATask(String name) {
		TokenGuard.getInstance().setServerName(name);
		TokenGuard.getInstance().start();
		new MenuGuard(name).start();
	}
	
	@Override
	public void onMessage(FjServer server, FjMessageWrapper wrapper) {
		FjMessage msg = wrapper.message();
		if (msg instanceof FjDscpMessage) {
			logger.info("message comes from SKI server");
			processSKI(server.name(), (FjDscpMessage) msg);
		} else if (msg instanceof FjHttpRequest) {
			if (((FjHttpRequest) msg).url().startsWith("/wechat")) {
				logger.info("message comes from wechat server: " + msg);
				processWechat(wrapper);
			} else {
				logger.error("invalid message, discard: " + msg);
			}
		} else {
			logger.error("invalid message, discard: " + msg);
		}
	}
	
	private void processSKI(String serverName, FjDscpMessage msg) {
	}
	
	private void processWechat(FjMessageWrapper wrapper) {
		if (((FjHttpRequest) wrapper.message()).urlParameters().containsKey("echostr")) {
			logger.info("response access message");
			WechatInterface.access(wrapper);
			return;
		}
		String msgType = ((FjHttpRequest) wrapper.message()).contentToXml().getDocumentElement().getElementsByTagName("MsgType").item(0).getTextContent();
		if ("text".equals(msgType)) {
			processWechatText(wrapper);
			return;
		}
		if ("event".equals(msgType)) {
			processWechatEvent(wrapper);
			return;
		}
	}
	
	private void processWechatText(FjMessageWrapper wrapper) {
		Element xml     = ((FjHttpRequest) wrapper.message()).contentToXml().getDocumentElement();
		String from     = xml.getElementsByTagName("FromUserName").item(0).getTextContent();
		String to       = xml.getElementsByTagName("ToUserName").item(0).getTextContent();
		String content  = xml.getElementsByTagName("Content").item(0).getTextContent();
		FjSender.sendHttpResponse(new FjHttpResponse(WechatInterface.createMessage(from, to, "text", content)), (SocketChannel) wrapper.attachment("conn"));
	}
	
	private void processWechatEvent(FjMessageWrapper wrapper) {
		Element xml      = ((FjHttpRequest) wrapper.message()).contentToXml().getDocumentElement();
		String from      = xml.getElementsByTagName("FromUserName").item(0).getTextContent();
		String to        = xml.getElementsByTagName("ToUserName").item(0).getTextContent();
		String event     = xml.getElementsByTagName("Event").item(0).getTextContent();
		String event_key = xml.getElementsByTagName("EventKey").item(0).getTextContent();
		if ("CLICK".equalsIgnoreCase(event)) FjSender.sendHttpResponse(new FjHttpResponse(WechatInterface.createMessage(from, to, "text", event_key)), (SocketChannel) wrapper.attachment("conn"));
	}
}
