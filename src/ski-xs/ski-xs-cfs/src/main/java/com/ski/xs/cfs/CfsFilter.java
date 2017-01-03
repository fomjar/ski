package com.ski.xs.cfs;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class CfsFilter extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(CfsFilter.class);

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if ("/ski-cfs".equals(request.path())) return true;
        
        try {logger.debug(String.format("access from %25s - %s", conn.getRemoteAddress().toString(), request.url()));}
        catch (IOException e) {logger.error("get remote address failed");}
        
        if (!document(response, request.path())) {
            response.code(404);
        }
        response.attr().put("Content-Encoding", "gzip");
        return true;
    }

}
