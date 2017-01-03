package com.ski.xs.wca;

import com.ski.xs.wca.filter.Filter1WechatAccess;
import com.ski.xs.wca.filter.Filter2WechatCommand;
import com.ski.xs.wca.filter.Filter3WechatAuthorize;
import com.ski.xs.wca.filter.b.WechatBusiness;

import fomjar.server.FjServer;
import fomjar.server.web.FjWebTask;

public class WcaTask extends FjWebTask {
    
    private WechatBusiness  wechat;

    @Override
    public void initialize(FjServer server) {
        super.initialize(server);
        
        wechat = new WechatBusiness();
        wechat.open();

        registerFilter(new Filter1WechatAccess());
        registerFilter(new Filter2WechatCommand(wechat));
        registerFilter(new Filter3WechatAuthorize());
    }

    @Override
    public void destroy(FjServer server) {
        super.destroy(server);
    }

}
