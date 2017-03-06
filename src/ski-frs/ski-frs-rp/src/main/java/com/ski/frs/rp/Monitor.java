package com.ski.frs.rp;

import org.apache.log4j.Logger;

import fomjar.util.FjLoopTask;

public class Monitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(Monitor.class);
    
    public Monitor() {
        setDelay(1000L);
    }

    @Override
    public void perform() {
        logger.debug("monitor begin");
        
        
        
        logger.debug("monitor end");
    }
    
    public void open() {
        if (isRun()) {
            logger.warn("monitor already opened");
            return;
        }
        
        new Thread(this, "rp-monitor").start();
    }

}
