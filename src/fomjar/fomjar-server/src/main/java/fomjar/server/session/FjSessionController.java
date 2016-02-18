package fomjar.server.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fomjar.server.FjServer;
import fomjar.server.msg.FjDscpMessage;

/**
 * 会话控制器。负责会话的打开、关闭以及分发
 * 
 * @author fomjar
 */
public abstract class FjSessionController {
    
    private static final Logger logger = Logger.getLogger(FjSessionController.class);
    
    /**
     * <p>
     * 分发新的消息至符合的会话控制器中，多个会话控制器符合则均会处理此消息。
     * </p>
     * <p>
     * 如果没有会话控制器能够匹配到此消息，则抛出{@link FjSessionNotMatchException}异常。
     * </p>
     * 
     * @param server 消息来源服务器
     * @param scs 待匹配的会话控制器
     * @param msg 待分发的消息
     * @throws FjSessionNotMatchException 如果没有会话控制器能够匹配此消息
     */
    public static void dispatch(FjServer server, List<FjSessionController> scs, FjDscpMessage msg) throws FjSessionNotMatchException {
        if (null == scs || scs.isEmpty()) throw new FjSessionNotMatchException(msg.sid());
        
        if (0 == scs.stream()
                .filter((sc)->{return sc.match(msg);})
                .count()) {
            throw new FjSessionNotMatchException(msg.sid());
        }
        
        scs.stream()
                .filter ((sc)->{return sc.match(msg);})
                .forEach((sc)->{
                    if (!sc.containSession(msg.sid())) sc.openSession(msg.sid());
                    FjSCB scb = sc.getSession(msg.sid());
                    scb.prepare(msg);
                    try {sc.onSession(server, scb, msg);}
                    catch (Exception e) {logger.error("error occurs when process session for message: " + msg, e);}
                    if (scb.isEnd()) sc.closeSession(msg.sid());
                });
    }
    
    private Map<String, FjSCB> scbs;
    private FjSessionMonitor   monitor;
    
    /**
     * 创建一个空白的会话控制器
     */
    public FjSessionController() {
        scbs = new HashMap<String, FjSCB>();
        monitor = new FjSessionMonitor(scbs);
        monitor.start();
    }

    private boolean match(FjDscpMessage msg) {
        if (containSession(msg.sid())) return true;
        return matchFirst(msg);
    }
    
    private boolean containSession(String sid) {return scbs.containsKey(sid);}
    
    private FjSCB getSession(String sid) {return scbs.get(sid);}
    
    private FjSCB openSession(String sid) {
        if (scbs.containsKey("sid")) {
            logger.error("session already open: " + sid);
            return scbs.get(sid);
        }
        logger.info("session open: " + sid);
        FjSCB scb = new FjSCB(sid);
        scb.put("time.open", System.currentTimeMillis());
        synchronized (scbs) {scbs.put(sid, scb);}
        return scb;
    }
    
    private FjSCB closeSession(String sid) {
        if (!scbs.containsKey(sid)) {
            logger.error("session not found: " + sid);
            return null;
        }
        logger.info("session close: " + sid);
        FjSCB scb = null;
        synchronized (scbs) {scb = scbs.remove(sid);}
        scb.put("time.close", System.currentTimeMillis());
        return scb;
    }

    public FjSessionMonitor getMonitor() {return monitor;}
    
    /**
     *  匹配会话的第一个消息需要此处指定
     * 
     * @param msg
     * @return true表示此消息是会话的第一个消息，false表示不是
     */
    protected abstract boolean matchFirst(FjDscpMessage msg);
    
    /**
     * 处理具体的会话
     * 
     * @param server
     * @param scb
     * @param msg
     */
    protected abstract void onSession(FjServer server, FjSCB scb, FjDscpMessage msg);
    
}
