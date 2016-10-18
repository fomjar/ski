package com.ski.bcs.monitor;

import org.apache.log4j.Logger;

import com.ski.common.CommonService;

import fomjar.server.FjServerToolkit;
import fomjar.util.FjLoopTask;

public class DataMonitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(DataMonitor.class);
    
    public void start() {
        if (isRun()) {
            logger.warn("monitor-data has already started");
            return;
        }
        new Thread(this, "monitor-data").start();
    }
    
    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("bcs.monitor.data.interval"));
        setInterval(second * 1000);
    }
    
    @Override
    public void perform() {
        resetInterval();
        
        CommonService.updateGame();
        CommonService.updateGameAccount();
        CommonService.updateGameAccountGame();
        CommonService.updateGameAccountRent();
        CommonService.updateGameRentPrice();
        CommonService.updateChannelAccount();
        CommonService.updatePlatformAccount();
        CommonService.updatePlatformAccountMap();
        CommonService.updateOrder();
        CommonService.updateNotification();
    }

}
