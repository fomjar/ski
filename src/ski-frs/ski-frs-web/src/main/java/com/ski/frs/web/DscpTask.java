package com.ski.frs.web;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpResponse;
import net.sf.json.JSONObject;

public class DscpTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(DscpTask.class);

    @Override
    public void initialize(FjServer server) {}

    @Override
    public void destroy(FjServer server) {}

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
    
    private static void processUpdatePic(FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        String data_ori = args.remove("data").toString();
        
        FjServerToolkit.dscpRequest("bcs", dmsg.sid(), ISIS.INST_UPDATE_PIC, args);
        
        byte[] data = Base64.getDecoder().decode(data_ori);
        File file = new File(FjServerToolkit.getServerConfig("web.pic") + "/" + args.getString("name"));
        try {writeFile(data, file);}
        catch (IOException e) {
            logger.error("write picture file failed: " + file, e);
            return;
        }
    }
    
    private static void writeFile(byte[] data, File file) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bos.write(data);
        bos.flush();
        bos.close();
    }

}
