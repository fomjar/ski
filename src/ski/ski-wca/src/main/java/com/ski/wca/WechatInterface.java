package com.ski.wca;

import java.nio.channels.SocketChannel;

import com.ski.wca.guard.TokenGuard;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjSender;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjJsonMessage;

public class WechatInterface {
	
	// private static final Logger logger = Logger.getLogger(WechatInterface.class);
	
	public static class WechatPermissionDeniedException extends Exception {

		private static final long serialVersionUID = 6641226267444874372L;

		public WechatPermissionDeniedException() {
			super();
		}

		public WechatPermissionDeniedException(String message, Throwable cause,
				boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public WechatPermissionDeniedException(String message, Throwable cause) {
			super(message, cause);
		}

		public WechatPermissionDeniedException(String message) {
			super(message);
		}

		public WechatPermissionDeniedException(Throwable cause) {
			super(cause);
		}
		
	}
	
	private static void checkWechatPermission() throws WechatPermissionDeniedException {
		if (null == TokenGuard.getInstance().token()) throw new WechatPermissionDeniedException("havn't got access token yet");
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
	 * </pre>
	 * 
	 * @param serverName
	 * @param wrapper
	 */
	public static void access(FjMessageWrapper wrapper) {
		FjSender.sendHttpResponse(new FjHttpResponse(((FjHttpRequest) wrapper.message()).urlParameters().get("echostr")), (SocketChannel) wrapper.attachment("conn"));
	}
	
	private static final String URL_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	public static FjJsonMessage token(String appid, String secret) {
		String url = String.format(URL_TOKEN, appid, secret);
		return sendRequest("GET", url);
	}
	
	private static final String URL_MENU_CREATE = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s";
	public static FjJsonMessage menuCreate(String menu) throws WechatPermissionDeniedException {
		checkWechatPermission();
		String url = String.format(URL_MENU_CREATE, TokenGuard.getInstance().token());
		return sendRequest("POST", url, menu.replace("'", "\""));
	}
	
	private static final String URL_MENU_DELETE = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=%s";
	public static FjJsonMessage menuDelete(String serverName) throws WechatPermissionDeniedException {
		checkWechatPermission();
		String url = String.format(URL_MENU_DELETE, TokenGuard.getInstance().token());
		return sendRequest("GET", url);
	}
	
	public static FjJsonMessage sendRequest(String method, String url) {
		return sendRequest(method, url, null);
	}
	
	public static FjJsonMessage sendRequest(String method, String url, String content) {
		return (FjJsonMessage) FjSender.sendHttpRequest(new FjHttpRequest(method, url, content));
	}
	
	public static void sendResponse(String user_from, String user_to, SocketChannel conn) {
		FjSender.sendHttpResponse(new FjHttpResponse(WechatInterface.createTextMessage(user_from, user_to, null)), conn);
	}
	
	public static void sendResponse(String user_from, String user_to, String content, SocketChannel conn) {
		FjSender.sendHttpResponse(new FjHttpResponse(WechatInterface.createTextMessage(user_from, user_to, content)), conn);
	}
	
	private static final String TEMPLATE_TEXT_MESSAGE = "<xml>\r\n"
			+ "<FromUserName><![CDATA[%s]]></FromUserName>\r\n"
			+ "<ToUserName><![CDATA[%s]]></ToUserName>\r\n"
			+ "<CreateTime>%d</CreateTime>\r\n"
			+ "<MsgType><![CDATA[text]]></MsgType>\r\n"
			+ "<Content><![CDATA[%s]]></Content>\r\n"
			+ "</xml>";	
	private static String createTextMessage(String user_from, String user_to, String content) {
		if (null == content) content = "";
		return String.format(TEMPLATE_TEXT_MESSAGE, user_from, user_to, System.currentTimeMillis() / 1000, content);
	}
	
}
