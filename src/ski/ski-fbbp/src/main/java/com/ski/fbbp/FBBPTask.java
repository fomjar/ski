package com.ski.fbbp;

import com.ski.fbbp.be.ProcTaobaoOrder;
import com.ski.fbbp.be.ProcWechat;
import com.ski.fbbp.guard.TaobaoOrderListNewGuard;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.be.FjBusinessExecutor;
import fomjar.server.be.SessionNotOpenException;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjMessage;

public class FBBPTask implements FjServerTask {
	
	private FjBusinessExecutor[] bes;
	
	public FBBPTask(FjServer server) {
		bes = new FjBusinessExecutor[] {new ProcTaobaoOrder(server), new ProcWechat(server)};
		new TaobaoOrderListNewGuard(bes[0]).start();
	}

	@Override
	public void onMessage(FjServer server, FjMessageWrapper wrapper) {
		FjMessage msg = wrapper.message();
		if (msg instanceof FjDscpMessage) {
			if (((FjDscpMessage) msg).fs().startsWith("wca")) bes[1].openSession(((FjDscpMessage) msg).sid());
			
			try {FjBusinessExecutor.dispatch(bes, (FjDscpMessage) msg);}
			catch (SessionNotOpenException e) {e.printStackTrace();}
		}
	}

}
