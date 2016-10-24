package com.ski.vcg.web.wechat;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ski.vcg.common.CommonDefinition;

import fomjar.server.FjMessage;
import fomjar.server.FjSender;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjJsonMessage;
import fomjar.server.msg.FjXmlMessage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@SuppressWarnings("deprecation")
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

    private static void checkWechatCustomService(String token) throws WechatCustomServiceException {
        FjJsonMessage rsp = customserviceGet(token);
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
    public static void access(SocketChannel conn, FjHttpRequest request) {
        FjHttpResponse response = new FjHttpResponse(null, 200, null, request.urlArgs().get("echostr"));
        sendResponse(response, conn);
    }

    public static FjJsonMessage token(String appid, String secret) {
        String url = String.format("https://%s/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", host(), appid, secret);
        return (FjJsonMessage) sendRequest("GET", url);
    }

    public static FjJsonMessage ticket(String token) {
        String url = String.format("https://%s/cgi-bin/ticket/getticket?access_token=%s&type=jsapi", host(), token);
        return (FjJsonMessage) sendRequest("GET", url);
    }

    public static byte[] media(String token, String media_id) {
        String url = String.format("https://%s/cgi-bin/media/get?access_token=%s&media_id=%s", host(), token, media_id);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            InputStream is = new URL(url).openStream();
            byte[] buf = new byte[1024];
            int len = -1;
            while (0 < (len = is.read(buf))) baos.write(buf, 0, len);
            is.close();
            return baos.toByteArray();
        } catch (IOException e) {e.printStackTrace();}
        return null;
    }

    public static FjJsonMessage menuCreate(String token, String menu) {
        String url = String.format("https://%s/cgi-bin/menu/create?access_token=%s", host(), token);
        return (FjJsonMessage) sendRequest("POST", url, menu);
    }

    public static FjJsonMessage menuDelete(String token) {
        String url = String.format("https://%s/cgi-bin/menu/delete?access_token=%s", host(), token);
        return (FjJsonMessage) sendRequest("GET", url);
    }

    public static FjJsonMessage customserviceAdd(String token, String kfaccount) {
        String url = String.format("https://%s/customservice/kfaccount/add?access_token=%s", host(), token);
        return (FjJsonMessage) sendRequest("POST", url, kfaccount);
    }

    public static FjJsonMessage customserviceUpdate(String token, String kfaccount) {
        String url = String.format("https://%s/customservice/kfaccount/update?access_token=%s", host(), token);
        return (FjJsonMessage) sendRequest("POST", url, kfaccount);
    }

    public static FjJsonMessage customserviceDel(String token, String kfaccount) {
        String url = String.format("https://%s/customservice/kfaccount/del?access_token=%s", host(), token);
        return (FjJsonMessage) sendRequest("GET", url, kfaccount);
    }

    public static FjJsonMessage customserviceGet(String token) {
        String url = String.format("https://%s/cgi-bin/customservice/getkflist?access_token=%s", host(), token);
        return (FjJsonMessage) sendRequest("GET", url);
    }

    public static FjDscpMessage customConvertRequest(FjHttpRequest request) {
        Element xml      = request.contentToXml().getDocumentElement();
        String user_from = xml.getElementsByTagName("FromUserName").item(0).getTextContent().trim();
        // String user_to   = xml.getElementsByTagName("ToUserName").item(0).getTextContent().trim();

        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",    "wechat");
        req.json().put("ts",    FjServerToolkit.getAnyServer().name());
        req.json().put("sid",   user_from);

        JSONObject args = new JSONObject();
        args.put("user", user_from);

        String event     = null;
        String event_key = null;
        String msg_type  = xml.getElementsByTagName("MsgType").item(0).getTextContent().trim();
        switch (msg_type) {
        case "text": {
            String content = xml.getElementsByTagName("Content").item(0).getTextContent().trim();
            logger.info("INST_USER_REQUEST     - wechat:" + user_from + ":" + content);
            req.json().put("inst", CommonDefinition.ISIS.INST_USER_REQUEST);
            args.put("content", content);
            break;
        }
        case "event": {
            event     = xml.getElementsByTagName("Event").item(0).getTextContent().trim();
            if (event.equals("TEMPLATESENDJOBFINISH")) {    // 模板消息结果通知
                req.json().put("inst", CommonDefinition.ISIS.INST_USER_NOTIFY);
                args.put("status", event + " " + xml.getElementsByTagName("Status").item(0).getTextContent().trim());
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
                args.put("cmd", event_key);
                break;
            }
            case "VIEW": {
                logger.info("INST_USER_VIEW        - wechat:" + user_from);
                req.json().put("inst", CommonDefinition.ISIS.INST_USER_VIEW);
                args.put("view", event_key);
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
            args.put("location", JSONObject.fromObject(String.format("{'x':%f, 'y':%f, 'scale':%d, 'label':\"%s\"}", x, y, scale, label)));
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
            logger.error("unknown message type: " + msg_type);
            break;
        }
        req.json().put("args", args);
        return req;
    }

    public static FjJsonMessage messageCustomSendText(String token, String user, String content) throws WechatCustomServiceException {
        checkWechatCustomService(token);

        String url = String.format("https://%s/cgi-bin/message/custom/send?access_token=%s", host(), token);

        JSONObject text = new JSONObject();
        text.put("content", content);
        JSONObject msg = new JSONObject();
        msg.put("touser", user);
        msg.put("msgtype", "text");
        msg.put("text", text);
        return (FjJsonMessage) sendRequest("POST", url, msg.toString());
    }

    public static FjJsonMessage messageCustomSendNews(String token, String user, Article... article) throws WechatCustomServiceException {
        checkWechatCustomService(token);

        String url = String.format("https://%s/cgi-bin/message/custom/send?access_token=%s", host(), token);

        JSONArray articles = new JSONArray();
        for (Article a : article) articles.add(a.toString());
        JSONObject news = new JSONObject();
        news.put("articles", articles);
        JSONObject msg = new JSONObject();
        msg.put("touser", user);
        msg.put("msgtype", "news");
        msg.put("news", news);
        return (FjJsonMessage) sendRequest("POST", url, msg.toString());
    }

    private static String TEMPLATE_COLOR = "#173177";

    public static void messageTemplateColor(Color color) {
        TEMPLATE_COLOR = String.format("#%x%x%x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static FjMessage messageTemplateSend(String token, String user, String template, String url, Map<String, String> data) {
        String _url = String.format("https://%s/cgi-bin/message/template/send?access_token=%s", host(), token);

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

    /**
     * {
     * "subscribe": 1,
     * "openid": "o6_bmjrPTlm6_2sgVt7hMZOPfL2M",
     * "nickname": "Band",
     * "sex": 1,
     * "language": "zh_CN",
     * "city": "广州",
     * "province": "广东",
     * "country": "中国",
     * "headimgurl":  "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     * "subscribe_time": 1382694957,
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * "remark": "",
     * "groupid": 0,
     * "tagid_list":[128,2]
     * }
     *
     * @param token
     * @param user
     * @return
     */
    public static FjJsonMessage userInfo(String token, String user) {
        String url = String.format("https://%s/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN", host(), token, user);
        return (FjJsonMessage) sendRequest("GET", url);
    }

    /**
     * 微信公众号网页认证
     *
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE"
     * }
     *
     * @param appid
     * @param secret
     * @param code
     * @return
     */
    public static FjJsonMessage snsOauth2(String appid, String secret, String code) {
        String url = String.format("https://%s/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", host(), appid, secret, code);
        return (FjJsonMessage) sendRequest("GET", url);
    }

    /**
     * 微信小程序认证
     *
     * {
     * "openid": "OPENID",
     * "session_key": "SESSIONKEY"
     * "expires_in": 2592000
     * }
     *
     * @param appid
     * @param secret
     * @param code
     * @return
     */
    public static FjJsonMessage snsJscode2session(String appid, String secret, String code) {
        String url = String.format("https://%s/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code", host(), appid, secret, code);
        return (FjJsonMessage) sendRequest("GET", url);
    }

    public static FjMessage sendRequest(String method, String url) {
        return sendRequest(method, url, null);
    }

    public static FjMessage sendRequest(String method, String url, String content) {
        logger.debug(">> " + (null != content ? content.replace("\r\n", "") : null));
        FjMessage rsp = FjSender.sendHttpRequest(new FjHttpRequest(method, url, "application/json", content));
        logger.debug("<< " + rsp);
        return rsp;
    }

    public static void sendResponse(FjHttpResponse response, SocketChannel conn) {
        FjSender.sendHttpResponse(response, conn);
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

    /**
     * request:
     * <xml>
     * <appid>wx2421b1c4370ec43b</appid>
     * <attach>支付测试</attach>
     * <body>JSAPI支付测试</body>
     * <mch_id>10000100</mch_id>
     * <detail><![CDATA[{ "goods_detail":[ { "goods_id":"iphone6s_16G", "wxpay_goods_id":"1001", "goods_name":"iPhone6s 16G", "quantity":1, "price":528800, "goods_category":"123456", "body":"苹果手机" }, { "goods_id":"iphone6s_32G", "wxpay_goods_id":"1002", "goods_name":"iPhone6s 32G", "quantity":1, "price":608800, "goods_category":"123789", "body":"苹果手机" } ] }]]></detail>
     * <nonce_str>1add1a30ac87aa2db72f57a2375d8fec</nonce_str>
     * <notify_url>http://wxpay.weixin.qq.com/pub_v2/pay/notify.v2.php</notify_url>
     * <openid>oUpF8uMuAJO_M2pxb1Q9zNjWeS6o</openid>
     * <out_trade_no>1415659990</out_trade_no>
     * <spbill_create_ip>14.23.150.211</spbill_create_ip>
     * <total_fee>1</total_fee><trade_type>JSAPI</trade_type>
     * <sign>0CB01533B8C1EF103065174F50BCA001</sign>
     * </xml>
     *
     * response:
     * <xml>
     * <return_code><![CDATA[SUCCESS]]></return_code>
     * <return_msg><![CDATA[OK]]></return_msg>
     * <appid><![CDATA[wx2421b1c4370ec43b]]></appid>
     * <mch_id><![CDATA[10000100]]></mch_id>
     * <nonce_str><![CDATA[IITRi8Iabbblz1Jc]]></nonce_str>
     * <sign><![CDATA[7921E432F65EB8ED0CE9755F0E86D72F]]></sign>
     * <result_code><![CDATA[SUCCESS]]></result_code>
     * <prepay_id><![CDATA[wx201411101639507cbf6ffd8b0779950874]]></prepay_id>
     * <trade_type><![CDATA[JSAPI]]></trade_type>
     * </xml>
     *
     * @param body
     * @param attach
     * @param money
     * @param host
     * @param notify_url
     * @param user
     * @return
     */
    public static FjXmlMessage prepay(String body, String attach, int money, String host, String notify_url, String user) {
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        String msg_pay = String.format(MSG_PAY,
                FjServerToolkit.getServerConfig("web.wechat.appid"),
                FjServerToolkit.getServerConfig("web.wechat.mch.id"),
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
        FjXmlMessage rsp = (FjXmlMessage) FjSender.sendHttpRequest(new FjHttpRequest("POST", url, "text/xml", msg_pay));
        logger.debug("prepay response: " + rsp);
        return rsp;
    }

    private static final String MSG_REDPACK = "<xml>"
            + "<sign><![CDATA[%s]]></sign>"
            + "<mch_billno><![CDATA[%s]]></mch_billno>"
            + "<mch_id><![CDATA[%s]]></mch_id>"
            + "<wxappid><![CDATA[%s]]></wxappid>"
            + "<send_name><![CDATA[%s]]></send_name>"
            + "<re_openid><![CDATA[%s]]></re_openid>"
            + "<total_amount><![CDATA[%d]]></total_amount>"
            + "<total_num><![CDATA[1]]></total_num>"
            + "<wishing><![CDATA[%s]]></wishing>"
            + "<client_ip><![CDATA[%s]]></client_ip>"
            + "<act_name><![CDATA[%s]]></act_name>"
            + "<remark><![CDATA[%s]]></remark>"
            + "<nonce_str><![CDATA[%s]]></nonce_str>"
            + "</xml>";

    /**
     * request:
     * <xml>
     * <sign><![CDATA[E1EE61A91C8E90F299DE6AE075D60A2D]]></sign>
     * <mch_billno><![CDATA[0010010404201411170000046545]]></mch_billno>
     * <mch_id><![CDATA[888]]></mch_id>
     * <wxappid><![CDATA[wxcbda96de0b165486]]></wxappid>
     * <send_name><![CDATA[send_name]]></send_name>
     * <re_openid><![CDATA[onqOjjmM1tad-3ROpncN-yUfa6uI]]></re_openid>
     * <total_amount><![CDATA[200]]></total_amount>
     * <total_num><![CDATA[1]]></total_num>
     * <wishing><![CDATA[恭喜发财]]></wishing>
     * <client_ip><![CDATA[127.0.0.1]]></client_ip>
     * <act_name><![CDATA[新年红包]]></act_name>
     * <remark><![CDATA[新年红包]]></remark>
     * <nonce_str><![CDATA[50780e0cca98c8c8e814883e5caa672e]]></nonce_str>
     * </xml>
     *
     * response:
     * <xml>
     * <return_code><![CDATA[SUCCESS]]></return_code>
     * <return_msg><![CDATA[发放成功.]]></return_msg>
     * <result_code><![CDATA[SUCCESS]]></result_code>
     * <err_code><![CDATA[0]]></err_code>
     * <err_code_des><![CDATA[发放成功.]]></err_code_des>
     * <mch_billno><![CDATA[0010010404201411170000046545]]></mch_billno>
     * <mch_id>10010404</mch_id>
     * <wxappid><![CDATA[wx6fa7e3bab7e15415]]></wxappid>
     * <re_openid><![CDATA[onqOjjmM1tad-3ROpncN-yUfa6uI]]></re_openid>
     * <total_amount>1</total_amount>
     * <send_listid>100000000020150520314766074200</send_listid>
     * <send_time>20150520102602</send_time>
     * </xml>
     *
     * @return
     */
    public synchronized static FjXmlMessage sendredpack(String sendername, String user, float money, String wishing, String host, String activity, String remark) {
        if (money > 200.0f) return null;

        String url = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
        String nonce_str = Long.toHexString(System.currentTimeMillis());
        String msg_redpack = String.format(MSG_REDPACK,
                "%s", // sign
                String.format("%s%s%s", FjServerToolkit.getServerConfig("web.wechat.mch.id"), new SimpleDateFormat("yyyyMMdd").format(new Date()), String.valueOf(System.currentTimeMillis()).substring(0, 10)),
                FjServerToolkit.getServerConfig("web.wechat.mch.id"),
                FjServerToolkit.getServerConfig("web.wechat.appid"),
                sendername,
                user,
                (int) (money * 100),
                wishing,
                host,
                activity,
                remark,
                nonce_str);
        String sign = createSignature4Pay(new FjXmlMessage(msg_redpack).xml());
        msg_redpack = String.format(msg_redpack, sign);
        logger.debug("send red pack request: " + msg_redpack);
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream is = new FileInputStream(new File("conf/cert/apiclient_cert.p12"));
            keyStore.load(is, FjServerToolkit.getServerConfig("web.wechat.mch.id").toCharArray());
            SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, FjServerToolkit.getServerConfig("web.wechat.mch.id").toCharArray()).build();
            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(new StringEntity(msg_redpack, "utf-8"));
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuilder result = new StringBuilder();
            String text;
            while ((text = br.readLine()) != null) result.append(text + "\r\n");
            br.close();
            is.close();
            httpclient.getConnectionManager().shutdown();
            logger.debug("send red pack response: " + result.toString());
            return new FjXmlMessage(result.toString());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | KeyManagementException | UnrecoverableKeyException e) {
            logger.error("send red pack railed", e);
        }
        return null;
    }

    public static String createSignature4Config(String nonceStr, String ticket, long timestamp, String url) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("noncestr",     nonceStr);
        map.put("jsapi_ticket", ticket);
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
        sb.append("&key=" + FjServerToolkit.getServerConfig("web.wechat.mch.key"));
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
