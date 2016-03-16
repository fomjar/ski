package com.ski.wca;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.wca.monitor.TokenMonitor;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjSender;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjJsonMessage;

public class WechatInterface {
    
    private static final Logger logger = Logger.getLogger(WechatInterface.class);
    
    public static class WechatInterfaceException extends Exception {
        
        private static final long serialVersionUID = 3844740117616582893L;

        public WechatInterfaceException() {
            super();
        }

        public WechatInterfaceException(String message, Throwable cause,
                boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        public WechatInterfaceException(String message, Throwable cause) {
            super(message, cause);
        }

        public WechatInterfaceException(String message) {
            super(message);
        }

        public WechatInterfaceException(Throwable cause) {
            super(cause);
        }
        
    }
    
    public static class WechatPermissionDeniedException extends WechatInterfaceException {

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
    
    public static class WechatCustomServiceException extends WechatInterfaceException {

        private static final long serialVersionUID = -8302791326146427719L;

        public WechatCustomServiceException() {
            super();
        }

        public WechatCustomServiceException(String message,
                Throwable cause, boolean enableSuppression,
                boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        public WechatCustomServiceException(String message,
                Throwable cause) {
            super(message, cause);
        }

        public WechatCustomServiceException(String message) {
            super(message);
        }

        public WechatCustomServiceException(Throwable cause) {
            super(cause);
        }
        
    }
    
    private static String host = "api.weixin.qq.com";
    
    public static void setHost(String host) {
        logger.info(String.format("host has changed from %s to %s", WechatInterface.host, host));
        WechatInterface.host = host;
    }
    
    public static String host() {
        return host;
    }
    
    private static void checkWechatPermission() throws WechatPermissionDeniedException {
        if (null == TokenMonitor.getInstance().token()) throw new WechatPermissionDeniedException("havn't got access token yet");
    }
    
    private static void checkWechatCustomService() throws WechatPermissionDeniedException, WechatCustomServiceException {
        FjJsonMessage rsp = customServiceGet();
        if (!rsp.json().containsKey("kf_list"))  logger.warn("custom service maybe unavailable");
        if (0 == rsp.json().getJSONArray("kf_list").size()) throw new WechatCustomServiceException("custom service account not found");
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
        sendResponse(((FjHttpRequest) wrapper.message()).urlParameters().get("echostr"), (SocketChannel) wrapper.attachment("conn"));
    }
    
    public static FjJsonMessage token(String appid, String secret) {
        String url = String.format("https://%s/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", host(), appid, secret);
        return sendRequest("GET", url);
    }
    
    public static FjJsonMessage menuCreate(String menu) throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/cgi-bin/menu/create?access_token=%s", host(), TokenMonitor.getInstance().token());
        return sendRequest("POST", url, menu);
    }
    
    public static FjJsonMessage menuDelete() throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/cgi-bin/menu/delete?access_token=%s", host(), TokenMonitor.getInstance().token());
        return sendRequest("GET", url);
    }
    
    public static FjJsonMessage customServiceAdd(String kfaccount) throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/customservice/kfaccount/add?access_token=%s", host(), TokenMonitor.getInstance().token());
        return sendRequest("POST", url, kfaccount);
    }
    
    public static FjJsonMessage customServiceUpdate(String kfaccount) throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/customservice/kfaccount/update?access_token=%s", host(), TokenMonitor.getInstance().token());
        return sendRequest("POST", url, kfaccount);
    }
    
    public static FjJsonMessage customServiceDel(String kfaccount) throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/customservice/kfaccount/del?access_token=%s", host(), TokenMonitor.getInstance().token());
        return sendRequest("GET", url, kfaccount);
    }
    
    public static FjJsonMessage customServiceGet() throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/cgi-bin/customservice/getkflist?access_token=%s", host(), TokenMonitor.getInstance().token());
        return sendRequest("GET", url);
    }
    
    private static final String TEMPLATE_CUSTOM_TEXT_MESSAGE =
              "{\r\n"
            + "    \"touser\":\"%s\",\r\n"
            + "    \"msgtype\":\"text\",\r\n"
            + "    \"text\":\r\n"
            + "    {\r\n"
            + "         \"content\":\"%s\"\r\n"
            + "    }\r\n"
            + "}";
    public static FjJsonMessage customSendTextMessage(String user_to, String content) throws WechatPermissionDeniedException, WechatCustomServiceException {
        checkWechatPermission();
        checkWechatCustomService();
        String url = String.format("https://%s/cgi-bin/message/custom/send?access_token=%s", host(), TokenMonitor.getInstance().token());
        String msg_content = String.format(TEMPLATE_CUSTOM_TEXT_MESSAGE, user_to, content);
        return sendRequest("POST", url, msg_content);
    }
    
    public static FjJsonMessage sendRequest(String method, String url) {
        return sendRequest(method, url, null);
    }
    
    public static FjJsonMessage sendRequest(String method, String url, String content) {
        return (FjJsonMessage) FjSender.sendHttpRequest(new FjHttpRequest(method, url, content));
    }
    
    public static void sendResponse(String content, SocketChannel conn) {
        FjSender.sendHttpResponse(new FjHttpResponse(content), conn);
    }
        
    public static void sendXmlResponse(String user_from, String user_to, String content, SocketChannel conn) {
        sendResponse(WechatInterface.createXmlMessage(user_from, user_to, content), conn);
    }
    
    private static final String TEMPLATE_XML_MESSAGE =
              "<xml>\r\n"
            + "    <FromUserName><![CDATA[%s]]></FromUserName>\r\n"
            + "    <ToUserName><![CDATA[%s]]></ToUserName>\r\n"
            + "    <CreateTime>%d</CreateTime>\r\n"
            + "    <MsgType><![CDATA[text]]></MsgType>\r\n"
            + "    <Content><![CDATA[%s]]></Content>\r\n"
            + "</xml>";    
    private static String createXmlMessage(String user_from, String user_to, String content) {
        return String.format(TEMPLATE_XML_MESSAGE, user_from, user_to, System.currentTimeMillis() / 1000, content);
    }
    
}
