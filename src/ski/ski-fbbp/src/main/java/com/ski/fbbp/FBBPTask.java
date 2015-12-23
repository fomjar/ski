package com.ski.fbbp;

import com.ski.fbbp.be.TaobaoOrderListNew;
import com.ski.fbbp.guard.TaobaoOrderListNewGuard;

import fomjar.server.FjBE;
import fomjar.server.FjMsg;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;

public class FBBPTask implements FjServerTask {
	
	private FjBE[] bes;
	
	public FBBPTask(String name) {
		bes = new FjBE[] {new TaobaoOrderListNew()};
		for (FjBE be : bes) be.setServerName(name);
		new TaobaoOrderListNewGuard(bes[0]).start();
	}

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		FjBE.dispatch(bes, msg);
	}

}
