package com.wtcrm.fbbp.be;

import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.wtcrm.fbbp.BE;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjToolkit;

public class TaobaoOrderNew extends BE {
	
	public TaobaoOrderNew(String serverName) {
		super(serverName);
	}

	private static final Logger logger = Logger.getLogger(TaobaoOrderNew.class);

	@Override
	public boolean execute(FjJsonMsg msg, List<FjJsonMsg> msgs_ago) {
		if (0 == msgs_ago.size()) { // 订单
			processOrder(msg);
			return false;
		} else if (msg.json().getString("fs").startsWith("cdb")) {
			logger.error("cdb process order result: " + msg);
			return true;
		} else {
			logger.error("business flow infer error for msg: " + msg);
			return false;
		}
	}
	
	private void processOrder(FjJsonMsg msg) {
		for (Object order_obj : msg.json().getJSONArray("ae-desc")) {
			JSONObject order = (JSONObject) order_obj;
			FjJsonMsg msg_cdb = new FjJsonMsg();
			msg_cdb.json().put("fs", getServerName());
			msg_cdb.json().put("ts", "cdb");
			msg_cdb.json().put("sid", msg.json().getString("sid"));
			msg_cdb.json().put("cdb-cmd", "taobao-order-new");
			msg_cdb.json().put("cdb-arg", order);
			FjToolkit.getSender(getServerName()).send(msg_cdb);
		}
	}

}
