package com.ski.wca.monitor;

import org.apache.log4j.Logger;

import com.ski.wca.WechatInterface;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjJsonMessage;
import fomjar.util.FjLoopTask;

public class TokenMonitor extends FjLoopTask {
    
    private static TokenMonitor instance = null;
    public static TokenMonitor getInstance() {
        if (null == instance) instance = new TokenMonitor();
        
        return instance;
    }
    
    private static final Logger logger = Logger.getLogger(TokenMonitor.class);
    
    private String token;
    
    private TokenMonitor() {}
    
    public void setServerName() {}
    
    public void start() {
        if (isRun()) {
            logger.warn("token-monitor has already started");
            return;
        }
        new Thread(this, "token-monitor").start();
    }
    
    @Override
    public void perform() {
        token = null;
        FjJsonMessage token_msg = WechatInterface.token(FjServerToolkit.getServerConfig("wca.appid"), FjServerToolkit.getServerConfig("wca.secret"));
        if (null == token_msg || !token_msg.json().containsKey("access_token") || !token_msg.json().containsKey("expires_in")) {
            logger.error("get wechat access token failed: " + token_msg);
            resetInterval();
            return;
        }
        token = token_msg.json().getString("access_token");
        logger.info("get wechat access token successfully: " + token_msg);
        setInterval(token_msg.json().getInt("expires_in"));
    }
    
    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("taobao.order.proc-interval"));
        setInterval(second);
    }
    
    @Override
    public void setInterval(long second) {
        logger.debug("will try again after " + second + " seconds");
        super.setInterval(second * 1000);
    }
    
    public String token() {
        return token;
    }
}
