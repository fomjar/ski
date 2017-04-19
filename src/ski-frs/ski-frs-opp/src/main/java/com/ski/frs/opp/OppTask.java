package com.ski.frs.opp;

import org.apache.log4j.Logger;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.msg.FjDscpMessage;

public class OppTask implements FjServer.FjServerTask {
    
    private static final Logger logger = Logger.getLogger(OppTask.class);

    @Override
    public void initialize(FjServer server) {}

    @Override
    public void destroy(FjServer server) {}

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }
        
        FjDscpMessage dmsg = (FjDscpMessage) wrapper.message();
        logger.info(String.format("%s - 0x%08X", dmsg.sid(), dmsg.inst()));
        
        switch (dmsg.inst()) {
        
        }
    }

}
