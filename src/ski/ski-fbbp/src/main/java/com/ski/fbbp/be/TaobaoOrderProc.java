package com.ski.fbbp.be;

import org.apache.log4j.Logger;

import com.ski.common.DSCP;

import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.be.FjBusinessExecutor;
import fomjar.server.msg.FjDSCPMessage;
import fomjar.server.msg.FjDSCPRequest;
import fomjar.server.msg.FjDSCPResponse;

/**
 * 淘宝订单查询和入库业务
 * 
 * @author fomja
 *
 */
public class TaobaoOrderProc extends FjBusinessExecutor {

	public TaobaoOrderProc(FjServer server) {
		super(server);
	}

	private static final Logger logger = Logger.getLogger(TaobaoOrderProc.class);

	@Override
	public void execute(FjSCB scb, FjDSCPMessage msg) {
		switch (msg.ssn()) {
		case 1: // 网页侧查询订单结果
			processQueryResult(scb, (FjDSCPResponse) msg);
			break;
		case 3: // 数据库预处理订单的结果
			processStoreResult(scb, (FjDSCPResponse) msg);
			scb.end();
			break;
		default:
			logger.error("unexpected ssn for msg: " + msg);
			scb.end();
			break;
		}
	}
	
	private void processQueryResult(FjSCB scb, FjDSCPResponse rsp) {
		if (DSCP.CODE.SYSTEM_SUCCESS != rsp.code()) {
			logger.error(String.format("query new taobao order list failed, code: %d, reason: %s", rsp.code(), rsp.desc()));
			return;
		}
		FjDSCPRequest req_cdb = new FjDSCPRequest();
		req_cdb.json().put("fs",  getServer().name());
		req_cdb.json().put("ts",  "cdb");
		req_cdb.json().put("sid", rsp.sid());
		req_cdb.json().put("ssn", rsp.ssn() + 1);
		req_cdb.json().put("cmd", DSCP.CMD.TAOBAO_ORDER_PROC_NEW);
		req_cdb.json().put("arg", rsp.desc());
		FjServerToolkit.getSender(getServer().name()).send(req_cdb);
		logger.debug("forward taobao order list new from wa to cdb");
	}
	
	private void processStoreResult(FjSCB scb, FjDSCPResponse rsp) {
		if (DSCP.CODE.SYSTEM_SUCCESS != rsp.code()) {
			logger.error("store taobao new order failed, reason: " + rsp.desc());
			return;
		}
		logger.info("store taobao new order success: " + rsp);
	}

}
