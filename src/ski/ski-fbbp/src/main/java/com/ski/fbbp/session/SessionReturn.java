package com.ski.fbbp.session;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.ski.common.DSCP;

import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSCB;
import fomjar.server.session.FjSessionController;

public class SessionReturn extends FjSessionController {
	
	private static final Logger logger = Logger.getLogger(SessionReturn.class);

	@Override
	public void onSession(FjServer server, FjSCB scb, FjDscpMessage msg) {
		switch (msg.cmd()) {
		case DSCP.CMD.ECOM_APPLY_RETURN:
			logger.info("ECOM_APPLY_RETURN - " + scb.sid());
			processReturnApply(server.name(), scb, msg);
			break;
		case DSCP.CMD.ECOM_SPECIFY_RETURN:
			logger.info("ECOM_SPECIFY_RETURN - " + scb.sid());
			processReturnSpecify(server.name(), scb, msg);
			break;
		}
	}
	
	private static void processReturnApply(String serverName, FjSCB scb, FjDscpMessage msg) {
		if (msg.fs().startsWith("wca")) { // 请求来自WCA，向CDB请求产品详单
			JSONObject arg = new JSONObject();
			arg.put("user", ((JSONObject) msg.arg()).getString("user"));
			arg.put("content", FjServerToolkit.getServerConfig("wechat.response.accept"));
			FjDscpMessage msg_wca = new FjDscpMessage();
			msg_wca.json().put("fs",  serverName);
			msg_wca.json().put("ts",  msg.fs());
			msg_wca.json().put("sid", msg.sid());
			msg_wca.json().put("cmd", DSCP.CMD.USER_RESPONSE);
			String url = "http://www.pan-o.cn:8080/wcweb?cmd=%s&user=%s&content=%s";
			url = String.format(url, Integer.toHexString(DSCP.CMD.ECOM_SPECIFY_RETURN), ((JSONObject) msg.arg()).getString("user"), "test");
			arg.put("content", "<a href='" + url + "'>《刺客信条：兄弟会》</a>\n价格：84.8美元\n起租时间：2016/01/14 00:49\n退租时间：2016/01/14 01:49\n费用: 12元");
			msg_wca.json().put("arg", arg);
			FjServerToolkit.getSender(serverName).send(msg_wca);
		} else if (msg.fs().startsWith("cdb")) { // 请求来自CDB，转发产品详单至WCA
			
		}
	}
	
	private static void processReturnSpecify(String serverName, FjSCB scb, FjDscpMessage msg) {
		scb.end();
	}

}
