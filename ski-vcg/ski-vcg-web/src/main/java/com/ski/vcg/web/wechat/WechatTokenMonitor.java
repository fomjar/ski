package com.ski.vcg.web.wechat;

import org.apache.log4j.Logger;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjJsonMessage;
import fomjar.util.FjLoopTask;

public class WechatTokenMonitor extends FjLoopTask {

    private static final Logger logger = Logger.getLogger(WechatTokenMonitor.class);

    private String token;
    private String ticket;

    public void start() {
        if (isRun()) {
            logger.warn("monitor-wechat-token has already started");
            return;
        }
        new Thread(this, "monitor-wechat-token").start();
    }

    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("web.wechat.token.reload-interval"));
        setInterval(second);
    }

    @Override
    public void setInterval(long second) {
        logger.debug("will try again after " + second + " seconds");
        super.setInterval(second * 1000);
    }

    @Override
    public void perform() {
        token   = null;
        ticket  = null;

        FjJsonMessage token_msg = WechatInterface.token(FjServerToolkit.getServerConfig("web.wechat.appid"), FjServerToolkit.getServerConfig("web.wechat.secret"));
        if (null == token_msg || !token_msg.json().containsKey("access_token") || !token_msg.json().containsKey("expires_in")) {
            logger.error("get wechat access token failed: " + token_msg);
            resetInterval();
            return;
        }
        token = token_msg.json().getString("access_token");
        logger.info("get wechat access token successfully: " + token_msg);

        FjJsonMessage ticket_msg = WechatInterface.ticket(token());
        if (null == ticket_msg || !ticket_msg.json().containsKey("ticket")) logger.error("get wechat jsapi ticket failed: " + ticket_msg);
        ticket = ticket_msg.json().getString("ticket");

        setInterval(token_msg.json().getInt("expires_in"));
    }


    public String token() {return token;}

    public String ticket() {return ticket;}
}
