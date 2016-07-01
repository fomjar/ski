package com.ski.wca;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.wca.biz.WcaBusiness;
import com.ski.wca.monitor.DataMonitor;
import com.ski.wca.monitor.MenuMonitor;
import com.ski.wca.monitor.TokenMonitor;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;

public class WcaTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(WcaTask.class);
    
    public WcaTask() {
        DataMonitor.getInstance().start();
        TokenMonitor.getInstance().start();
        MenuMonitor.getInstance().start();
    }
    
    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (msg instanceof FjHttpRequest) {
            FjHttpRequest hmsg = (FjHttpRequest) msg;
            if (hmsg.url().startsWith("/ski-wca")) {
                logger.debug("dispatch message from wechat server: " + wrapper.message());
                processWechat(server.name(), wrapper);
            } else logger.error("unsupported http message:\n" + wrapper.attachment("raw"));
        } else if (msg instanceof FjDscpMessage) {
            logger.debug("dispatch message from ski server: " + msg);
            WcaBusiness.getInstance().dispatch((FjDscpMessage) msg);
        } else {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
        }
    }
    
    private void processWechat(String server, FjMessageWrapper wrapper) {
        if (((FjHttpRequest) wrapper.message()).urlParameters().containsKey("echostr")) {
            WechatInterface.access(wrapper);
            logger.info("wechat access");
            return;
        }
        // 第一时间给微信响应
        WechatInterface.sendResponse("success", (SocketChannel) wrapper.attachment("conn"));
        
        FjDscpMessage req = WechatInterface.convertRequest(server, (FjHttpRequest) wrapper.message());
        WcaBusiness.getInstance().dispatch(req);
    }
}
