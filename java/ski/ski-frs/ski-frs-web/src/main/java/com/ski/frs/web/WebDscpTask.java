package com.ski.frs.web;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ski.frs.isis.ISIS;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjISIS;
import fomjar.util.FjThreadFactory;
import net.sf.json.JSONObject;

public class WebDscpTask implements FjServerTask {
    
    private ExecutorService pool_file;
    
    @Override
    public void initialize(FjServer server) {
        pool_file = Executors.newCachedThreadPool(new FjThreadFactory("web-pool-file"));
    }

    @Override
    public void destroy(FjServer server) {
        if (null != pool_file) pool_file.shutdownNow();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) return;
        
        FjDscpMessage dmsg = (FjDscpMessage) msg;
        switch (dmsg.inst()) {
        case ISIS.INST_SET_PIC:
            processSetPic(server, dmsg);
            break;
        default:
            if (!dmsg.fs().startsWith("bcs")) {
                FjServerToolkit.dscpRequest("bcs", dmsg.sid(), dmsg.ttl() - 1, dmsg.inst(), dmsg.argsToJsonObject());
                waitSessionForResponse(server, dmsg);
            }
            break;
        }
    }
    
    private static void waitSessionForResponse(FjServer server, FjDscpMessage req) {
        server.onDscpSession(req.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage rsp = (FjDscpMessage) wrapper.message();
                FjServerToolkit.dscpResponse(req, FjServerToolkit.dscpResponseCode(rsp), FjServerToolkit.dscpResponseDesc(rsp));
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
    }
    
    private void processSetPic(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        
        pool_file.submit(()->{
            JSONObject json = WebToolkit.processSetPic(args);
            if (FjISIS.CODE_SUCCESS != json.getInt("code")) {
                FjServerToolkit.dscpResponse(dmsg, json.getInt("code"), json.get("desc"));
                return;
            }
            
            FjServerToolkit.dscpRequest("bcs", dmsg.sid(), dmsg.ttl() - 1, dmsg.inst(), json.getJSONObject("desc"));
            waitSessionForResponse(server, dmsg);
        });
    }

}
