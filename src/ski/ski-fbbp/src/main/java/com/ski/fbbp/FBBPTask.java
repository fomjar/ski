package com.ski.fbbp;

import com.ski.fbbp.be.TaobaoOrderListNew;
import com.ski.fbbp.guard.TaobaoOrderListNewGuard;

import fomjar.server.FjMessage;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.be.FjBusinessExecutor;
import fomjar.server.be.SessionNotOpenException;

public class FBBPTask implements FjServerTask {
	
	private FjBusinessExecutor[] bes;
	
	public FBBPTask(FjServer server) {
		bes = new FjBusinessExecutor[] {new TaobaoOrderListNew(server)};
		new TaobaoOrderListNewGuard(bes[0]).start();
	}

	@Override
	public void onMsg(FjServer server, FjMessage msg) {
		try {FjBusinessExecutor.dispatch(bes, msg);}
		catch (SessionNotOpenException e) {e.printStackTrace();}
	}

}
