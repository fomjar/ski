package com.ski.fbbp.be;

import org.apache.log4j.Logger;

import com.ski.common.DSCP;

import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.be.FjBusinessExecutor;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjDscpRequest;
import fomjar.server.msg.FjDscpResponse;

public class ProcWechat extends FjBusinessExecutor {
	
	private static final Logger logger = Logger.getLogger(ProcWechat.class);

	public ProcWechat(FjServer server) {
		super(server);
	}

	@Override
	public void execute(FjSCB scb, FjDscpMessage msg) {
		logger.info("wechat report a message: " + msg);
		switch (msg.ssn()) {
		case 0:
			FjDscpRequest req  = (FjDscpRequest) msg;
			FjDscpResponse rsp = new FjDscpResponse();
			rsp.json().put("fs", getServer().name());
			rsp.json().put("ts", req.fs());
			rsp.json().put("sid", req.sid());
			rsp.json().put("ssn", req.ssn() + 1);
			rsp.json().put("code", DSCP.CODE.SYSTEM_SUCCESS);
			switch (req.cmd()) {
			case DSCP.CMD.WECHAT_USER_TEXT:
				rsp.json().put("desc", req.arg());
				break;
			case DSCP.CMD.WECHAT_USER_SUBSCRIBE:
				rsp.json().put("desc", req.arg());
				break;
			case DSCP.CMD.WECHAT_USER_UNSUBSCRIBE:
				rsp.json().put("desc", req.arg());
				break;
			case DSCP.CMD.WECHAT_USER_CLICK:
				rsp.json().put("desc", req.arg());
				break;
			case DSCP.CMD.WECHAT_USER_VIEW:
				rsp.json().put("desc", req.arg());
				break;
			case DSCP.CMD.WECHAT_USER_LOCATION:
				rsp.json().put("desc", req.arg());
				break;
			default:
				break;
			}
			FjServerToolkit.getSender(getServer().name()).send(rsp);
			scb.end();
			break;
		default:
			break;
		}
	}

}
