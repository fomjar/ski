package com.ski.vcg.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.vcg.web.wechat.WechatBusiness;
import com.ski.vcg.web.wechat.WechatInterface;

import fomjar.server.FjServer;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;

public class Filter2WechatCommand extends FjWebFilter {

    private static final Logger logger = Logger.getLogger(Filter2WechatCommand.class);

    public WechatBusiness wechat;

    public Filter2WechatCommand(WechatBusiness wechat) {
        this.wechat = wechat;
    }

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn, FjServer server) {
        if (request.url().startsWith("/ski-wechat")) {
            logger.info("wechat user command: " + request.url());
            // 第一时间给微信响应
            WechatInterface.sendResponse(new FjHttpResponse(null, 200, null, "success"), conn);

            FjDscpMessage dmsg = WechatInterface.customConvertRequest(request);
            wechat.dispatch(dmsg);
            return false;
        }
        return true;
    }

}
