package com.ski.wca;

import java.nio.channels.SocketChannel;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjSender;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;

public class WcWebTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(WcWebTask.class);
    
    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (msg instanceof FjHttpRequest) {
            FjHttpRequest hmsg = (FjHttpRequest) msg;
            if (hmsg.url().startsWith("/" + server.name())) process(server.name(), wrapper);
            else logger.error("unsupported http message:\n" + wrapper.attachment("raw"));
        } else {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
        }
    }
    
    private void process(String serverName, FjMessageWrapper wrapper) {
        FjHttpRequest       hmsg = (FjHttpRequest) wrapper.message();
        final SocketChannel conn = (SocketChannel) wrapper.attachment("conn");
        Map<String, String> args  = hmsg.urlParameters();
        
        if (null == args || args.isEmpty()) {
            logger.error("bad request: " + hmsg.url());
            FjSender.sendHttpResponse(new FjHttpResponse(String.format(FjServerToolkit.getServerConfig("wcweb.error"), "非法参数")), conn);
            return;
        }
        
        if (!args.containsKey("inst") && !args.containsKey("user")) {
            logger.error("bad request: " + hmsg.url());
            FjSender.sendHttpResponse(new FjHttpResponse(String.format(FjServerToolkit.getServerConfig("wcweb.error"), "非法参数")), conn);
            return;
        }
        
        String insthex = args.get("inst");
        while (8 > insthex.length()) insthex = "0" + insthex;
        logger.info(String.format("INST_USER_INSTRUCTION - wechat:%s:0x%s", args.get("user"), insthex));
        
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",  serverName);
        req.json().put("ts",  "wca");
        req.json().put("sid", args.get("user"));
        try{req.json().put("inst", Integer.parseInt(args.get("inst"), 16));}
        catch (NumberFormatException e) {
            logger.error("bad request: " + hmsg.url());
            FjSender.sendHttpResponse(new FjHttpResponse(String.format(FjServerToolkit.getServerConfig("wcweb.error"), "非法指令")), conn);
            return;
        }
        req.json().put("args", JSONObject.fromObject(args));
        wrapper.attach("conn", null); // 清除连接缓存 防止被服务器自动释放
        // 请求上报业务
        FjServerToolkit.getSender(serverName).send(new FjMessageWrapper(req).attach("observer", new FjSender.FjSenderObserver() {
            @Override
            public void onSuccess() {FjSender.sendHttpResponse(new FjHttpResponse(FjServerToolkit.getServerConfig("wcweb.accept")), conn);}
            @Override
            public void onFail() {FjSender.sendHttpResponse(new FjHttpResponse(String.format(FjServerToolkit.getServerConfig("wcweb.error"), "请求失败")), conn);}
        }));
    }
    
}
