package com.ski.sn.bcs;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;

public class BcsTask implements FjServer.FjServerTask {
    
    private static final Logger logger = Logger.getLogger(BcsTask.class);
    
    private Map<String, FjDscpMessage> cache;
    
    @Override
    public void initialize(FjServer server) {
        cache = new HashMap<String, FjDscpMessage>();
    }

    @Override
    public void destroy(FjServer server) {
        cache.clear();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }

        FjDscpMessage dmsg = (FjDscpMessage) msg;
        if (server.name().startsWith("wsi")) { // 用户侧请求
            logger.info(String.format("[ REQUEST  ] - %s:%s:0x%08X", dmsg.fs(), dmsg.sid(), dmsg.inst()));
            processRequest(dmsg);
        } else { // 平台侧响应
            logger.info(String.format("[ RESPONSE ] - %s:%s:0x%08X", dmsg.fs(), dmsg.sid(), dmsg.inst()));
            processResponse(dmsg, cache.remove(dmsg.sid()));
        }
    }
    
    public static void processRequest(FjDscpMessage request) {
        switch (request.inst()) {
        
        }
    }
    
    public static void processResponse(FjDscpMessage response, FjDscpMessage request) {
        if (null == request) {
            logger.warn("find no request for response: " + response);
            return;
        }
        
        response.json().put("fs", FjServerToolkit.getAnyServer().name());
        response.json().put("ts", request.fs());
        FjServerToolkit.getAnySender().send(response);
    }

}
