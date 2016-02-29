package fomjar.server.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.msg.FjDscpMessage;

public class FjSessionGraph {
    
    private static final Logger logger = Logger.getLogger(FjSessionGraph.class);
    
    private Set<FjSessionNode>              nodes;
    private Map<Integer, FjSessionNode>     heads;
    private Map<String, FjSessionContext>   contexts;
    private Map<String, FjSessionNode>      node_cur;
    
    public FjSessionGraph() {
        contexts  = new HashMap<String, FjSessionContext>();
        node_cur = new HashMap<String, FjSessionNode>();
    }
    
    public FjSessionNode createNode(int inst, FjSessionTask task) {
        FjSessionNode n = new FjSessionNode(inst, task);
        if (null == nodes) nodes = new HashSet<FjSessionNode>();
        nodes.add(n);
        return n;
    }
    
    public void prepare() {
        if (null == nodes || nodes.isEmpty()) throw new IllegalStateException("graph has no node");
        
        heads = nodes.stream()
                .filter((node)->{return !node.hasPrev();})
                .collect(Collectors.toMap((node)->{return node.inst();}, (node)->{return node;}));
        
        if (null == heads || heads.isEmpty()) throw new IllegalStateException("no head node for graph");
    }
    
    public Map<Integer, FjSessionNode> heads() {return heads;}
    
    public void dispatch(FjServer server, FjMessageWrapper wrapper) {
        if (!(wrapper.message() instanceof FjDscpMessage)) {
            logger.warn("can not dispatch non dscp message: " + wrapper.message());
            return;
        }
        
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        FjSessionNode curr = node_cur.get(msg.sid());
        FjSessionNode next = null;
        // 可能的头节点
        if (null == curr) next = heads.get(msg.sid());
        // 可能的非头节点
        else next = curr.next(msg.inst());
        
        if (null != next) { // 匹配到了
            FjSessionContext context = contexts.get(msg.sid());
            if (null == context) context = openSession(msg.sid()); // 会话流程打开
            
            context.prepare(msg);
            try {next.task().onSession(server, context, wrapper);}
            catch (Exception e) {logger.error("error occurs when process session for message: " + msg, e);}
            
            if (!next.hasNext()) { // 会话流程结束
                closeSession(msg.sid()); // 会话流程关闭
                node_cur.remove(msg.sid());
            } else { // 会话还有流程继续
                node_cur.put(msg.sid(), next);
            }
        } else { // 没有匹配到节点
            // logger.error("message does not match this graph: " + msg);
        }
    }
    
    private FjSessionContext openSession(String sid) {
        if (contexts.containsKey("sid")) {
            logger.error("session already open: " + sid);
            return contexts.get(sid);
        }
        logger.info("session open: " + sid);
        FjSessionContext context = new FjSessionContext(sid);
        context.put("time.open", System.currentTimeMillis());
        synchronized (contexts) {contexts.put(sid, context);}
        return context;
    }
    
    private FjSessionContext closeSession(String sid) {
        if (!contexts.containsKey(sid)) {
            logger.error("session not found: " + sid);
            return null;
        }
        logger.info("session close: " + sid);
        FjSessionContext context = null;
        synchronized (contexts) {context = contexts.remove(sid);}
        context.put("time.close", System.currentTimeMillis());
        return context;
    }
    
}
