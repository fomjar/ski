package com.ski.wca;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.ski.common.DSCP;
import com.ski.wca.be.DefaultBE;
import com.ski.wca.guard.MenuGuard;
import com.ski.wca.guard.TokenGuard;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.be.FjBusinessExecutor;
import fomjar.server.be.SessionNotOpenException;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjDscpRequest;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjMessage;

public class WCATask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(WCATask.class);
	private FjBusinessExecutor[] bes;
	
	public WCATask(String name) {
		TokenGuard.getInstance().setServerName(name);
		TokenGuard.getInstance().start();
		new MenuGuard(name).start();
		bes = new FjBusinessExecutor[] {new DefaultBE(FjServerToolkit.getServer(name))};
	}
	
	@Override
	public void onMessage(FjServer server, FjMessageWrapper wrapper) {
		FjMessage msg = wrapper.message();
		if (msg instanceof FjHttpRequest) {
			if (((FjHttpRequest) msg).url().startsWith("/wechat")) {
				logger.info("message comes from wechat server");
				processWechat(server.name(), wrapper);
			} else {
				logger.error("invalid http message, discard: " + msg);
			}
		} else if (msg instanceof FjDscpMessage) {
			try {FjBusinessExecutor.dispatch(bes, (FjDscpMessage) msg);}
			catch (SessionNotOpenException e) {logger.error(e);}
		} else {
			logger.error("invalid format message, discard: " + msg);
		}
	}
	
	private void processWechat(String serverName, FjMessageWrapper wrapper) {
		if (((FjHttpRequest) wrapper.message()).urlParameters().containsKey("echostr")) {
			logger.info("response access message");
			WechatInterface.access(wrapper);
			return;
		}
		Element xml      = ((FjHttpRequest) wrapper.message()).contentToXml().getDocumentElement();
		String user_from = xml.getElementsByTagName("FromUserName").item(0).getTextContent();
		String user_to   = xml.getElementsByTagName("ToUserName").item(0).getTextContent();
		String content   = null;
		String event     = null;
		String event_key = null;
		String msg_type   = xml.getElementsByTagName("MsgType").item(0).getTextContent();
		FjDscpRequest req = new FjDscpRequest();
		req.json().put("fs", serverName);
		req.json().put("ts", FjServerToolkit.getServerConfig("wca.report"));
		req.json().put("sid", FjDscpRequest.newSid(serverName));
		req.json().put("ssn", 0);
		if ("text".equals(msg_type))  {
			content = xml.getElementsByTagName("Content").item(0).getTextContent();
			req.json().put("cmd", DSCP.CMD.WECHAT_USER_TEXT);
			req.json().put("arg", content);
		} else if ("event".equals(msg_type)) {
			event     = xml.getElementsByTagName("Event").item(0).getTextContent();
			event_key = xml.getElementsByTagName("EventKey").item(0).getTextContent();
			if ("subscribe".equals(event)) {
				req.json().put("cmd", DSCP.CMD.WECHAT_USER_SUBSCRIBE);
				req.json().put("arg", user_from);
			} else if ("unsubscribe".equals(event)) {
				req.json().put("cmd", DSCP.CMD.WECHAT_USER_UNSUBSCRIBE);
				req.json().put("arg", user_from);
			} else if ("CLICK".equals(event)) {
				req.json().put("cmd", DSCP.CMD.WECHAT_USER_CLICK);
				req.json().put("arg", event_key);
			} else if ("VIEW".equals(event)) {
				req.json().put("cmd", DSCP.CMD.WECHAT_USER_VIEW);
				req.json().put("arg", event_key);
			}
		} else if ("location".equals(msg_type)) {
			float x      = Float.parseFloat(xml.getElementsByTagName("Location_X").item(0).getTextContent());
			float y      = Float.parseFloat(xml.getElementsByTagName("Location_Y").item(0).getTextContent());
			int   scale  = Integer.parseInt(xml.getElementsByTagName("Scale").item(0).getTextContent());
			String label = xml.getElementsByTagName("Label").item(0).getTextContent();
			req.json().put("cmd", DSCP.CMD.WECHAT_USER_LOCATION);
			req.json().put("arg", JSONObject.fromObject(String.format("{'x':%f, 'y':%f, 'scale':%d, 'label':\"%s\"}", x, y, scale, label)));
		}
		bes[0].openSession(req.sid())
				.put("user_from", user_from)
				.put("user_to", user_to)
				.put("conn", wrapper.attachment("conn")); // 将消息附属连接存到会话控制块缓存中，后续响应消息用
		wrapper.attach("conn", null); // 清除连接，防止平台将其关闭
		FjServerToolkit.getSender(serverName).send(req);
	}
}
