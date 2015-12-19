package com.wtcrm.fbbp;

import org.apache.log4j.Logger;

import com.wtcrm.fbbp.be.TaobaoOrderListNew;
import com.wtcrm.fbbp.guard.TaobaoOrderListNewGuard;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjMsg;
import fomjar.server.FjServer;
import fomjar.server.FjToolkit;
import fomjar.server.FjServer.FjServerTask;

public class FBBPTask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(FBBPTask.class);
	private BE[] bes;
	
	public FBBPTask(String name) {
		bes = new BE[] {new TaobaoOrderListNew()};
		for (BE be : bes) be.setServerName(name);
		new TaobaoOrderListNewGuard(bes[0]).start();
	}

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (!FjToolkit.isLegalMsg(msg)) {
			logger.error("illegal message, discard: " + msg);
			return;
		}
		FjJsonMsg jmsg = (FjJsonMsg) msg;
		String sid = jmsg.json().getString("sid");
		for (BE be : bes) {
			if (be.hasSession(sid)) {
				BE.SCB scb = be.getSession(sid);
				scb.nextPhase();
				boolean end = false;
				try {end = be.execute(scb, jmsg);}
				catch (Exception e) {logger.error("error occurs when execute be for this message: " + msg, e);}
				if (end) {
					be.closeSession(sid);
					logger.info("session " + sid + " closed");
				}
			}
		}
	}

}
