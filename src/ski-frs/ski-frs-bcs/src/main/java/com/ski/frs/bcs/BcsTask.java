package com.ski.frs.bcs;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class BcsTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(BcsTask.class);

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
        if (dmsg.fs().startsWith("cdb")) return;
        
        switch (dmsg.inst()) {
        case ISIS.INST_UPDATE_PIC:
            processUpdatePic(dmsg);
            break;
        }
    }
    
    private static void processUpdatePic(FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        FjServerToolkit.dscpRequest("cdb", dmsg.sid(), ISIS.INST_UPDATE_PIC, args);
    }

}
