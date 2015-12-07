package com.wtcrm.am;

import java.net.Socket;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import fomjar.server.FjHttpRequest;
import fomjar.server.FjHttpResponse;
import fomjar.server.FjJsonMsg;
import fomjar.server.FjMsg;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjToolkit;

public class WeChatTask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(WeChatTask.class);
	
	public WeChatTask() {
		AccessTokenGuard.getInstance().start();
	}
	
	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (msg instanceof FjJsonMsg
				&& ((FjJsonMsg) msg).json().containsKey("fs")
				&& ((FjJsonMsg) msg).json().containsKey("ts")
				&& ((FjJsonMsg) msg).json().containsKey("sid")) {
			logger.info("message comes from wtcrm server");
			processWtcrm(server, (FjJsonMsg) msg);
		} else if (msg instanceof FjHttpRequest) {
			logger.info("message comes from wechat server");
			processWechat(server, (FjHttpRequest) msg);
		} else {
			logger.error("unrecognized message: " + msg);
		}
	}
	
	private void processWtcrm(FjServer server, FjJsonMsg rsp) {
		
	}
	
	private void processWechat(FjServer server, FjHttpRequest msg) {
		if (msg.titleParams().containsKey("echostr")) {
			logger.info("wechat access message");
			processWechatAccess(server, msg);
		} else {
			logger.info("wechat common message");
			processWechatCommon(server, msg);
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
	private void processWechatAccess(FjServer server, FjHttpRequest msg) {
		responseWechatRequest(server, server.mq().pollConnection(msg), "text/plain", msg.titleParam("echostr"));
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
	private void processWechatCommon(FjServer server, FjHttpRequest msg) {
		responseWechatRequest(server, server.mq().pollConnection(msg), "text/xml", createWechatResponseBody(msg, msg.bodyToJson().getString("Content")));
	}
	
	private static void responseWechatRequest(FjServer server, Socket conn, String bodyType, String body) {
		FjMsg rsp = new FjHttpResponse(bodyType, body);
		FjToolkit.getSender(server.name()).send(rsp, conn);
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
