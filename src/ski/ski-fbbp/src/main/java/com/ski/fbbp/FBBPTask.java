package com.ski.fbbp;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ski.common.DSCP;
import com.ski.fbbp.monitor.OrderMonitor;
import com.ski.fbbp.session.SessionReturn;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjMessage;
import fomjar.server.session.FjSessionController;
import fomjar.server.session.FjSessionNotOpenException;

public class FBBPTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(FBBPTask.class);
    
    private List<FjSessionController> scs;
    private FjSessionController       scReturn;
    
    public FBBPTask(FjServer server) {
        scs = new LinkedList<FjSessionController>();
        scs.add(scReturn = new SessionReturn());
        new OrderMonitor(server.name()).start();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (msg instanceof FjDscpMessage) {
            FjDscpMessage dmsg = (FjDscpMessage) msg;
            try {FjSessionController.dispatch(server, scs, dmsg);} // 通用会话消息
            catch (FjSessionNotOpenException e) {
                // 新会话开始
                if (DSCP.CMD.ECOM_APPLY_RETURN == dmsg.cmd()
                        || DSCP.CMD.ECOM_SPECIFY_RETURN == dmsg.cmd()) { // 退货
                    scReturn.openSession(dmsg.sid());
                }
                try {FjSessionController.dispatch(server, scs, dmsg);}
                catch (FjSessionNotOpenException e1) {logger.error("dispatch failed for message: " + msg, e1);}
            }
        } else {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
        }
    }

}
