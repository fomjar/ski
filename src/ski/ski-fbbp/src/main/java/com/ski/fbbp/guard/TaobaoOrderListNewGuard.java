package com.ski.fbbp.guard;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.ski.common.DSCP;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjSender;
import fomjar.server.FjServerToolkit;
import fomjar.server.be.FjBusinessExecutor;
import fomjar.server.msg.FjDSCPRequest;
import fomjar.util.FjLoopTask;

public class TaobaoOrderListNewGuard extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(TaobaoOrderListNewGuard.class);
	
	private FjBusinessExecutor be;
	
	public TaobaoOrderListNewGuard(FjBusinessExecutor be) {
		long time = Long.parseLong(FjServerToolkit.getServerConfig("taobao.order.proc-interval"));
		time *= 1000L;
		setDelay(time);
		setInterval(time);
		this.be = be;
	}

	@Override
	public void perform() {
		String serverName = be.getServer().name();
		FjDSCPRequest req = new FjDSCPRequest();
		req.json().put("fs",  serverName);
		req.json().put("ts",  "wa");
		req.json().put("sid", FjDSCPRequest.newSid(serverName));
		req.json().put("ssn", 0);
		req.json().put("cmd", DSCP.CMD.TAOBAO_ORDER_LIST_NEW);
		req.json().put("arg", JSONObject.fromObject(String.format("{'user':'%s','pass':'%s'}", FjServerToolkit.getServerConfig("taobao.account.user"), FjServerToolkit.getServerConfig("taobao.account.pass"))));
		FjServerToolkit.getSender(serverName).send(new FjMessageWrapper(req).attach("observer", new FjSender.FjSenderObserver() {@Override public void onSuccess() {be.openSession(req.sid());}}));
		logger.debug("send request to get taobao new order list: " + req);
	}
	
	public void start() {
		if (isRun()) {
			logger.warn("taobao-order-list-guard has already started");
			return;
		}
		new Thread(this, "taobao-order-list-guard").start();
	}
}
