package com.ski.sn.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.sn.common.CommonDefinition;
import com.ski.sn.common.CommonService;

import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;
import net.sf.json.JSONObject;

public class Filter6Interface extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter6Interface.class);

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if (!"/ski-web".equals(request.path())) return true;
        
        logger.info(String.format("[ INTERFACE ] - %s", request.url()));
        
        JSONObject args = request.argsToJson();
        if (!args.has("inst")) {
            logger.error("illegal arguments, no inst: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_ILLEGAL_INST);
            args_rsp.put("desc", "指令错误");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return false;
        }
        
        int inst = FilterToolkit.getIntFromArgs(args, "inst");
        args.remove("inst");
        
        if (request.cookie().containsKey("uid"))    args.put("uid",     Integer.parseInt(request.cookie().get("uid")));
        if (request.cookie().containsKey("token"))  args.put("token",   request.cookie().get("token"));
        
        FjDscpMessage rsp = CommonService.requests("bcs", inst, args);
        response.attr().put("Content-Type", "application/json");
        response.attr().put("Content-Encoding", "gzip");
        response.content(rsp.args());
        return true;
    }

}
