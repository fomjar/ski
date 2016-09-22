package com.ski.web;

import org.apache.log4j.Logger;

import com.ski.common.CommonService;

import fomjar.server.FjServerToolkit;
import fomjar.util.FjLoopTask;

public class DataMonitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(DataMonitor.class);
    
    private boolean isNeverLoad = true;
    
    public void start() {
        if (isRun()) {
            logger.warn("monitor-data has already started");
            return;
        }
        new Thread(this, "monitor-data").start();
    }
    
    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("web.data.reload-interval"));
        setInterval(second * 1000);
    }
    
    @Override
    public void perform() {
        resetInterval();
        
        CommonService.updateGameAccount();
        CommonService.updateGameAccountGame();
        CommonService.updateGameAccountRent();
        CommonService.updateGameRentPrice();
        CommonService.updateChannelAccount();
        CommonService.updatePlatformAccount();
        CommonService.updatePlatformAccountMap();
        CommonService.updateOrder();
        CommonService.updateChatroom();
        CommonService.updateChatroomMember();
        CommonService.updateChatroomMessage();
        
        boolean swich = "on".equalsIgnoreCase(FjServerToolkit.getServerConfig("web.data.reload-switch"));
        if (isNeverLoad || swich) {
            CommonService.updateGame();
            CommonService.updateTag();
            String osn = FjServerToolkit.getServerConfig("web.cc.osn");
            if (null == osn) CommonService.updateChannelCommodity();
            else CommonService.updateChannelCommodity(Integer.parseInt(osn));
            isNeverLoad = false;
        }
    }


}
