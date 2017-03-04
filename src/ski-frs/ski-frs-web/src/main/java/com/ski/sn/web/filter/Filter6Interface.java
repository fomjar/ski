package com.ski.sn.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

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
        
        FjDscpMessage req = FjServerToolkit.dscpRequest("bcs", inst, args);
        CacheResponse.getInstance().cacheWait(req.sid(), response);
        
        return true;
    }
}
