package com.ski.talk.web.filter;

import java.nio.channels.SocketChannel;

import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class Filter6Interface extends FjWebFilter {

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if (!"/ski-web".equals(request.path())) return false;
        
        return true;
    }

}
