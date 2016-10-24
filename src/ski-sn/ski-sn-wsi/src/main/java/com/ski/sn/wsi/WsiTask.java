package com.ski.sn.wsi;


import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.ski.sn.common.CommonDefinition;

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

    private ExecutorService         pool;
    private Map<String, CacheConn>  cache;
    private CacheMonitor            monitor;

    @Override
    public void initialize(FjServer server) {
        cache = new ConcurrentHashMap<String, CacheConn>();
        new Thread(monitor = new CacheMonitor(), "cache").start();
        pool = Executors.newCachedThreadPool();
    }

    @Override
    public void destroy(FjServer server) {
        cache.clear();
        monitor.close();
        pool.shutdownNow();
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
            responseSimple( CommonDefinition.CODE.CODE_ILLEGAL_INST, "没有指令", conn);
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
                responseSimple(CommonDefinition.CODE.CODE_ILLEGAL_INST, "非法指令", conn);
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

        cache.put(newreq.sid(), new CacheConn(conn));
        wrapper.attach("conn", null); // 清除连接缓存 防止被服务器自动释放

        // 请求上报业务
        FjServerToolkit.getAnySender().send(newreq);

        logger.debug(newreq);
    }

    private void responseDscpMessage(FjDscpMessage rsp) {
        logger.info(String.format("[RESPONSE] %s:0x%08X", rsp.fs(), rsp.inst()));
        SocketChannel conn = (SocketChannel) cache.remove(rsp.sid()).conn;
        pool.submit(()->{FjSender.sendHttpResponse(new FjHttpResponse(null, 200, "application/json", rsp), conn);});
    }

    private static void responseSimple(int code, String desc, SocketChannel conn) {
        logger.info(String.format("[RESPONSE] 0x%08X:%s", code, desc));
        JSONObject args = new JSONObject();
        args.put("code", code);
        args.put("desc", desc);
        FjServerToolkit.getAnySender().send(new FjMessageWrapper(new FjJsonMessage(args)).attach("conn", conn));
    }

    private class CacheMonitor extends FjLoopTask {

        private long timeout = 1000L * 60 * 3;

        public CacheMonitor() {
            long interval = 1000L * 60;
            setDelay(interval);
            setInterval(interval);
        }

        @Override
        public void perform() {
            try {
                setInterval(Integer.parseInt(FjServerToolkit.getServerConfig("wsi.cache.interval")) * 1000L);
                timeout = Integer.parseInt(FjServerToolkit.getServerConfig("wsi.cache.timeout")) * 1000L;
            } catch (Exception e) {e.printStackTrace();}

            List<String> toremove = new LinkedList<String>();
            cache.forEach((sid, cc)->{
                long time = System.currentTimeMillis() - cc.timestamp;
                if (time >= timeout) {
                    logger.error("remove cache: " + sid + " for timeout: " + time);
                    toremove.add(sid);
                }
            });
            toremove.forEach(sid->{
                try {cache.remove(sid).conn.close();}
                catch (Exception e) {e.printStackTrace();}
            });
            logger.error("current cache connections: " + cache.size());
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
