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
		case DSCP.CMD.ECOM_RETURN_APPLY:
			logger.info("ECOM_RETURN_APPLY");
			processReturnApply(server.name(), scb, msg);
			break;
		}
	}
	
	private static void processReturnApply(String serverName, FjSCB scb, FjDscpMessage msg) {
		if (msg.fs().startsWith("wca")) { // 请求来自WCA，向CDB请求产品详单
			FjDscpMessage msg_cdb = new FjDscpMessage();
			msg_cdb.json().put("fs",  serverName);
			msg_cdb.json().put("ts",  "cdb");
			msg_cdb.json().put("sid", msg.sid());
			msg_cdb.json().put("cmd", msg.cmd());
			msg_cdb.json().put("arg", ((JSONObject) msg.arg()).getString("user"));
			FjServerToolkit.getSender(serverName).send(msg_cdb);
		} else if (msg.fs().startsWith("cdb")) { // 请求来自CDB，转发产品详单至WCA
			
		}
	}

}
