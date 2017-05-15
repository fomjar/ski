package com.ski.frs.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.frs.web.WebToolkit;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjISIS;
import fomjar.server.web.FjWebFilter;
import net.sf.json.JSONObject;

public class Filter6Interface extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter6Interface.class);
    
    public Filter6Interface() {}
    
    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn, FjServer server) {
        if (!"/ski-web".equals(request.path())) return true;
        
        JSONObject args = request.argsToJson();
        if (!args.has("inst")) {
            String desc = "illegal arguments, no inst";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_INST, desc);
            return false;
        }
        
        int inst = WebToolkit.getIntFromArgs(args, "inst");
        args.remove("inst");
        logger.info(String.format("[ INTERFACE ] %s - 0x%08X", request.url(), inst));
        
        switch (inst) {
        default: {
            FjDscpMessage req_bcs = FjServerToolkit.dscpRequest("bcs", inst, args);
            waitSessionForResponse(server, response, req_bcs.sid());
            break;
        }
        }
        
        return true;
    }
    
    private static void waitSessionForResponse(FjServer server, FjHttpResponse response, String sid) {
        server.onDscpSession(sid, new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage dmsg = (FjDscpMessage) wrapper.message();
                response(response, FjServerToolkit.dscpResponseCode(dmsg), FjServerToolkit.dscpResponseDesc(dmsg));
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
        responseWait(response);
    }
    
    private static void responseWait(FjHttpResponse response) {
        synchronized (response) {
            try {response.wait();}
            catch (InterruptedException e) {e.printStackTrace();}
        }
    }
    
    private static void response(FjHttpResponse response, int code, Object desc) {
        JSONObject args = new JSONObject();
        args.put("code", code);
        args.put("desc", desc);
        response.content(args);
        response.attr().put("Content-Type", "application/json");
        response.attr().put("Content-Encoding", "gzip");
        synchronized (response) {response.notifyAll();}
    }
}
