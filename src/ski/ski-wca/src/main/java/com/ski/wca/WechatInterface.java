package com.ski.wca;

import java.awt.Color;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ski.common.CommonDefinition;
import com.ski.wca.monitor.TokenMonitor;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjSender;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjJsonMessage;
import fomjar.server.msg.FjXmlMessage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
        FjJsonMessage rsp = customserviceGet();
        if (!rsp.json().containsKey("kf_list")) logger.warn("custom service maybe unavailable");
        else if (0 == rsp.json().getJSONArray("kf_list").size()) throw new WechatCustomServiceException("custom service account not found");
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
        sendResponse(FjHttpRequest.CT_TEXT, ((FjHttpRequest) wrapper.message()).urlArgs().get("echostr"), (SocketChannel) wrapper.attachment("conn"));
    }
    
    public static FjJsonMessage token(String appid, String secret) {
        String url = String.format("https://%s/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", host(), appid, secret);
        return sendRequest("GET", url);
    }
    
    public static FjJsonMessage ticket() throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/cgi-bin/ticket/getticket?access_token=%s&type=jsapi", host(), TokenMonitor.getInstance().token());
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
    
    public static FjJsonMessage customserviceAdd(String kfaccount) throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/customservice/kfaccount/add?access_token=%s", host(), TokenMonitor.getInstance().token());
        return sendRequest("POST", url, kfaccount);
    }
    
    public static FjJsonMessage customserviceUpdate(String kfaccount) throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/customservice/kfaccount/update?access_token=%s", host(), TokenMonitor.getInstance().token());
        return sendRequest("POST", url, kfaccount);
    }
    
    public static FjJsonMessage customserviceDel(String kfaccount) throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/customservice/kfaccount/del?access_token=%s", host(), TokenMonitor.getInstance().token());
        return sendRequest("GET", url, kfaccount);
    }
    
    public static FjJsonMessage customserviceGet() throws WechatPermissionDeniedException {
        checkWechatPermission();
        String url = String.format("https://%s/cgi-bin/customservice/getkflist?access_token=%s", host(), TokenMonitor.getInstance().token());
        return sendRequest("GET", url);
    }
    
    public static FjDscpMessage customConvertRequest(String server, FjHttpRequest request) {
        Element xml      = request.contentToXml().getDocumentElement();
        String user_from = xml.getElementsByTagName("FromUserName").item(0).getTextContent().trim();
        // String user_to   = xml.getElementsByTagName("ToUserName").item(0).getTextContent().trim();
        
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",    "wechat");
        req.json().put("ts",    server);
        req.json().put("sid",   user_from);
        
        JSONObject args = new JSONObject();
        args.put("user", user_from);
        
        String content   = null;
        String event     = null;
        String event_key = null;
        String msg_type  = xml.getElementsByTagName("MsgType").item(0).getTextContent().trim();
        switch (msg_type) {
        case "text": {
            content = xml.getElementsByTagName("Content").item(0).getTextContent().trim();
            logger.info("INST_USER_REQUEST     - wechat:" + user_from + ":" + content);
            req.json().put("inst", CommonDefinition.ISIS.INST_USER_REQUEST);
            args.put("content", content);
            break;
        }
        case "event": {
            event     = xml.getElementsByTagName("Event").item(0).getTextContent().trim();
            if (event.equals("TEMPLATESENDJOBFINISH")) {    // 模板消息结果通知
                req.json().put("inst", CommonDefinition.ISIS.INST_USER_NOTIFY);
                args.put("content", event + " " + xml.getElementsByTagName("Status").item(0).getTextContent().trim());
                break;
            }
            event_key = xml.getElementsByTagName("EventKey").item(0).getTextContent().trim();
            switch (event) {
            case "subscribe": {
                logger.info("INST_USER_SUBSCRIBE   - wechat:" + user_from);
                req.json().put("inst", CommonDefinition.ISIS.INST_USER_SUBSCRIBE);
                break;
            }
            case "unsubscribe": {
                logger.info("INST_USER_UNSUBSCRIBE - wechat:" + user_from);
                req.json().put("inst", CommonDefinition.ISIS.INST_USER_UNSUBSCRIBE);
                break;
            }
            case "CLICK": {
                logger.info("INST_USER_COMMAND     - wechat:" + user_from + ":" + event_key);
                req.json().put("inst", CommonDefinition.ISIS.INST_USER_COMMAND);
                args.put("content", event_key);
                break;
            }
            case "VIEW": {
                logger.info("INST_USER_GOTO        - wechat:" + user_from);
                req.json().put("inst", CommonDefinition.ISIS.INST_USER_GOTO);
                args.put("content", event_key);
                break;
            }
            default:
                logger.error("unknown event: " + event);
                break;
            }
            break;
        }
        case "location": {
            logger.info("INST_USER_LOCATION    - wechat:" + user_from);
            float  x     = Float.parseFloat(xml.getElementsByTagName("Location_X").item(0).getTextContent());
            float  y     = Float.parseFloat(xml.getElementsByTagName("Location_Y").item(0).getTextContent());
            int    scale = Integer.parseInt(xml.getElementsByTagName("Scale").item(0).getTextContent());
            String label = xml.getElementsByTagName("Label").item(0).getTextContent();
            req.json().put("inst", CommonDefinition.ISIS.INST_USER_LOCATION);
            args.put("content", JSONObject.fromObject(String.format("{'x':%f, 'y':%f, 'scale':%d, 'label':\"%s\"}", x, y, scale, label)));
            break;
        }
        case "image": {
            /**
             * <xml><ToUserName><![CDATA[gh_8b1e54d8e5df]]></ToUserName>
             * <FromUserName><![CDATA[oRojEwPTK3o2cYrLsXuuX-FuypBM]]></FromUserName>
             * <CreateTime>1451406148</CreateTime>
             * <MsgType><![CDATA[image]]></MsgType>
             * <PicUrl><![CDATA[http://mmbiz.qpic.cn/mmbiz/mOh7Zj68sT6BagSkm5VVdSY19Zqn2W32uAaJzADL46bzheBEXUKUaX0H2tsRLe2WXtSsj7tgSUj5wkDlkuuZBA/0]]></PicUrl>
             * <MsgId>6233741939275317091</MsgId>
             * <MediaId><![CDATA[m0136L5dVlQckJJUHFOSc1ZW757ZuVBhZAvvBr1kXV8DwBW3t-w7l3a8i4btf5yO]]></MediaId>
             * </xml>
             */
            logger.info("INST_USER_IMAGE       - wechat:" + user_from);
            break;
        }
        case "voice": {
            /**
             * <xml><ToUserName><![CDATA[gh_8b1e54d8e5df]]></ToUserName>
             * <FromUserName><![CDATA[oRojEwPTK3o2cYrLsXuuX-FuypBM]]></FromUserName>
             * <CreateTime>1451406497</CreateTime>
             * <MsgType><![CDATA[voice]]></MsgType>
             * <MediaId><![CDATA[6IJxKBt0aUcsKSxTD9MHX9WV1sYnFkf0lAArZWWm_YZVRnF2OWGcQdvXOxcBwdom]]></MediaId>
             * <Format><![CDATA[amr]]></Format>
             * <MsgId>6233743438218903488</MsgId>
             * <Recognition><![CDATA[]]></Recognition>
             * </xml>
             */
            logger.info("INST_USER_VOICE       - wechat:" + user_from);
            break;
        }
        case "shortvideo": {
            /**
             * <xml><ToUserName><![CDATA[gh_8b1e54d8e5df]]></ToUserName>
             * <FromUserName><![CDATA[oRojEwPTK3o2cYrLsXuuX-FuypBM]]></FromUserName>
             * <CreateTime>1451406218</CreateTime>
             * <MsgType><![CDATA[shortvideo]]></MsgType>
             * <MediaId><![CDATA[UtzXqtCNJXMMzT7Wm3uCxB0xoYyXosqueheO05qaERJTMmOKQbBPQT3Pt9xVyC-4]]></MediaId>
             * <ThumbMediaId><![CDATA[NKm_Nl1889sucsIhojyebk9W-dcxPXjV4xPwObm2RgvoGgH21y6-X653AdZYFQc3]]></ThumbMediaId>
             * <MsgId>6233742239923027843</MsgId>
             * </xml>
             */
            logger.info("INST_USER_SHORTVIDEO  - wechat:" + user_from);
            break;
        }
        default:
            logger.error("unknown msg type: " + msg_type);
            break;
        }
        req.json().put("args", args);
        return req;
    }
    
    public static FjJsonMessage messageCustomSendText(String user, String content) throws WechatPermissionDeniedException, WechatCustomServiceException {
        checkWechatPermission();
        checkWechatCustomService();
        
        String url = String.format("https://%s/cgi-bin/message/custom/send?access_token=%s", host(), TokenMonitor.getInstance().token());
        
        JSONObject text = new JSONObject();
        text.put("content", content);
        JSONObject msg = new JSONObject();
        msg.put("touser", user);
        msg.put("msgtype", "text");
        msg.put("text", text);
        return sendRequest("POST", url, msg.toString());
    }
    
    public static FjJsonMessage messageCustomSendNews(String user, Article... article) throws WechatPermissionDeniedException, WechatCustomServiceException {
        checkWechatPermission();
        checkWechatCustomService();
        
        String url = String.format("https://%s/cgi-bin/message/custom/send?access_token=%s", host(), TokenMonitor.getInstance().token());
        
        JSONArray articles = new JSONArray();
        for (Article a : article) articles.add(a.toString());
        JSONObject news = new JSONObject();
        news.put("articles", articles);
        JSONObject msg = new JSONObject();
        msg.put("touser", user);
        msg.put("msgtype", "news");
        msg.put("news", news);
        return sendRequest("POST", url, msg.toString());
    }
    
    private static String TEMPLATE_COLOR = "#173177";
    
    public static void messageTemplateColor(Color color) {
        TEMPLATE_COLOR = String.format("#%x%x%x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    public static FjMessage messageTemplateSend(String user, String template, String url, Map<String, String> data) throws WechatPermissionDeniedException {
        checkWechatPermission();
        
        String _url = String.format("https://%s/cgi-bin/message/template/send?access_token=%s", host(), TokenMonitor.getInstance().token());
        
        JSONObject datas = new JSONObject();
        data.entrySet().forEach(entry->{
            JSONObject v = new JSONObject();
            v.put("value", entry.getValue());
            v.put("color", TEMPLATE_COLOR);
            datas.put(entry.getKey(), v);
        });
        
        JSONObject msg = new JSONObject();
        msg.put("touser", user);
        msg.put("template_id", template);
        msg.put("url", url);
        msg.put("data", datas);
        
        return sendRequest("POST", _url, msg.toString());
    }
    
    public static FjJsonMessage userInfo(String user) throws WechatPermissionDeniedException {
        checkWechatPermission();
        
        String url = String.format("https://%s/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN", host(), TokenMonitor.getInstance().token(), user);
        return sendRequest("GET", url);
    }
    
    public static FjJsonMessage sendRequest(String method, String url) {
        return sendRequest(method, url, null);
    }
    
    public static FjJsonMessage sendRequest(String method, String url, String content) {
        logger.debug(">> " + (null != content ? content.replace("\r\n", "") : null));
        FjJsonMessage rsp = (FjJsonMessage) FjSender.sendHttpRequest(new FjHttpRequest(method, url, FjHttpRequest.CT_JSON, content));
        logger.debug("<< " + rsp);
        return rsp;
    }
    
    public static void sendResponse(String contentType, String content, SocketChannel conn) {
        FjSender.sendHttpResponse(new FjHttpResponse(contentType, content), conn);
    }
    
    public static class Article {
        
        private String title;
        private String description;
        private String url;
        private String picurl;
        
        public Article(String title, String description, String url, String picurl) {
            this.title = title;
            this.description = description;
            this.url = url;
            this.picurl = picurl;
        }
        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            json.put("title", title);
            json.put("description", description);
            json.put("url", url);
            json.put("picurl", picurl);
            return json.toString();
        }
    }
    
    private static final String MSG_PAY = "<xml>"
            + "<appid>%s</appid>"                           // 公众号APPID
            + "<mch_id>%s</mch_id>"                         // 微信支付分配的商户ID
            + "<device_info>%s</device_info>"               // 终端设备号(门店号或收银设备ID)，注意：PC网页或公众号内支付请传"WEB"
            + "<nonce_str>%s</nonce_str>"                   // 随机字符串不长于32位
            + "<sign>%s</sign>"                             // 签名
            + "<body>%s</body>"                             // 商品描述：title-name
            + "<attach>%s</attach>"                         // 附加字段，查询结果时显示
            + "<out_trade_no>%s</out_trade_no>"             // 商户订单号
            + "<total_fee>%d</total_fee>"                   // 交易总金额，单位：分
            + "<spbill_create_ip>%s</spbill_create_ip>"     // 本地IP
            + "<notify_url>%s</notify_url>"                 // 回调地址，不带参数
            + "<trade_type>%s</trade_type>"                 // 交易类型
            + "<openid>%s</openid>"                         // jsapi必须传
            + "</xml>";
    
    public static final String TRADE_TYPE_JSAPI     = "JSAPI";  // 公众号
    public static final String TRADE_TYPE_NATIVE    = "NATIVE"; // 原生扫码
    public static final String TRADE_TYPE_APP       = "APP";    // APP
    
    public static FjXmlMessage prepay(String body, String attach, int money, String host, String notify_url, String user) {
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        String msg_pay = String.format(MSG_PAY,
                FjServerToolkit.getServerConfig("wca.appid"),
                FjServerToolkit.getServerConfig("wca.mch.id"),
                "WEB",
                Long.toHexString(System.currentTimeMillis()) + Long.toHexString(System.nanoTime()),
                "%s", // sign
                body,
                attach,
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + String.valueOf(System.currentTimeMillis()).substring(10),
                money,
                host,
                notify_url,
                TRADE_TYPE_JSAPI,
                user);
        String sign = createSignature4Pay(new FjXmlMessage(msg_pay).xml());
        msg_pay = String.format(msg_pay, sign);
        logger.debug("prepay request: " + msg_pay);
        FjXmlMessage rsp = (FjXmlMessage) FjSender.sendHttpRequest(new FjHttpRequest("POST", url, FjHttpRequest.CT_XML, msg_pay));
        logger.debug("prepay response: " + rsp);
        return rsp;
    }
    
    public static String createSignature4Config(String nonceStr, long timestamp, String url) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("nonceStr",     nonceStr);
        map.put("jsapi_ticket", TokenMonitor.getInstance().ticket());
        map.put("timestamp",    String.valueOf(timestamp));
        map.put("url",          url);
        List<String> keys = new LinkedList<String>(map.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        keys.forEach(key->{
            if (0 < sb.length()) sb.append("&");
            sb.append(key);
            sb.append("=");
            sb.append(map.get(key));
        });
        StringBuilder result = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("sha-1");
            digest.update(sb.toString().getBytes());
            byte[] digestbyte = digest.digest();
            for (byte db : digestbyte) {
                String dbh = Integer.toHexString(db & 0xFF);
                if (dbh.length() < 2) result.append("0");
                result.append(dbh);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {logger.error("create signature by sha-1 failed", e);}
        return "";
    }
    
    public static String createSignature4Pay(Document xml) {
        Map<String, String> map = new HashMap<String, String>();
        NodeList nodes = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            
            if (null == node.getFirstChild()) continue;
            String name     = node.getNodeName();
            String value    = node.getFirstChild().getNodeValue();
            if (name.equals("sign")) continue;
            if (null == value || 0 == value.length()) continue;
            
            map.put(name, value);
        }
        return createSignature4Pay(map);
    }
    
    public static String createSignature4Pay(Map<String, String> map) {
        List<String> keys = new LinkedList<String>(map.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        keys.forEach(key->{
            if (0 < sb.length()) sb.append("&");
            sb.append(key);
            sb.append("=");
            sb.append(map.get(key));
        });
        sb.append("&key=" + FjServerToolkit.getServerConfig("wca.mch.key"));
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(sb.toString().getBytes());
            byte [] digestbyte = digest.digest();
            StringBuilder result = new StringBuilder();
            for (byte db : digestbyte) {
                String dbh = Integer.toHexString(db & 0xFF);
                if (dbh.length() < 2) result.append("0");
                result.append(dbh);
            }
            return result.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {logger.error("create signature by md5 failed", e);}
        return "";
    }

}
