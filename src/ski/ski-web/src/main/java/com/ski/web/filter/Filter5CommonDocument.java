package com.ski.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.common.CommonService;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class Filter5CommonDocument extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter5CommonDocument.class);

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        FjWebFilter.documentRoot(FjServerToolkit.getServerConfig("web.document.root"));
        
        if (request.path().endsWith(".html")) {
            if (request.cookie().containsKey("user")) {
                int user = Integer.parseInt(request.cookie().get("user"), 16);
                if (0 == CommonService.getChannelAccountByCaid(user).c_phone.length()
                        && !request.path().equals("/wechat/update_platform_account_map.html")) {
                    redirect(response, "/wechat/update_platform_account_map.html");
                    return false;
                }
            }
        }

        logger.info("user access document: " + request.url());
        document(response, request.path());
        return true;
    }

}
