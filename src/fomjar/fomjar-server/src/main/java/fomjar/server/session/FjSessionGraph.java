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

    private Map<String,  FjSessionContext>  contexts;
    private Map<String,  FjSessionPath>     paths;
    private Set<FjSessionNode>              nodes;
    private Map<Integer, FjSessionNode>     heads;
    private FjSessionTask       task_mismatch;
    private FjSessionTask       task_timeout;
    private FjSessionMonitor    monitor;

    public FjSessionGraph() {
        contexts    = new HashMap<String, FjSessionContext>();
        paths       = new HashMap<String, FjSessionPath>();
        monitor     = new FjSessionMonitor();
    }

    public void open() {
        if (monitor.isRun()) {
            logger.warn("session graph already opened");
            return;
        }
        new Thread(monitor, "fjsession-monitor").start();
    }

    public void close() {
        if (!monitor.isRun()) {
            logger.warn("session graph never opened");
            return;
        }
        monitor.close();
    }

    public FjSessionNode createHeadNode(int inst, FjSessionTask task) {
        FjSessionNode head = createNode(inst, task);
        if (null == heads) heads = new HashMap<Integer, FjSessionNode>();
        heads.put(inst, head);
        return head;
    }

    public FjSessionNode createNode(int inst, FjSessionTask task) {
        FjSessionNode node = new FjSessionNode(inst, task);
        if (null == nodes) nodes = new HashSet<FjSessionNode>();
        nodes.add(node);
        return node;
    }

    public FjSessionNode getHead(int inst) {
        if (null == heads) return null;
        return heads.get(inst);
    }

    public Map<Integer, FjSessionNode> getHeadAll() {return heads;}

    public Set<FjSessionNode> getNodeAll() {return nodes;}

    public FjSessionContext getContext(String sid) {return contexts.get(sid);}

    public FjSessionPath getPath (String sid) {return paths.get(sid);}

    public void setMismatchTask(FjSessionTask task) {this.task_mismatch = task;}

    public void setTimeoutTask (FjSessionTask task) {this.task_timeout  = task;}

    public FjSessionMonitor getMonitor() {return monitor;}

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

        if (!isSessionOpened(sid)) openSession(sid);

        context = getContext(sid);
        path    = getPath(sid);

        if (path.isEmpty()) node = getHead(inst);
        else                node = path.getLast().getNext(inst);

        // prepare before execute session task
        context.prepSession(server, msg);

        // no any match
        if (null == node) {
            closeSession(sid);
            if (path.isEmpty()) { // not match on the head, it's really not match
                logger.debug(String.format("message mismatch graph, do mismatch process and then drop, sid=%s, inst=0x%08X", sid, inst));
                if (null != task_mismatch) {
                    try {task_mismatch.onSession(context, path, wrapper);}
                    catch (Exception e) {logger.error("on mismatch session failed for message: " + msg, e);}
                }
            } else { // not match on an existing path, will close existing session and try again from head
                logger.debug(String.format("message mismatch graph, try dispatch again, sid=%s, inst=0x%08X", sid, inst));
                dispatch(server, wrapper);
            }
            return;
        }

        path.append(node);

        // execute session task
        boolean isSuccess = false;
        try {isSuccess = node.getTask().onSession(context, path, wrapper);}
        catch (Exception e) {logger.error("on session failed for message: " + msg, e);}

        context.postSession(isSuccess);

        if (isSuccess) { // success, check if complete
            if (!node.hasNext()) closeSession(sid);
        } else { // fail, roll back
            logger.error("on session failed for message: " + msg);
            path.removeLast();
        }
    }

    private boolean isSessionOpened(String sid) {return contexts.containsKey(sid);}

    private void openSession(String sid) {
        logger.info(String.format("session open : %s", sid));

        if (!contexts.containsKey(sid)) {
            synchronized(contexts) {
                FjSessionContext context = new FjSessionContext(sid);
                context.put("time.open", System.currentTimeMillis());
                contexts.put(sid, context);
            }
        }
        if (!paths.containsKey(sid))
            synchronized(paths) {paths.put(sid, new FjSessionPath(this, sid));}
    }

    private void closeSession(String sid) {
        logger.info(String.format("session close: %s, path: %s", sid, paths.get(sid)));

        if (contexts.containsKey(sid)) {
            synchronized(contexts)   {
                FjSessionContext context = contexts.remove(sid);
                context.put("time.close", System.currentTimeMillis());
            }
        }
        if (paths.containsKey(sid))
            synchronized(paths)     {paths.remove(sid);}
    }

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
            List<String> toclose = null;
            synchronized(contexts) {
                toclose = contexts.entrySet()
                        .stream()
                        .filter(entry->timeout <= System.currentTimeMillis() - entry.getValue().getLong("time.open"))
                        .map(entry->entry.getKey())
                        .collect(Collectors.toList());
            }
            if (null != toclose) {
                for (String sid : toclose) {
                    logger.warn("session timeout: " + sid);
                    FjSessionContext context = getContext(sid);
                    FjSessionPath    path    = getPath(sid);
                    FjSessionGraph.this.closeSession(sid);

                    if (null != task_timeout) task_timeout.onSession(context, path, null);
                }
            }
        }

    }
}
