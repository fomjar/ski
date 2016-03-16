package com.ski.wca;

import java.nio.channels.SocketChannel;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.ski.common.SkiCommon;
import com.ski.wca.WechatInterface.WechatInterfaceException;
import com.ski.wca.monitor.MenuMonitor;
import com.ski.wca.monitor.TokenMonitor;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjJsonMessage;

public class WcaTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(WcaTask.class);
    
    public WcaTask() {
        TokenMonitor.getInstance().start();
        new MenuMonitor().start();
    }
    
    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (msg instanceof FjHttpRequest) {
            FjHttpRequest hmsg = (FjHttpRequest) msg;
            if (hmsg.url().startsWith("/wechat")) processWechat(server.name(), wrapper);
            else logger.error("unsupported http message:\n" + wrapper.attachment("raw"));
        } else if (msg instanceof FjDscpMessage) {
            processSKI(server.name(), wrapper);
        } else {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
        }
    }
    
    private void processWechat(String serverName, FjMessageWrapper wrapper) {
        if (((FjHttpRequest) wrapper.message()).urlParameters().containsKey("echostr")) {
            WechatInterface.access(wrapper);
            logger.info("wechat access");
            return;
        }
        Element xml      = ((FjHttpRequest) wrapper.message()).contentToXml().getDocumentElement();
        String user_from = xml.getElementsByTagName("FromUserName").item(0).getTextContent();
        // String user_to   = xml.getElementsByTagName("ToUserName").item(0).getTextContent();
        // 第一时间给微信响应
        WechatInterface.sendResponse("success", (SocketChannel) wrapper.attachment("conn"));
        
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs", serverName);
        req.json().put("ts", FjServerToolkit.getServerConfig("wca.report"));
        req.json().put("sid", user_from);
        
        JSONObject args = new JSONObject();
        args.put("user", user_from);
        
        String content   = null;
        String event     = null;
        String event_key = null;
        String msg_type  = xml.getElementsByTagName("MsgType").item(0).getTextContent();
        if ("text".equals(msg_type))  {
            logger.info("INST_USER_REQUEST     - wechat:" + user_from);
            content = xml.getElementsByTagName("Content").item(0).getTextContent();
            req.json().put("inst", SkiCommon.ISIS.INST_USER_REQUEST);
            args.put("content", content);
        } else if ("event".equals(msg_type)) {
            event     = xml.getElementsByTagName("Event").item(0).getTextContent();
            event_key = xml.getElementsByTagName("EventKey").item(0).getTextContent();
            if ("subscribe".equals(event)) {
                logger.info("INST_USER_SUBSCRIBE   - wechat:" + user_from);
                req.json().put("inst", SkiCommon.ISIS.INST_USER_SUBSCRIBE);
            } else if ("unsubscribe".equals(event)) {
                logger.info("INST_USER_UNSUBSCRIBE - wechat:" + user_from);
                req.json().put("inst", SkiCommon.ISIS.INST_USER_UNSUBSCRIBE);
            } else if ("CLICK".equals(event)) {
                logger.info(String.format("INST_USER_INSTRUCTION - wechat:%s:0x%s", user_from, event_key));
                req.json().put("inst", Integer.parseInt(event_key, 16));
            } else if ("VIEW".equals(event)) {
                logger.info("INST_USER_GOTO - wechat:" + user_from);
                req.json().put("inst", SkiCommon.ISIS.INST_USER_GOTO);
                args.put("content", event_key);
            }
        } else if ("location".equals(msg_type)) {
            logger.info("INST_USER_LOCATION    - wechat:" + user_from);
            float  x     = Float.parseFloat(xml.getElementsByTagName("Location_X").item(0).getTextContent());
            float  y     = Float.parseFloat(xml.getElementsByTagName("Location_Y").item(0).getTextContent());
            int    scale = Integer.parseInt(xml.getElementsByTagName("Scale").item(0).getTextContent());
            String label = xml.getElementsByTagName("Label").item(0).getTextContent();
            req.json().put("inst", SkiCommon.ISIS.INST_USER_LOCATION);
            args.put("content", JSONObject.fromObject(String.format("{'x':%f, 'y':%f, 'scale':%d, 'label':\"%s\"}", x, y, scale, label)));
        } else if ("image".equals(msg_type)) {
            logger.info("INST_USER_IMAGE       - wechat:" + user_from);
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
        } else if ("voice".equals(msg_type)) {
            logger.info("INST_USER_VOICE       - wechat:" + user_from);
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
        } else if ("shortvideo".equals(msg_type)) {
            logger.info("INST_USER_SHORTVIDEO  - wechat:" + user_from);
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
        }
        req.json().put("args", args);
        FjServerToolkit.getSender(serverName).send(req); // 用户行为上报业务
    }
    
    private void processSKI(String serverName, FjMessageWrapper wrapper) {
        FjDscpMessage dmsg = (FjDscpMessage) wrapper.message();
        switch (dmsg.inst()) {
        case SkiCommon.ISIS.INST_USER_RESPONSE: // 响应用户
            logger.info(String.format("INST_USER_RESPONSE    - %s:%s", dmsg.fs(), dmsg.sid()));
            try {
                String user    = dmsg.argsToJsonObject().getString("user");
                String content = dmsg.argsToJsonObject().getString("content");
                FjJsonMessage rsp = WechatInterface.customSendTextMessage(user, content);
                if (0 != rsp.json().getInt("errcode")) logger.error("send custom service message failed: " + rsp);
                else logger.debug(String.format("send custom service message success: user=%s, content=%s", user, content));
            } catch (WechatInterfaceException e) {logger.error("send custom service message failed: " + dmsg, e);}
            break;
        default: // forward report
            String inst = Integer.toHexString(dmsg.inst());
            while (8 > inst.length()) inst = "0" + inst;
            logger.info(String.format("INST_USER_INSTRUCTION - %s:%s:0x%s", dmsg.fs(), dmsg.sid(), inst));
            FjDscpMessage msg = new FjDscpMessage(dmsg.json());
            msg.json().put("fs", serverName);
            msg.json().put("ts", FjServerToolkit.getServerConfig("wca.report"));
            FjServerToolkit.getSender(serverName).send(msg);
            break;
        }
    }
}
