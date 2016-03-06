package fomjar.server.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.msg.FjDscpMessage;

public class FjSessionGraph {
    
    private static final Logger logger = Logger.getLogger(FjSessionGraph.class);
    
    private Set<FjSessionNode>          nodes;
    private Map<Integer, FjSessionNode> heads;
    private Map<String,  FjSessionPath> paths;
    
    public FjSessionGraph() {
        paths = new HashMap<String, FjSessionPath>();
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
        
        FjDscpMessage msg  = (FjDscpMessage) wrapper.message();
        FjSessionPath path = paths.get(msg.sid());
        FjSessionNode curr = null;
        // match old
        if (null != path) curr = path.getLast().getNext(msg.inst());
        // match new
        else curr = getHeadNode(msg.inst());
        // not match
        if (null == curr) {
            logger.error("message not match this graph: " + msg);
            if (null != path) {
                logger.error("close path: " + msg.sid());
                path.close();
            }
            return;
        }
        // new path
        if (null == path) path = new FjSessionPath(this, msg.sid());
        path.context().prepare(server, msg);
        boolean isSuccess = false;
        // execute task
        try {isSuccess = curr.getTask().onSession(path, wrapper);}
        catch (Exception e) {logger.error("error occurs when process session for message: " + msg, e);}
        // infer result
        if (isSuccess) {
            if (!paths.containsKey(msg.sid())) paths.put(msg.sid(), path);
            path.append(curr);
            // no next, end
            if (!curr.hasNext()) path.close();
        } else logger.error("on session failed for message: " + msg);
    }
    
    void closePath(String sid) {
        if (null == paths || !paths.containsKey(sid)) {
            logger.error("session path not opened: " + sid);
            return;
        }
        paths.remove(sid);
    }
    
}
