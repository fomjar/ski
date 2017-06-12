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
        if (!args.has("data") || !args.has("name") || !args.has("type") || !args.has("size")) {
            String desc = "illegal arguments, no data, name, type, size";
            logger.error(desc);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        pool_file.submit(()->{
            String name = args.getString("name");
            String data = args.getString("data");
            int    type = args.getInt("type");
            int    size = args.getInt("size");
            args.remove("data");
            
            if (ISIS.FIELD_TYPE_MAN == type && ISIS.FIELD_PIC_SIZE_SMALL == size) {
                FeatureService.getDefault().fv_base64(new FeatureService.FV() {
                    @Override
                    public void fv(int mark, double[] fv, int glass, int mask, int hat, int gender, int nation) {
                        args.put("mark",    mark);
                        args.put("fv",      fv);
                        args.put("glass",   glass);
                        args.put("mask",    mask);
                        args.put("hat",     hat);
                        args.put("gender",  gender);
                        args.put("nation",  nation);
                    }
                }, data);
                if (FeatureService.SUCCESS != args.getInt("mark")) {
                    String desc = "illegal picture, mark=" + args.getInt("mark");
                    logger.error(desc);
                    FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, desc);
                    return;
                }
            }
            
            String path = null;
            if (args.has("did")) {
                path = "document" + FjServerToolkit.getServerConfig("web.pic.dev")
                        + "/" + args.getString("did").replace("/", "_").replace("\\", "_")
                        + "/" + name + ".jpg";
            } else if (args.has("sid") && args.has("siid")) {
                path = "document" + FjServerToolkit.getServerConfig("web.pic.sub")
                        + "/" + args.getString("sid")
                        + "/" + args.getString("siid")
                        + "/" + name + ".jpg";
            } else {
                String desc = "illegal arguments, no did or sid, siid";
                logger.error(desc);
                FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, desc);
                return;
            }
            args.put("path", path.substring("document".length()));
            try {
                WebToolkit.writeFileBase64Image(data, path);
            } catch (IOException e) {
                String desc = "internal error, write base64 image file failed: " + path;
                logger.error(desc);
                FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_INTERNAL_ERROR, desc);
                return;
            }
            
            FjServerToolkit.dscpRequest("bcs", dmsg.sid(), dmsg.ttl() - 1, dmsg.inst(), args);
            waitSessionForResponse(server, dmsg);
        });
    }

}
