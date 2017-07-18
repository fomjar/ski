package com.ski.frs.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import fomjar.server.FjServer;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class Filter1Authorize extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter1Authorize.class);
    
    public Filter1Authorize() {
        logger.getClass();
    }

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn, FjServer server) {
        return true;
    }

}
