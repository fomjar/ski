package com.ski.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class Filter5CommonDocument extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter5CommonDocument.class);

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if (!request.path().equals(Filter6CommonInterface.URL_KEY)) {
            logger.info("user access document: " + request.url());
            FjWebFilter.documentRoot(FjServerToolkit.getServerConfig("web.document.root"));
            document(response, request.path());
            if (response.contentType().startsWith("text")
                    || response.contentType().startsWith("application")) response.attr().put("Content-Encoding", "gzip");
        }
        return true;
    }

}
