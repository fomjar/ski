package com.ski.frs.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;
import com.ski.frs.web.CacheResponse;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjISIS;
import fomjar.server.web.FjWebFilter;
import net.sf.json.JSONObject;

public class Filter6Interface extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter6Interface.class);

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if (!"/ski-web".equals(request.path())) return true;
        
        JSONObject args = request.argsToJson();
        if (!args.has("inst")) {
            logger.error("illegal arguments, no inst: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", FjISIS.CODE_ILLEGAL_INST);
            args_rsp.put("desc", "非法指令");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return false;
        }
        
        int inst = FilterToolkit.getIntFromArgs(args, "inst");
        args.remove("inst");
        logger.info(String.format("[ INTERFACE ] - %s - 0x%08X", request.url(), inst));
        
        boolean result = true;
        switch (inst) {
        case ISIS.INST_QUERY_PIC_BY_FV:
            result = processQueryPicByFV(args);
            break;
        }
        
        if (result) {   // success
            FjDscpMessage req = FjServerToolkit.dscpRequest("bcs", inst, args);
            CacheResponse.getInstance().cacheWait(req.sid(), response);
        } else {    // fail
            logger.error("pre process fail: " + args);
            response.content(args);
        }
        
        return true;
    }
    
    private static boolean processQueryPicByFV(JSONObject args) {
        int     code = FjISIS.CODE_SUCCESS;
        String  desc = null;
        
        if (!args.has("pic")) {
            code = FjISIS.CODE_ILLEGAL_ARGS;
            desc = "illegal arguments, no pic";
            logger.error(desc + ", " + args);
            return false;
        }
        
        String pic = args.getString("pic");
        args.remove("pic");
        
        // TODO: convert pic to fv
        
        if (code != FjISIS.CODE_SUCCESS) {
            args.clear();
            args.put("code", code);
            args.put("desc", desc);
        }
        return code == FjISIS.CODE_SUCCESS;
    }
    
    public static void main(String[] args) {
        System.out.println(FaceInterface.init(FaceInterface.DEVICE_GPU));
        System.out.println(FaceInterface.free());
    }
}
