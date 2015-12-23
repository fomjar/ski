package com.ski.fbbp.be;

import org.apache.log4j.Logger;

import com.ski.fbbp.Constant;

import fomjar.server.FjBE;
import fomjar.server.FjJsonMsg;
import fomjar.server.FjServerToolkit;

/**
 * 淘宝订单查询和入库业务
 * 
 * @author fomja
 *
 */
public class TaobaoOrderListNew extends FjBE {

	private static final Logger logger = Logger.getLogger(TaobaoOrderListNew.class);

	@Override
	public boolean execute(FjSCB scb, FjJsonMsg msg) {
		switch (scb.currPhase()) {
		case 0: // 查询订单结果
			processQueryResult(msg);
			return false;
		case 1: // 订单入库结果
			processStoreResult(msg);
			return true;
		default:
			logger.error("unexpected scb phase(" + scb.currPhase() + ") for msg: " + msg);
			return true;
		}
	}
	
	private void processQueryResult(FjJsonMsg msg) {
		if (Constant.AE.CODE_SUCCESS != msg.json().getInt("code")) {
			logger.error("query new taobao order list failed, reason: " + msg.json().get("desc"));
			return;
		}
		FjJsonMsg msg_cdb = new FjJsonMsg();
		msg_cdb.json().put("fs", getServerName());
		msg_cdb.json().put("ts", "cdb");
		msg_cdb.json().put("sid", msg.json().getString("sid"));
		msg_cdb.json().put("cmd", "taobao-order-list-new");
		msg_cdb.json().put("arg", msg.json().getJSONObject("desc").getJSONArray("orders"));
		FjServerToolkit.getSender(getServerName()).send(msg_cdb);
		logger.debug("forward taobao order list from wa to cdb");
	}
	
	private void processStoreResult(FjJsonMsg msg) {
		if (Constant.AE.CODE_SUCCESS != msg.json().getInt("code")) {
			logger.error("store taobao new order list failed, reason: " + msg.json().get("desc"));
			return;
		}
		logger.error("store taobao new order list success");
	}

}
