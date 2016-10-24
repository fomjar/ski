package com.ski.sn.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class Filter5Document extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter5Document.class);
    
    public Filter5Document() {documentRoot("document/");}

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if ("/ski-web".equals(request.path())) return true;
        logger.info(String.format("[ DOCUMENT  ] - %s - %s", request.url(), request.contentToString().replace("\n", "")));
        
        document(response, request.path());
        response.attr().put("Content-Encoding", "gzip");
        return true;
    }

}
