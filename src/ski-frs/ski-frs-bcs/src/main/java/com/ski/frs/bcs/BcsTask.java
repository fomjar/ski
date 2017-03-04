package com.ski.frs.bcs;

import org.apache.log4j.Logger;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;

public class BcsTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(BcsTask.class);

    @Override
    public void initialize(FjServer server) {}

    @Override
    public void destroy(FjServer server) {}

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        logger.error(wrapper.message());
    }

}
