package com.ski.xs.bcs;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;

public class BcsTask implements FjServer.FjServerTask {
    
    private BcsSession session;

    @Override
    public void initialize(FjServer server) {
        session = new BcsSession();
    }

    @Override
    public void destroy(FjServer server) {}

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        session.dispatch(server, wrapper);
    }

}
