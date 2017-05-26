package com.ski.frs.web;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

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
    
    private static final Logger logger = Logger.getLogger(WebDscpTask.class);
    
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
        case ISIS.INST_UPDATE_PIC:
            processUpdatePic(server, dmsg);
            break;
        default:
            if (!dmsg.fs().startsWith("bcs")) {
                FjServerToolkit.dscpRequest("bcs", dmsg.sid(), dmsg.inst(), dmsg.argsToJsonObject());
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
    
    private void processUpdatePic(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("did") || !args.has("data") || !args.has("name")) {
            String desc = "illegal arguments, no did, data, name";
            logger.error(desc);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        pool_file.submit(()->{
            String did = args.getString("did");
            String name = args.getString("name");
            String data = args.getString("data");
            args.remove("data");
            String path = "document" + FjServerToolkit.getServerConfig("web.pic.dev")
                    + "/" + did.replace("/", "_").replace("\\", "_")
                    + "/" + name + ".jpg";
            try {
                WebToolkit.writeFileBase64Image(data, path);
            } catch (IOException e) {
                String desc = "internal error, write base64 image file failed: " + path;
                logger.error(desc);
                FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_INTERNAL_ERROR, desc);
                return;
            }
            
            String fv = WebToolkit.fvLocalImage(path);
            args.put("fv", fv);
            
            FjServerToolkit.dscpRequest("bcs", dmsg.sid(), dmsg.inst(), args);
            waitSessionForResponse(server, dmsg);
        });
    }

}
