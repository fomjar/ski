package com.ski.wca;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.ski.wca.biz.WcWeb;
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
import fomjar.server.msg.FjHttpResponse;

public class WcaTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(WcaTask.class);
    private ExecutorService pool;
    
    public WcaTask() {
        new DataMonitor().start();
        TokenMonitor.getInstance().start();
        new MenuMonitor().start();
        pool = Executors.newCachedThreadPool();
    }
    
    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (msg instanceof FjHttpRequest) {
            processWechat(server.name(), wrapper);
        } else if (msg instanceof FjDscpMessage) {
            logger.debug("dispatch ski message: " + msg);
            WcaBusiness.dispatch(server.name(), (FjDscpMessage) msg);
        } else {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
        }
    }
    
    private void processWechat(String server, FjMessageWrapper wrapper) {
        FjHttpRequest hmsg = (FjHttpRequest) wrapper.message();
        if (hmsg.url().startsWith(WcaBusiness.URL_KEY)) {
            logger.debug("dispatch wechat message: " + wrapper.message());
            if (((FjHttpRequest) wrapper.message()).urlArgs().containsKey("echostr")) {
                WechatInterface.access(wrapper);
                logger.info("wechat access");
                return;
            }
            // 第一时间给微信响应
            WechatInterface.sendResponse(new FjHttpResponse(null, 200, null, "success"), (SocketChannel) wrapper.attachment("conn"));
            
            FjDscpMessage dmsg = WechatInterface.customConvertRequest(server, (FjHttpRequest) wrapper.message());
            WcaBusiness.dispatch(server, dmsg);
        } else if (hmsg.url().startsWith(WcWeb.URL_KEY)) {
            logger.debug("dispatch wechat web message: " + wrapper.message());
            final SocketChannel conn = (SocketChannel) wrapper.attachment("conn");
            wrapper.attach("conn", null); // give up connection
            pool.submit(()->{
                try {WcWeb.dispatch(server, hmsg, conn);}
                catch (Exception e) {logger.error("error occurs when dispatch web message: " + hmsg, e);}
                finally {try {conn.close();} catch (IOException e) {e.printStackTrace();}}
            });
        } else logger.error("unsupported http message:\n" + wrapper.attachment("raw"));
    }
}

  