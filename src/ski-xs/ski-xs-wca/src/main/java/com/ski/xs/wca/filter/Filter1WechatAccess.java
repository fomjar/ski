package com.ski.xs.wca.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.xs.wca.filter.b.WechatInterface;

import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class Filter1WechatAccess extends FjWebFilter {

    private static final Logger logger = Logger.getLogger(Filter1WechatAccess.class);

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if (request.url().startsWith("/ski-wechat") && request.urlArgs().containsKey("echostr")) {
            logger.info("wechat access: " + request.url());
            WechatInterface.access(conn, request);
            return false;
        }
        return true;
    }

}
