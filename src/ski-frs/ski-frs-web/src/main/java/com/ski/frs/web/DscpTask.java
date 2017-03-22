package com.ski.frs.web;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;
import com.ski.frs.web.mon.PictureMonitor;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpResponse;
import net.sf.json.JSONObject;

public class DscpTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(DscpTask.class);
    
    private PictureMonitor mon_pic = new PictureMonitor();
    
    @Override
    public void initialize(FjServer server) {
        mon_pic.open();
    }

    @Override
    public void destroy(FjServer server) {
        mon_pic.close();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) return;
        
        FjDscpMessage dmsg = (FjDscpMessage) msg;
        FjHttpResponse response = CacheResponse.getInstance().cache(dmsg.sid());
        if (null != response) responseHttp(response, dmsg);
        else requestDscp(dmsg);
    }
    
    private void responseHttp(FjHttpResponse response, FjDscpMessage dmsg) {
        logger.debug("response http: " + dmsg);
        
        response.attr().put("Content-Type", "application/json");
        response.attr().put("Content-Encoding", "gzip");
        response.content(dmsg.args());
        CacheResponse.getInstance().cacheNotify(dmsg.sid());
    }
    
    private void requestDscp(FjDscpMessage dmsg) {
        switch (dmsg.inst()) {
        case ISIS.INST_UPDATE_PIC:
            processUpdatePic(dmsg);
            break;
        }
    }
    
    private void processUpdatePic(FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        FjServerToolkit.dscpRequest("bcs", dmsg.sid(), ISIS.INST_UPDATE_PIC, args);
    }

}
