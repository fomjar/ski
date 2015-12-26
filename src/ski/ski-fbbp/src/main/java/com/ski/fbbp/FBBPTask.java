package com.ski.fbbp;

import com.ski.fbbp.be.TaobaoOrderProc;
import com.ski.fbbp.guard.TaobaoOrderListNewGuard;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.be.FjBusinessExecutor;
import fomjar.server.be.SessionNotOpenException;
import fomjar.server.msg.FjDSCPMessage;
import fomjar.server.msg.FjMessage;

public class FBBPTask implements FjServerTask {
	
	private FjBusinessExecutor[] bes;
	
	public FBBPTask(FjServer server) {
		bes = new FjBusinessExecutor[] {new TaobaoOrderProc(server)};
		new TaobaoOrderListNewGuard(bes[0]).start();
	}

	@Override
	public void onMessage(FjServer server, FjMessageWrapper wrapper) {
		FjMessage msg = wrapper.message();
		if (msg instanceof FjDSCPMessage)
			try {FjBusinessExecutor.dispatch(bes, (FjDSCPMessage) msg);}
			catch (SessionNotOpenException e) {e.printStackTrace();}
	}

}
