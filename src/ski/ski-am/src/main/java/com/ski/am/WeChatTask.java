package com.ski.am;

import java.nio.channels.SocketChannel;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDSCPMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjMessage;

public class WeChatTask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(WeChatTask.class);
	
	private AccessTokenGuard guard;
	
	public WeChatTask(String name) {
		guard = new AccessTokenGuard(name);
		guard.start();
	}
	
	@Override
	public void onMessage(FjServer server, FjMessageWrapper wrapper) {
		FjMessage msg = wrapper.message();
		if (msg instanceof FjDSCPMessage) {
			logger.info("message comes from SKI server");
			processSKI(server.name(), (FjDSCPMessage) msg);
		} else if (msg instanceof FjHttpRequest) {
			logger.info("message comes from wechat server");
			processWechat(server.name(), (FjHttpRequest) msg, (SocketChannel) wrapper.attachment("conn"));
		} else {
			logger.error("invalid message, discard: " + msg);
		}
	}
	
	private void processSKI(String serverName, FjDSCPMessage rsp) {
		
	}
	
	private void processWechat(String serverName, FjHttpRequest msg, SocketChannel conn) {
		if (msg.titleParams().containsKey("echostr")) {
			logger.info("wechat access message");
			processWechatAccess(serverName, msg, conn);
		} else {
			logger.info("wechat common message");
			processWechatCommon(serverName, msg, conn);
		}
	}
	
	/**
	 * access message demo:
	 * <pre>
	 * GET /wechat?signature=47e8091f63a0d2042a94b373ab8374aa2df012a7&echostr=7133327084041532349&timestamp=1448367037&nonce=1572789203 HTTP/1.0
	 * User-Agent: Mozilla/4.0
	 * Accept: *\/*
	 * Host: 120.55.195.12
	 * Pragma: no-cache
	 * Connection: Keep-Alive
	 * 
	 * </pre>
	 * 
	 * @param server
	 * @param msg
	 */
	private void processWechatAccess(String serverName, FjHttpRequest msg, SocketChannel conn) {
		responseWechatRequest(serverName, conn, "text/plain", msg.titleParam("echostr"));
	}
	
	/**
	 * request:
	 * <xml>
	 * <ToUserName><![CDATA[gh_8b1e54d8e5df]]></ToUserName>
	 * <FromUserName><![CDATA[oRojEwPTK3o2cYrLsXuuX-FuypBM]]></FromUserName>
	 * <CreateTime>1448554450</CreateTime>
	 * <MsgType><![CDATA[text]]></MsgType>
	 * <Content><![CDATA[fuck]]></Content>
	 * <MsgId>6221493989626260549</MsgId>
	 * </xml>
	 * 
	 * response:
	 * <xml>
	 * <ToUserName><![CDATA[toUser]]></ToUserName>
	 * <FromUserName><![CDATA[fromUser]]></FromUserName>
	 * <CreateTime>12345678</CreateTime>
	 * <MsgType><![CDATA[text]]></MsgType>
	 * <Content><![CDATA[你好]]></Content>
	 * </xml>
	 * 
	 * @param server
	 * @param msg
	 */
	private void processWechatCommon(String serverName, FjHttpRequest msg, SocketChannel conn) {
		responseWechatRequest(serverName, conn, "text/xml", createWechatResponseBody(msg, msg.bodyToJson().getString("Content")));
	}
	
	private static void responseWechatRequest(String serverName, SocketChannel conn, String bodyType, String body) {
		FjMessage rsp = new FjHttpResponse(bodyType, body);
		FjMessageWrapper wrapper = FjServerToolkit.getSender(serverName).wrap(rsp).attach("conn", conn);
		FjServerToolkit.getSender(serverName).send(wrapper);
		logger.info("response wechat message: " + rsp.toString());
	}
	
	private static final String TEMPLATE = "<xml>\r\n"
			+ "<ToUserName><![CDATA[%s]]></ToUserName>\r\n"
			+ "<FromUserName><![CDATA[%s]]></FromUserName>\r\n"
			+ "<CreateTime>%d</CreateTime>\r\n"
			+ "<MsgType><![CDATA[text]]></MsgType>\r\n"
			+ "<Content><![CDATA[%s]]></Content>\r\n"
			+ "</xml>";
	
	private static String createWechatResponseBody(FjHttpRequest msg, String content) {
		JSONObject msgBody = msg.bodyToJson();
		return String.format(TEMPLATE, msgBody.getString("FromUserName"), msgBody.getString("ToUserName"), System.currentTimeMillis() / 1000, content);
	}
}
