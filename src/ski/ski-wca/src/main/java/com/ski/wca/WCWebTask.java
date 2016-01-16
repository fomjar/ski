package com.ski.wca;

import java.nio.channels.SocketChannel;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjSender;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjMessage;

public class WCWebTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(WCWebTask.class);
    
    private String wca;
    
    public WCWebTask(String wca) {
        this.wca = wca;
    }
    
    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (msg instanceof FjHttpRequest) {
            FjHttpRequest hmsg = (FjHttpRequest) msg;
            if (hmsg.url().startsWith("/wcweb")) process(server.name(), wrapper);
            else logger.error("unsupported http message: " + hmsg.url());
        } else {
            logger.error("unsupported format message: " + msg);
        }
    }
    
    private void process(String serverName, FjMessageWrapper wrapper) {
        FjHttpRequest hmsg = (FjHttpRequest) wrapper.message();
        SocketChannel conn = (SocketChannel) wrapper.attachment("conn");
        JSONObject arg = JSONObject.fromObject(hmsg.urlParameters());
        
        if (!arg.containsKey("cmd") && !arg.containsKey("user")) {
            logger.error("bad request: " + hmsg.url());
            FjSender.sendHttpResponse(new FjHttpResponse(String.format(FjServerToolkit.getServerConfig("wca.web.error"), "非法参数")), conn);
            return;
        }
        
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",  wca); // 以wca的名义
        req.json().put("ts",  FjServerToolkit.getServerConfig("wca.report"));
        req.json().put("sid", arg.getString("user"));
        try{req.json().put("cmd", Integer.parseInt(arg.getString("cmd"), 16));}
        catch (NumberFormatException e) {
            logger.error("bad request: " + hmsg.url());
            FjSender.sendHttpResponse(new FjHttpResponse(String.format(FjServerToolkit.getServerConfig("wca.web.error"), "非法指令")), conn);
            return;
        }
        req.json().put("arg", arg);
        wrapper.attach("conn", null); // 清除连接缓存 防止被服务器自动释放
        // 请求上报业务
        FjServerToolkit.getSender(serverName).send(new FjMessageWrapper(req).attach("observer", new FjSender.FjSenderObserver() {
            @Override
            public void onSuccess() {FjSender.sendHttpResponse(new FjHttpResponse(FjServerToolkit.getServerConfig("wca.web.accept")), conn);}
            @Override
            public void onFail() {FjSender.sendHttpResponse(new FjHttpResponse(String.format(FjServerToolkit.getServerConfig("wca.web.error"), "请求失败")), conn);}
        }));
    }
    
}
