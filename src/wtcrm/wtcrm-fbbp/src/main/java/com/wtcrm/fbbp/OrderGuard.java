package com.wtcrm.fbbp;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjLoopTask;
import fomjar.server.FjMsg;
import fomjar.server.FjToolkit;

public class OrderGuard extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(OrderGuard.class);
	
	private String name;
	
	public OrderGuard(String name) {
		long time = Long.parseLong(FjToolkit.getServerConfig("fbbp.reload-order-interval"));
		time *= 1000L;
		setDelay(time);
		setInterval(time);
		this.name = name;
	}

	@Override
	public void perform() {
		Map<String, String> msg_data = new HashMap<String, String>();
		msg_data.put("fs", name);
		msg_data.put("ts", "wa");
		msg_data.put("sid", String.valueOf(System.currentTimeMillis()));
		msg_data.put("ae-cmd", "ae.taobao.order-list-new");
		msg_data.put("ae-arg", null);
		FjMsg msg = new FjJsonMsg(msg_data);
		FjToolkit.getSender(name).send(msg);
		logger.debug("send request to get latest order list: " + msg);
	}
	
	public void start() {
		if (isRun()) {
			logger.warn("order-guard has already started");
			return;
		}
		new Thread(this, "order-guard").start();
	}
}
