package com.wtcrm.fbbp;

import org.apache.log4j.Logger;

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
		logger.error(msg);
	}

}
