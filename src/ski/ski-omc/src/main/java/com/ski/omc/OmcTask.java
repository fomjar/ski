package com.ski.omc;

import org.apache.log4j.Logger;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;

public class OmcTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(OmcTask.class);

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        String inst = Integer.toHexString(((FjDscpMessage) wrapper.message()).inst());
        while (8 > inst.length()) inst = "0" + inst;
        logger.info(String.format("fs: %10s ts: %10s sid: %40s inst: 0x%s", 
                ((FjDscpMessage) wrapper.message()).fs(),
                ((FjDscpMessage) wrapper.message()).ts(),
                ((FjDscpMessage) wrapper.message()).sid(),
                inst));
    }

}
