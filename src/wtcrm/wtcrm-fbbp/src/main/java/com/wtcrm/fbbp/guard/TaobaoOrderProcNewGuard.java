package com.wtcrm.fbbp.guard;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.wtcrm.fbbp.BE;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjLoopTask;
import fomjar.server.FjToolkit;

public class TaobaoOrderProcNewGuard extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(TaobaoOrderProcNewGuard.class);
	private BE be;
	
	public TaobaoOrderProcNewGuard(BE be) {
		long time = Long.parseLong(FjToolkit.getServerConfig("fbbp.reproc-order-interval"));
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
		msg.json().put("sid", FjToolkit.newSid(serverName));
		msg.json().put("cmd", "ae.taobao.order-proc-new");
		msg.json().put("arg", JSONObject.fromObject(null));
		FjToolkit.getSender(serverName).send(msg);
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
