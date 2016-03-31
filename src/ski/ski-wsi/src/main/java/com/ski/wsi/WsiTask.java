package com.ski.wsi;

import java.nio.channels.SocketChannel;
import java.util.Map;

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
import net.sf.json.JSONObject;

public class WsiTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(WsiTask.class);
    
    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (msg instanceof FjHttpRequest) {
            FjHttpRequest hmsg = (FjHttpRequest) msg;
            if (hmsg.url().startsWith("/ski-wsi")) process(server.name(), wrapper);
            else logger.error("unsupported http message:\n" + wrapper.attachment("raw"));
        } else {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
        }
    }
    
    private void process(String serverName, FjMessageWrapper wrapper) {
        FjHttpRequest hmsg = (FjHttpRequest) wrapper.message();
        final SocketChannel conn = (SocketChannel) wrapper.attachment("conn");
        Map<String, String> args = hmsg.urlParameters();
        
        if (null == args || !args.containsKey("sid") || !args.containsKey("inst")) {
            logger.error("bad request: " + hmsg.url());
            FjSender.sendHttpResponse(new FjHttpResponse(String.format(FjServerToolkit.getServerConfig("wsi.error"), "非法参数")), conn);
            return;
        }
        
        String sid  = args.remove("sid");
        int    inst = -1;
        try {inst = Integer.parseInt(args.remove("inst"), 16);}
        catch (NumberFormatException e) {
            logger.error("bad request: " + hmsg.url());
            FjSender.sendHttpResponse(new FjHttpResponse(String.format(FjServerToolkit.getServerConfig("wsi.error"), "非法指令")), conn);
            return;
        }
        
        logger.info(String.format("INST_USER_INSTRUCTION - wechat:%s:0x%08X", sid, inst));
        
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",   serverName);
        req.json().put("ts",   FjServerToolkit.getServerConfig("wsi.report"));
        req.json().put("sid",  sid);
        req.json().put("inst", inst);
        req.json().put("args", JSONObject.fromObject(args));
        wrapper.attach("conn", null); // 清除连接缓存 防止被服务器自动释放
        // 请求上报业务
        FjServerToolkit.getSender(serverName).send(new FjMessageWrapper(req).attach("observer", new FjSender.FjSenderObserver() {
            @Override
            public void onSuccess() {FjSender.sendHttpResponse(new FjHttpResponse(FjServerToolkit.getServerConfig("wsi.accept")), conn);}
            @Override
            public void onFail() {FjSender.sendHttpResponse(new FjHttpResponse(String.format(FjServerToolkit.getServerConfig("wsi.error"), "请求失败")), conn);}
        }));
    }
    
}
