package com.ski.sn.ura;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;

public class UraTask implements FjServer.FjServerTask {

    @Override
    public void initialize(FjServer server) {}

    @Override
    public void destroy(FjServer server) {}

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
    }

}
