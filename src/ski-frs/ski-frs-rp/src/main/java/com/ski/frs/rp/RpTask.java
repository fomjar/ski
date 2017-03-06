package com.ski.frs.rp;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;

public class RpTask implements FjServerTask {
    
    private Monitor monitor;
    
    public RpTask() {
        monitor = new Monitor();
    }

    @Override
    public void initialize(FjServer server) {
        monitor.open();
    }

    @Override
    public void destroy(FjServer server) {
        monitor.close();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {}

}
