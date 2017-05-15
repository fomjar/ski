package com.ski.frs.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class Filter5Document extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter5Document.class);
    
    public Filter5Document() {documentRoot(FjServerToolkit.getServerConfig("web.doc"));}

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn, FjServer server) {
        if ("/ski-web".equals(request.path())) return true;
        
        logger.info(String.format("[ DOCUMENT  ] %s - %s", request.url(), request.contentToString().replace("\n", "")));
        
        if ("/".equals(request.path())) {
            redirect(response, "/index.html");
            return false;
        }
        response.attr().put("Content-Encoding", "gzip");
        if (!document(response, request.path())) {
            response.code(404);
            document(response, "/404.html");
        }
        return false;
    }

}
