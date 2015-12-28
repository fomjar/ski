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
	
	public static class WechatAuthorityException extends Exception {

		private static final long serialVersionUID = 6641226267444874372L;

		public WechatAuthorityException() {
			super();
		}

		public WechatAuthorityException(String message, Throwable cause,
				boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public WechatAuthorityException(String message, Throwable cause) {
			super(message, cause);
		}

		public WechatAuthorityException(String message) {
			super(message);
		}

		public WechatAuthorityException(Throwable cause) {
			super(cause);
		}
		
	}
	
	private static void checkAuthority() throws WechatAuthorityException {
		if (null == TokenGuard.getInstance().token()) throw new WechatAuthorityException("have no access token");
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
		return (FjJsonMessage) FjSender.sendHttpRequest(new FjHttpRequest("GET", url, null));
	}
	
	private static final String URL_MENU_CREATE = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s";
	public static FjJsonMessage menuCreate(String menu) throws WechatAuthorityException {
		checkAuthority();
		String url = String.format(URL_MENU_CREATE, TokenGuard.getInstance().token());
		return (FjJsonMessage) FjSender.sendHttpRequest(new FjHttpRequest("POST", url, menu.replace("'", "\"")));
	}
	
	private static final String URL_MENU_DELETE = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=%s";
	public static FjJsonMessage menuDelete(String serverName) throws WechatAuthorityException {
		checkAuthority();
		String url = String.format(URL_MENU_DELETE, TokenGuard.getInstance().token());
		return (FjJsonMessage) FjSender.sendHttpRequest(new FjHttpRequest("GET", url, null));
	}
	
	
	private static final String TEMPLATE_MESSAGE = "<xml>\r\n"
			+ "<ToUserName><![CDATA[%s]]></ToUserName>\r\n"
			+ "<FromUserName><![CDATA[%s]]></FromUserName>\r\n"
			+ "<CreateTime>%d</CreateTime>\r\n"
			+ "<MsgType><![CDATA[%s]]></MsgType>\r\n"
			+ "<Content><![CDATA[%s]]></Content>\r\n"
			+ "</xml>";	
	public static String createMessage(String to, String from, String type, String content) {
		return String.format(TEMPLATE_MESSAGE, to, from, System.currentTimeMillis() / 1000, type, content);
	}

}