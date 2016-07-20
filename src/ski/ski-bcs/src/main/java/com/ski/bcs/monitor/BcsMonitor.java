package com.ski.bcs.monitor;

import com.ski.common.CommonService;

import fomjar.server.FjServerToolkit;
import fomjar.util.FjLoopTask;

public class BcsMonitor extends FjLoopTask {
    
    public BcsMonitor() {
        setInterval(1000L * 3600);
    }

    @Override
    public void perform() {
        resetInterval();
        
        CommonService.updateGame();
        CommonService.updateGameAccount();
        CommonService.updateGameAccountGame();
        CommonService.updateGameAccountRent();
        
        CommonService.updateChannelAccount();
        CommonService.updatePlatformAccount();
        CommonService.updatePlatformAccountMap();
        CommonService.updateOrder();
        
        CommonService.getChannelAccountAll().values().forEach(user->{
            CommonService.getOrderByCaid(user.i_caid).forEach(o->{
                o.commodities.values().forEach(c->{
                    
                });
            });
        });
    }
    
    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("bcs.monitor.statement.proc-interval"));
        setInterval(second * 1000);
    }
}
