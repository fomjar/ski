package com.ski.fbbp;

import com.ski.fbbp.guard.TaobaoOrderListNewGuard;
import com.ski.fbbp.sc.ProcTaobaoOrder;
import com.ski.fbbp.sc.ProcWechat;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjMessage;
import fomjar.server.session.FjSessionController;
import fomjar.server.session.FjSessionNotOpenException;

public class FBBPTask implements FjServerTask {
	
	private FjSessionController[] scs;
	
	public FBBPTask(FjServer server) {
		scs = new FjSessionController[] {new ProcTaobaoOrder(server), new ProcWechat(server)};
		new TaobaoOrderListNewGuard(scs[0]).start();
	}

	@Override
	public void onMessage(FjServer server, FjMessageWrapper wrapper) {
		FjMessage msg = wrapper.message();
		if (msg instanceof FjDscpMessage) {
			if (((FjDscpMessage) msg).fs().startsWith("wca")
					&& 0 == ((FjDscpMessage) msg).ssn()) scs[1].openSession(((FjDscpMessage) msg).sid());
			
			try {FjSessionController.dispatch(scs, (FjDscpMessage) msg);}
			catch (FjSessionNotOpenException e) {e.printStackTrace();}
		}
	}

}
