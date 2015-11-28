package com.wtcrm.am;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjMsg;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjToolkit;

public class WCAMTask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(WCAMTask.class);
	
	public WCAMTask() {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	}

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (msg instanceof FjJsonMsg
				&& ((FjJsonMsg) msg).json().containsKey("fm")
				&& ((FjJsonMsg) msg).json().containsKey("tm")) {
			logger.info("message comes from wtcrm server");
			processWtcrm(server, (FjJsonMsg) msg);
		} else if (msg.toString().contains(" /wechat")) {
			logger.info("message comes from wechat server");
			processWechat(server, msg);
		} else {
			logger.error("unrecognized message: " + msg);
		}
	}
	
	private void processWtcrm(FjServer server, FjJsonMsg rsp) {
		
	}
	
	public static Map<String, String> parseWechatParams(String data) {
		String paramLine = data.substring(data.indexOf("?") + 1, data.indexOf(" HTTP"));
		Map<String, String> params = new HashMap<String, String>();
		for (String param : paramLine.split("&")) {
			String k = param.split("=")[0];
			String v = param.split("=")[1];
			params.put(k, v);
		}
		int from = data.indexOf("<xml>");
		if (-1 != from) {
			from += 6;
			int to = 0;
			while (-1 != (to = data.indexOf(">", from))) {
				String k = data.substring(from, to);
				if (k.equals("/xml")) break;
				
				from = to + 1;
				to = data.indexOf("</", from);
				if (-1 == to) {
					logger.error("invalid wechat message body: " + data);
					break;
				}
				String v = data.substring(from, to);
				if (v.startsWith("<![CDATA[")) v = v.substring(9, v.lastIndexOf("]]>"));
				params.put(k, v);
				
				from = data.indexOf("<", to + 2) + 1;
			}
		}
		return params;
	}
	
	private void processWechat(FjServer server, FjMsg msg) {
		Map<String, String> params = parseWechatParams(msg.toString());
		if (params.containsKey("echostr")) {
			logger.info("wechat access message");
			processWechatAccess(server, msg, params);
		} else {
			logger.info("wechat common message");
			processWechatCommon(server, msg, params);
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
	private void processWechatAccess(FjServer server, FjMsg msg, Map<String, String> params) {
		responseWechatRequest(server, msg, params.get("echostr"));
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
	 * @param params
	 */
	private void processWechatCommon(FjServer server, FjMsg msg, Map<String, String> params) {
		if (params.get("Content").equals("fuck!")) {
			responseWechatRequest(server, msg, createWechatResponseBody(params, "fuck you too!"));
		}
	}
	
	private static String createWechatResponseBody(Map<String, String> params, String content) {
		StringBuffer body = new StringBuffer();
		body.append("<xml>\r\n");
		body.append("<ToUserName><![CDATA[" + params.get("FromUserName") + "]]></ToUserName>\r\n");
		body.append("<FromUserName><![CDATA[" + params.get("ToUserName") + "]]></FromUserName>\r\n");
		body.append("<CreateTime>" + (System.currentTimeMillis() / 1000) + "</CreateTime>\r\n");
		body.append("<MsgType><![CDATA[text]]></MsgType>\r\n");
		body.append("<Content><![CDATA[" + content + "]]></Content>\r\n");
		body.append("</xml>");
		return body.toString();
	}
	
	private static void responseWechatRequest(FjServer server, FjMsg msg, String body) {
		StringBuffer rsp = new StringBuffer();
		rsp.append("HTTP/1.1 200 OK\r\n");
		rsp.append("Server: fomjar\r\n");
		rsp.append("Date: " + new Date() + "\r\n");
		rsp.append("Connection: Keep-Alive\r\n");
		rsp.append("Content-Length: " + body.length() + "\r\n");
		rsp.append("Content-Type: text/xml; charset=UTF-8\r\n");
		rsp.append("\r\n");
		if (null != body) rsp.append(body);
		FjToolkit.getSender(server.name()).send(FjMsg.create(rsp.toString()), server.mq().pollConnection(msg));
		logger.info("response wechat message: " + rsp.toString());
	}
}
