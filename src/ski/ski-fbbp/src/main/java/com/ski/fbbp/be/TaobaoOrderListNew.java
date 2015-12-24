package com.ski.fbbp.be;

import org.apache.log4j.Logger;

import com.ski.fbbp.Constant;

import fomjar.server.FjJsonMessage;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.be.FjBusinessExecutor;

/**
 * 淘宝订单查询和入库业务
 * 
 * @author fomja
 *
 */
public class TaobaoOrderListNew extends FjBusinessExecutor {

	public TaobaoOrderListNew(FjServer server) {
		super(server);
	}

	private static final Logger logger = Logger.getLogger(TaobaoOrderListNew.class);

	@Override
	public void execute(FjSCB scb, FjJsonMessage msg) {
		switch (scb.currPhase()) {
		case 0: // 查询订单结果
			processQueryResult(scb, msg);
			break;
		case 1: // 订单入库结果
			processStoreResult(scb, msg);
			scb.end();
			break;
		default:
			logger.error("unexpected scb phase(" + scb.currPhase() + ") for msg: " + msg);
			scb.end();
			break;
		}
	}
	
	private void processQueryResult(FjSCB scb, FjJsonMessage msg) {
		if (Constant.AE.CODE_SUCCESS != msg.json().getInt("code")) {
			logger.error("query new taobao order list failed, reason: " + msg.json().get("desc"));
			return;
		}
		FjJsonMessage msg_cdb = new FjJsonMessage();
		msg_cdb.json().put("fs", getServer().name());
		msg_cdb.json().put("ts", "cdb");
		msg_cdb.json().put("sid", msg.json().getString("sid"));
		msg_cdb.json().put("cmd", "taobao-order-list-new");
		msg_cdb.json().put("arg", msg.json().getJSONObject("desc").getJSONArray("orders"));
		FjServerToolkit.getSender(getServer().name()).send(msg_cdb);
		logger.debug("forward taobao order list from wa to cdb");
	}
	
	private void processStoreResult(FjSCB scb, FjJsonMessage msg) {
		if (Constant.AE.CODE_SUCCESS != msg.json().getInt("code")) {
			logger.error("store taobao new order list failed, reason: " + msg.json().get("desc"));
			return;
		}
		logger.error("store taobao new order list success");
	}

}
