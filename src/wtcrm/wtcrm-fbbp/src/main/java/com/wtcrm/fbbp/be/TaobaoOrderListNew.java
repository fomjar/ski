package com.wtcrm.fbbp.be;

import org.apache.log4j.Logger;

import com.wtcrm.fbbp.BE;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjToolkit;

/**
 * 淘宝订单查询和入库业务
 * 
 * @author fomja
 *
 */
public class TaobaoOrderListNew extends BE {

	private static final Logger logger = Logger.getLogger(TaobaoOrderListNew.class);

	@Override
	public boolean execute(SCB scb, FjJsonMsg msg) {
		switch (scb.currPhase()) {
		case 0: // 查询订单结果
			processOrder(msg);
			return false;
		case 1: // 订单入库结果
			logger.error("cdb process order result: " + msg);
			return true;
		default:
			logger.error("business flow infer error for msg: " + msg);
			return true;
		}
	}
	
	private void processOrder(FjJsonMsg msg) {
		FjJsonMsg msg_cdb = new FjJsonMsg();
		msg_cdb.json().put("fs", getServerName());
		msg_cdb.json().put("ts", "cdb");
		msg_cdb.json().put("sid", msg.json().getString("sid"));
		msg_cdb.json().put("cdb-cmd", "taobao-order-list-new");
		msg_cdb.json().put("cdb-arg", msg.json().getJSONObject("ae-desc").getJSONArray("orders"));
		FjToolkit.getSender(getServerName()).send(msg_cdb);
		logger.debug("forward taobao order list from wa to cdb");
	}

}
