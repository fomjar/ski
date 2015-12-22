package com.wtcrm.fbbp.guard;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.wtcrm.fbbp.BE;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjServerToolkit;
import fomjar.util.FjLoopTask;

public class TaobaoOrderListNewGuard extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(TaobaoOrderListNewGuard.class);
	
	private BE be;
	
	public TaobaoOrderListNewGuard(BE be) {
		long time = Long.parseLong(FjServerToolkit.getServerConfig("reload-order-interval"));
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
		msg.json().put("ts", "wa");
		msg.json().put("sid", FjServerToolkit.newSid(serverName));
		msg.json().put("cmd", "ae.taobao.order-list-new");
		msg.json().put("arg", JSONObject.fromObject(String.format("{'user':'%s','pass':'%s'}", FjServerToolkit.getServerConfig("taobao.user"), FjServerToolkit.getServerConfig("taobao.pass"))));
		FjServerToolkit.getSender(serverName).send(msg);
		be.openSession(msg.json().getString("sid"));
		logger.debug("send request to get taobao new order list: " + msg);
	}
	
	public void start() {
		if (isRun()) {
			logger.warn("order-guard has already started");
			return;
		}
		new Thread(this, "order-list-guard").start();
	}
}
