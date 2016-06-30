package com.ski.wca.monitor;

import org.apache.log4j.Logger;

import com.ski.common.CommonService;

import fomjar.server.FjServerToolkit;
import fomjar.util.FjLoopTask;

public class DataMonitor extends FjLoopTask {
    
    private static DataMonitor instance = null;
    public static DataMonitor getInstance() {
        if (null == instance) instance = new DataMonitor();
        return instance;
    }
    
    private static final Logger logger = Logger.getLogger(DataMonitor.class);
    
    private DataMonitor() {}
    
    public void start() {
        if (isRun()) {
            logger.warn("data-monitor has already started");
            return;
        }
        new Thread(this, "data-monitor").start();
    }
    
    @Override
    public void perform() {
        resetInterval();
        
        CommonService.updateChannelAccount();
    }
    
    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("wca.user.reload-interval"));
        setInterval(second * 1000);
    }

}
