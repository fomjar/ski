package fomjar.server.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.msg.FjDscpMessage;
import fomjar.util.FjLoopTask;

public class FjSessionGraph {
    
    private static final Logger logger = Logger.getLogger(FjSessionGraph.class);
    
    private Map<String,  FjSessionPath>     paths;
    private Map<String,  FjSessionContext>  contexts;
    private Set<FjSessionNode>              nodes;
    private Map<Integer, FjSessionNode>     heads;
    private FjSessionTask[]     mismatch;
    private FjSessionMonitor    monitor;
    
    public FjSessionGraph() {
        paths       = new HashMap<String, FjSessionPath>();
        contexts    = new HashMap<String, FjSessionContext>();
        monitor     = new FjSessionMonitor();
        new Thread(monitor, "fjsession-monitor").start();
    }
    
    public FjSessionNode createHeadNode(int inst, FjSessionTask task) {
        FjSessionNode head = createNode(inst, task);
        if (null == heads) heads = new HashMap<Integer, FjSessionNode>();
        heads.put(inst, head);
        return head;
    }
    
    public void setMismatchTask(FjSessionTask... mismatch) {this.mismatch = mismatch;}
    
    public FjSessionNode createNode(int inst, FjSessionTask task) {
        FjSessionNode node = new FjSessionNode(inst, task);
        if (null == nodes) nodes = new HashSet<FjSessionNode>();
        nodes.add(node);
        return node;
    }
    
    public FjSessionNode getHeadNode(int inst) {
        if (null == heads) return null;
        return heads.get(inst);
    }
    
    public Map<Integer, FjSessionNode> getHeadNode() {return heads;}
    
    public Set<FjSessionNode> getNode() {return nodes;}
    
    public void dispatch(FjServer server, FjMessageWrapper wrapper) {
        if (!(wrapper.message() instanceof FjDscpMessage)) {
            logger.warn("can not dispatch non dscp message: " + wrapper.message());
            return;
        }
        
        
        FjDscpMessage       msg     = (FjDscpMessage) wrapper.message();
        String              sid     = msg.sid();
        int                 inst    = msg.inst();
        FjSessionContext    context = null;
        FjSessionPath       path    = null;
        FjSessionNode       node    = null;
        
        if (!isOpened(sid)) open(sid);
        
        path    = paths.get(msg.sid());
        context = contexts.get(msg.sid());
        
        if (path.isEmpty()) node = getHeadNode(inst);
        else                node = path.getLast().getNext(inst);
        
        if (null == node) {
            logger.error(String.format("message mismatch graph, sid=%s, inst=0x%08X", sid, inst));
            if (!path.isEmpty()) {
                logger.warn("try dispatch again");
                close(sid);
                dispatch(server, wrapper);
            } else {
                logger.warn("do mismatch process");
                if (null != mismatch) {
                    for (FjSessionTask task : mismatch) {
                        try {task.onSession(context, path, wrapper);}
                        catch (Exception e) {logger.error("on mismatch session failed for message: " + msg, e);}
                    }
                }
            }
            return;
        }
        
        context.prepare(server, msg);
        path.append(node);
        
        boolean isSuccess = false;
        try {isSuccess = node.getTask().onSession(context, path, wrapper);}
        catch (Exception e) {logger.error("on session failed for message: " + msg, e);}
        
        if (isSuccess) if (!node.hasNext()) close(sid);
        else {
            logger.error("on session failed for message: " + msg);
            path.removeLast();
        }

    }
    
    public boolean isOpened(String sid) {return contexts.containsKey(sid);}
    
    private FjSessionContext open(String sid) {
        logger.info(String.format("session open: %s", sid));
        
        if (!paths.containsKey(sid))
            synchronized(paths) {paths.put(sid, new FjSessionPath(this, sid));}
        if (!contexts.containsKey(sid)) {
            synchronized(contexts) {
                FjSessionContext context = new FjSessionContext(sid);
                context.put("time.open", System.currentTimeMillis());
                contexts.put(sid, context);
            }
        }
        return contexts.get(sid);
    }
    
    public FjSessionContext close(String sid) {
        logger.info(String.format("session close: sid=%s, path=%s", sid, paths.get(sid)));

        if (paths.containsKey(sid))
            synchronized(paths)     {paths.remove(sid);}
        if (contexts.containsKey(sid)) {
            synchronized(contexts)   {
                FjSessionContext context = contexts.remove(sid);
                context.put("time.close", System.currentTimeMillis());
                return context;
            }
        }
        
        return null;
    }
    
    public FjSessionMonitor getMonitor() {return monitor;}
    
    public class FjSessionMonitor extends FjLoopTask {
        
        private static final long INTERVAL = 1000L * 60 * 1;
        
        private long timeout;
        
        public FjSessionMonitor() {
            super(INTERVAL, INTERVAL);
            timeout  = 1000L * 60 * 10;
        }
        
        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        @Override
        public void perform() {
            List<String> toremove = null;
            synchronized(contexts) {
                toremove = contexts.entrySet()
                        .stream()
                        .filter((entry)->{
                            FjSessionContext context = entry.getValue();
                            return timeout <= System.currentTimeMillis() - context.getLong("time.open"); 
                        }).map((entry)->{
                            return entry.getKey();
                        }).collect(Collectors.toList());
            }
            if (null != toremove) {
                for (String sid : toremove) {
                    logger.warn("session timeout: " + sid);
                    FjSessionGraph.this.close(sid);
                }
            }
        }
        
    }
}
