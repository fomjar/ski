package com.ski.game.monitor;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.util.FjLoopTask;

public class OrderMonitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(OrderMonitor.class);
    
    private String serverName;
    
    public OrderMonitor(String serverName) {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("taobao.order.proc-interval"));
        setDelay(second * 1000);
        this.serverName = serverName;
    }
    
    public void start() {
        if (isRun()) {
            logger.warn("order-monitor has already started");
            return;
        }
        new Thread(this, "order-monitor").start();
    }

    @Override
    public void perform() {
        resetInterval();
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",   serverName);
        req.json().put("ts",   "wa");
        req.json().put("inst", SkiCommon.ISIS.INST_ECOM_QUERY_ORDER);
        req.json().put("args", String.format("{'user':'%s','pass':'%s'}", FjServerToolkit.getServerConfig("taobao.account.user"), FjServerToolkit.getServerConfig("taobao.account.pass")));
        FjServerToolkit.getSender(serverName).send(req);
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
}
