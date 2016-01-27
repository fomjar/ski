package com.ski.fbbp.monitor;

import org.apache.log4j.Logger;

import com.ski.common.DSCP;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.util.FjLoopTask;

public class OrderMonitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(OrderMonitor.class);
    
    private String serverName;
    
    public OrderMonitor(String serverName) {
        long time = Long.parseLong(FjServerToolkit.getServerConfig("taobao.order.proc-interval"));
        time *= 1000L;
        setDelay(time);
        setInterval(time);
        this.serverName = serverName;
    }

    @Override
    public void perform() {
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",  serverName);
        req.json().put("ts",  "wa");
        req.json().put("cmd", DSCP.CMD.ECOM_APPLY_ORDER);
        req.json().put("arg", String.format("{'user':'%s','pass':'%s'}", FjServerToolkit.getServerConfig("taobao.account.user"), FjServerToolkit.getServerConfig("taobao.account.pass")));
        FjServerToolkit.getSender(serverName).send(req);
    }
    
    public void start() {
        if (isRun()) {
            logger.warn("order-monitor has already started");
            return;
        }
        new Thread(this, "order-monitor").start();
    }
}
