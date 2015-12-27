package com.ski.wca;

import java.nio.channels.SocketChannel;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjJsonMessage;

public class WechatInterface {
	
	// private static final Logger logger = Logger.getLogger(WechatInterface.class);
	
	/**
	 * access message demo:
	 * <pre>
	 * GET /wechat?signature=47e8091f63a0d2042a94b373ab8374aa2df012a7&echostr=7133327084041532349&timestamp=1448367037&nonce=1572789203 HTTP/1.0
	 * User-Agent: Mozilla/4.0
	 * Accept: *\/*
	 * Host: 120.55.195.12
	 * Pragma: no-cache
	 * Connection: Keep-Alive
	 * </pre>
	 * 
	 * @param serverName
	 * @param wrapper
	 */
	public static void access(String serverName, FjMessageWrapper wrapper) {
		response(serverName, (SocketChannel) wrapper.attachment("conn"), "text/plain", ((FjHttpRequest) wrapper.message()).titleParam("echostr"));
	}
	
	private static final String URL_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	public static FjJsonMessage token(String serverName, String appid, String secret) {
		String url = String.format(URL_TOKEN, appid, secret);
		return (FjJsonMessage) FjServerToolkit.getSender(serverName).sendHttpRequest("GET", url, null);
	}
	
	private static final String URL_MENU_CREATE = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s";
	public static FjJsonMessage menuCreate(String serverName) {
		String url = String.format(URL_MENU_CREATE, TokenGuard.getInstance().getToken());
		String content = "{'button':[{'type':'click', 'name':'Test1', 'key':'test1'},{'type':'click', 'name':'Test2', 'key':'test2'},{'type':'click', 'name':'Test3', 'key':'test3'}]}";
		content = content.replace("'", "\"");
		return (FjJsonMessage) FjServerToolkit.getSender(serverName).sendHttpRequest("POST", url, content);
	}
	
	private static final String URL_MENU_DELETE = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=%s";
	public static FjJsonMessage menuDelete(String serverName) {
		String url = String.format(URL_MENU_DELETE, TokenGuard.getInstance().getToken());
		return (FjJsonMessage) FjServerToolkit.getSender(serverName).sendHttpRequest("GET", url, null);
	}
	
	private static final String TEMPLATE_MESSAGE = "<xml>\r\n"
			+ "<ToUserName><![CDATA[%s]]></ToUserName>\r\n"
			+ "<FromUserName><![CDATA[%s]]></FromUserName>\r\n"
			+ "<CreateTime>%d</CreateTime>\r\n"
			+ "<MsgType><![CDATA[%s]]></MsgType>\r\n"
			+ "<Content><![CDATA[%s]]></Content>\r\n"
			+ "</xml>";	
	
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
	 * @param serverName
	 * @param wrapper
	 * @param to
	 * @param from
	 * @param messageType
	 * @param message
	 */
	public static void responseMessage(String serverName, SocketChannel conn, String to, String from, String messageType, String message) {
		String content = String.format(TEMPLATE_MESSAGE, to, from, System.currentTimeMillis() / 1000, messageType, message);
		response(serverName, conn, "text/xml", content);
	}
	
	public static void response(String serverName, SocketChannel conn, String contentType, String content) {
		FjServerToolkit.getSender(serverName).send(new FjMessageWrapper(new FjHttpResponse(contentType, content)).attach("conn", conn));
	}

}
