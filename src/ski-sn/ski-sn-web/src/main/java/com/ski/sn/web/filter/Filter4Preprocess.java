package com.ski.sn.web.filter;

import java.nio.channels.SocketChannel;

import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class Filter4Preprocess extends FjWebFilter {

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if ("/".equals(request.path())) {
            redirect(response, "/index.html");
            return false;
        }
        return true;
    }

}
