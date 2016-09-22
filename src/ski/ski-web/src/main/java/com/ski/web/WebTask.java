package com.ski.web;

import org.apache.log4j.Logger;

import com.ski.web.filter.Filter1WechatAccess;
import com.ski.web.filter.Filter2WechatCommand;
import com.ski.web.filter.Filter3WechatAuthorize;
import com.ski.web.filter.Filter4CommonPreprocess;
import com.ski.web.filter.Filter5CommonDocument;
import com.ski.web.filter.Filter6CommonInterface;
import com.ski.web.wechat.WechatBusiness;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.web.FjWebTask;

public class WebTask extends FjWebTask {
    
    private static final Logger logger = Logger.getLogger(WebTask.class);
    
    private DataMonitor     mon_data;
    private WechatBusiness  wechat;

    @Override
    public void initialize(FjServer server) {
        super.initialize(server);
        
        mon_data = new DataMonitor();
        wechat = new WechatBusiness();
        
        mon_data.start();
        wechat.open();
        
        registerFilter(new Filter1WechatAccess());
        registerFilter(new Filter2WechatCommand(wechat));
        registerFilter(new Filter3WechatAuthorize());
        registerFilter(new Filter4CommonPreprocess());
        registerFilter(new Filter5CommonDocument());
        registerFilter(new Filter6CommonInterface(wechat));
    }
    

    @Override
    public void destroy(FjServer server) {
        super.destroy(server);
        
        mon_data.close();
        wechat.close();
    }



    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (msg instanceof FjHttpRequest) {
            super.onMessage(server, wrapper);
        } else if (msg instanceof FjDscpMessage) {
            wechat.dispatch((FjDscpMessage) msg);
        } else {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
        }
    }

}
