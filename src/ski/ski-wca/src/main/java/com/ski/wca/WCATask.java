package com.ski.wca;

import java.nio.channels.SocketChannel;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.ski.common.DSCP;
import com.ski.wca.WechatInterface.WechatPermissionDeniedException;
import com.ski.wca.guard.CustomServiceGuard;
import com.ski.wca.guard.MenuGuard;
import com.ski.wca.guard.TokenGuard;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjMessage;

public class WCATask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(WCATask.class);
	
	public WCATask(String name) {
		TokenGuard.getInstance().setServerName(name);
		TokenGuard.getInstance().start();
		new MenuGuard().start();
		new CustomServiceGuard().start();
	}
	
	@Override
	public void onMessage(FjServer server, FjMessageWrapper wrapper) {
		FjMessage msg = wrapper.message();
		if (msg instanceof FjHttpRequest) {
			FjHttpRequest hmsg = (FjHttpRequest) msg;
			if (hmsg.url().startsWith("/wechat")) processWechat(server.name(), wrapper);
			else logger.error("unsupported http message: " + hmsg.url());
		} else if (msg instanceof FjDscpMessage) {
			processSKI(server.name(), wrapper);
		} else {
			logger.error("unsupported format message: " + msg);
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
		String user_to   = xml.getElementsByTagName("ToUserName").item(0).getTextContent();
		// 第一时间给微信响应
//		WechatInterface.sendResponse("success", (SocketChannel) wrapper.attachment("conn"));
		WechatInterface.sendXmlResponse(user_to, user_from, "<a href=\"http://www.pan-o.cn:8080/wcweb?cmd=00200001&user=123&account=456\">刺客信条</a>", (SocketChannel) wrapper.attachment("conn"));
		
		FjDscpMessage req = new FjDscpMessage();
		req.json().put("fs", serverName);
		req.json().put("ts", FjServerToolkit.getServerConfig("wca.report"));
		req.json().put("sid", user_from);
		
		JSONObject arg = new JSONObject();
		arg.put("user", user_from);
		
		String content   = null;
		String event     = null;
		String event_key = null;
		String msg_type  = xml.getElementsByTagName("MsgType").item(0).getTextContent();
		if ("text".equals(msg_type))  {
			content = xml.getElementsByTagName("Content").item(0).getTextContent();
			req.json().put("cmd", DSCP.CMD.USER_REQUEST);
			arg.put("content", content);
		} else if ("event".equals(msg_type)) {
			event     = xml.getElementsByTagName("Event").item(0).getTextContent();
			event_key = xml.getElementsByTagName("EventKey").item(0).getTextContent();
			if ("subscribe".equals(event)) {
				req.json().put("cmd", DSCP.CMD.USER_SUBSCRIBE);
			} else if ("unsubscribe".equals(event)) {
				req.json().put("cmd", DSCP.CMD.USER_UNSUBSCRIBE);
			} else if ("CLICK".equals(event)) {
				req.json().put("cmd", Integer.parseInt(event_key, 16));
			} else if ("VIEW".equals(event)) {
				req.json().put("cmd", DSCP.CMD.USER_GOTO);
				arg.put("content", event_key);
			}
		} else if ("location".equals(msg_type)) {
			float  x     = Float.parseFloat(xml.getElementsByTagName("Location_X").item(0).getTextContent());
			float  y     = Float.parseFloat(xml.getElementsByTagName("Location_Y").item(0).getTextContent());
			int    scale = Integer.parseInt(xml.getElementsByTagName("Scale").item(0).getTextContent());
			String label = xml.getElementsByTagName("Label").item(0).getTextContent();
			req.json().put("cmd", DSCP.CMD.USER_GOTO);
			arg.put("content", JSONObject.fromObject(String.format("{'x':%f, 'y':%f, 'scale':%d, 'label':\"%s\"}", x, y, scale, label)));
		} else if ("image".equals(msg_type)) {
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
		req.json().put("arg", arg);
		FjServerToolkit.getSender(serverName).send(req); // 用户行为上报业务
	}
	
	private void processSKI(String serverName, FjMessageWrapper wrapper) {
		FjDscpMessage req = (FjDscpMessage) wrapper.message();
		switch (req.cmd()) {
		case DSCP.CMD.USER_RESPONSE: // 响应用户
			try {WechatInterface.customSendTextMessage(((JSONObject) req.arg()).getString("user"), ((JSONObject) req.arg()).getString("content"));}
			catch (WechatPermissionDeniedException e) {logger.error("send custom service message failed: " + req, e);}
			break;
		}
	}
}
