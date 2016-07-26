package com.ski.wsi;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjSender;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjJsonMessage;
import fomjar.util.FjLoopTask;
import net.sf.json.JSONObject;

public class WsiTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(WsiTask.class);
    
    private Map<String, CacheConn> cache;
    
    public WsiTask() {
        cache = new HashMap<String, CacheConn>();
        new Thread(new CacheMonitor(), "cache").start();
    }
    
    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (msg instanceof FjHttpRequest) {
            FjHttpRequest hmsg = (FjHttpRequest) msg;
            if (hmsg.url().startsWith("/ski-wsi")) process(server.name(), wrapper);
            else logger.error("unsupported http message:\n" + wrapper.attachment("raw"));
        } else if (msg instanceof FjDscpMessage) {
            if (!cache.containsKey(((FjDscpMessage) msg).sid())) {
                logger.warn("no request cached for this response: " + msg);
                return;
            }
            responseDscpMessage((FjDscpMessage) msg);
        } else {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
        }
    }
    
    private void process(String server, FjMessageWrapper wrapper) {
        FjHttpRequest req = (FjHttpRequest) wrapper.message();
        SocketChannel conn = (SocketChannel) wrapper.attachment("conn");
        JSONObject args = req.argsToJson();
        
        if (!args.has("inst")) {
            logger.error("bad request: " + req);
            responseSimple( CommonDefinition.CODE.CODE_SYS_ILLEGAL_INST, "没有指令", conn);
            return;
        }
        
        String report   = args.containsKey("report") ? args.remove("report").toString() : FjServerToolkit.getServerConfig("wsi.report");
        int    inst     = -1;
        Object instobj  = args.remove("inst");
        if (instobj instanceof Integer) inst = ((Integer) instobj).intValue();
        else {
            try {inst = Integer.parseInt(instobj.toString(), 16);}
            catch (NumberFormatException e) {
                logger.error("bad request: " + req);
                responseSimple(CommonDefinition.CODE.CODE_SYS_ILLEGAL_INST, "非法指令", conn);
                return;
            }
        }
        
        logger.info(String.format("[ REPORT ] %s:0x%08X", report, inst));
        
        FjDscpMessage newreq = new FjDscpMessage();
        newreq.json().put("fs", server);
        newreq.json().put("ts", report);
        if (args.has("sid")) newreq.json().put("sid",  args.remove("sid").toString());
        newreq.json().put("inst", inst);
        newreq.json().put("args", JSONObject.fromObject(args));
        
        synchronized (cache) {cache.put(newreq.sid(), new CacheConn(conn));}
        wrapper.attach("conn", null); // 清除连接缓存 防止被服务器自动释放
        
        // 请求上报业务
        FjServerToolkit.getAnySender().send(newreq);
        
        logger.debug(newreq);
    }
    
    private void responseDscpMessage(FjDscpMessage rsp) {
        logger.info(String.format("[RESPONSE] %s:0x%08X", rsp.fs(), rsp.inst()));
        synchronized (cache) {
            SocketChannel conn = (SocketChannel) cache.remove(rsp.sid()).conn;
            FjSender.sendHttpResponse(new FjHttpResponse(FjHttpResponse.CT_JSON, rsp.toString()), conn);
        }
    }
    
    private static void responseSimple(int code, String desc, SocketChannel conn) {
        logger.info(String.format("[RESPONSE] 0x%08X:%s", code, desc));
        JSONObject args = new JSONObject();
        args.put("code", code);
        args.put("desc", desc);
        FjServerToolkit.getAnySender().send(new FjMessageWrapper(new FjJsonMessage(args)).attach("conn", conn));
    }
    
    private class CacheMonitor extends FjLoopTask {
        
        private static final long INTEVAL = 1000L * 1;
        private static final long TIMEOUT = 1000L * 60 * 3;
        
        public CacheMonitor() {
            setDelay(INTEVAL);
            setInterval(INTEVAL);
        }
        
        @Override
        public void perform() {
            synchronized (cache) {
                List<String> toremove = new LinkedList<String>();
                cache.forEach((sid, cc)->{
                    long time = System.currentTimeMillis() - cc.timestamp;
                    if (time >= TIMEOUT) {
                        logger.error("remove cache: " + sid + " for timeout: " + time);
                        toremove.add(sid);
                    }
                });
                toremove.forEach(sid->{
                    try {cache.remove(sid).conn.close();}
                    catch (Exception e) {e.printStackTrace();}
                });
            }
        }
    }
    
    private static class CacheConn {
        
        public long timestamp;
        public SocketChannel conn;
        
        public CacheConn(SocketChannel conn) {
            timestamp = System.currentTimeMillis();
            this.conn = conn;
        }
    }
    
}
