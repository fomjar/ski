package com.ski.frs.ccu;

import org.apache.log4j.Logger;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class CcuTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(CcuTask.class);
    
    private StoreBlockService sbs = StoreBlockService.getInstance();

    @Override
    public void initialize(FjServer server) {
        sbs.open();
    }

    @Override
    public void destroy(FjServer server) {
        sbs.close();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }

        FjDscpMessage dmsg = (FjDscpMessage) msg;
        logger.info(String.format("%s - 0x%08X", dmsg.sid(), dmsg.inst()));
        
        JSONObject json = StoreBlockService.getInstance().dispatch(dmsg.inst(), dmsg.argsToJsonObject());
        
        logger.info("dispatch: " + json);
        FjServerToolkit.dscpResponse(dmsg, json.getInt("code"), json.get("desc"));
    }
}
