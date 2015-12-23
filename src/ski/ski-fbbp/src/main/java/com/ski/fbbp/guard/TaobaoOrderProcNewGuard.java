package com.ski.fbbp.guard;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;
import fomjar.server.FjBE;
import fomjar.server.FjJsonMsg;
import fomjar.server.FjServerToolkit;
import fomjar.util.FjLoopTask;

public class TaobaoOrderProcNewGuard extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(TaobaoOrderProcNewGuard.class);
	private FjBE be;
	
	public TaobaoOrderProcNewGuard(FjBE be) {
		long time = Long.parseLong(FjServerToolkit.getServerConfig("reproc-order-interval"));
		time *= 1000L;
		setDelay(time);
		setInterval(time);
		this.be = be;
	}

	@Override
	public void perform() {
		String serverName = be.getServerName();
		FjJsonMsg msg = new FjJsonMsg();
		msg.json().put("fs", serverName);
		msg.json().put("ts", "cdb");
		msg.json().put("sid", FjServerToolkit.newSid(serverName));
		msg.json().put("cmd", "ae.taobao.order-proc-new");
		msg.json().put("arg", JSONObject.fromObject(null));
		FjServerToolkit.getSender(serverName).send(msg);
		be.openSession(msg.json().getString("sid"));
		logger.debug("send request to get taobao new order list: " + msg);
	}
	
	public void start() {
		if (isRun()) {
			logger.warn("order-proc-guard has already started");
			return;
		}
		new Thread(this, "order-proc-guard").start();
	}

}
