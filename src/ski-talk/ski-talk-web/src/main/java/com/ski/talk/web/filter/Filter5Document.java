package com.ski.talk.web.filter;

import java.nio.channels.SocketChannel;

import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class Filter5Document extends FjWebFilter {
    
    public Filter5Document() {documentRoot("document/");}

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if ("/ski-web".equals(request.path())) return true;
        
        document(response, request.path());
        if (response.contentType().startsWith("text")
                || response.contentType().startsWith("application"))
            response.attr().put("Content-Encoding", "gzip");
        return true;
    }

}
