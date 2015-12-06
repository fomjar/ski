package com.wtcrm.fbbp;

import org.apache.log4j.Logger;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjMsg;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;

public class FBBPTask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(FBBPTask.class);
	
	public FBBPTask(String name) {
		new OrderGuard(name).start();
	}

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (!(msg instanceof FjJsonMsg)
				|| !((FjJsonMsg) msg).json().containsKey("fs")
				|| !((FjJsonMsg) msg).json().containsKey("ts")) {
			logger.error("message not come from wtcrm server, discard: " + msg);
			return;
		}
		FjJsonMsg jmsg = (FjJsonMsg) msg;
		if (jmsg.json().getString("fs").startsWith("wa"))       processWaMsg(server, jmsg);
		else if (jmsg.json().getString("fs").startsWith("cdb")) processCdbMsg(server, jmsg);
		else {logger.error("invalid wtcrm message, discard: " + jmsg);}
	}

	private static void processWaMsg(FjServer server, FjJsonMsg msg) {
		
	}
	
	private static void processCdbMsg(FjServer server, FjJsonMsg msg) {
	}

}
