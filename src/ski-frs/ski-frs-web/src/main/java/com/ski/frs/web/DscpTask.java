package com.ski.frs.web;

import org.apache.log4j.Logger;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpResponse;

public class DscpTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(DscpTask.class);

    @Override
    public void initialize(FjServer server) {}

    @Override
    public void destroy(FjServer server) {}

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) return;
        
        FjDscpMessage dmsg = (FjDscpMessage) msg;
        logger.info(String.format("[ RESPONSE  ] - 0x%08X", dmsg.inst()));
        
        FjHttpResponse response = CacheResponse.getInstance().cache(dmsg.sid());
        if (null != response) {
            response.attr().put("Content-Type", "application/json");
            response.attr().put("Content-Encoding", "gzip");
            response.content(dmsg.args());
            CacheResponse.getInstance().cacheNotify(dmsg.sid());
        }
    }

}
