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
    private Map<String, FjSessionPath>      paths;
    
    public FjSessionGraph() {
        paths = new HashMap<String, FjSessionPath>();
    }
    
    public FjSessionNode createNode(int inst, FjSessionTask task) {
        FjSessionNode node = new FjSessionNode(inst, task);
        if (null == nodes) nodes = new HashSet<FjSessionNode>();
        nodes.add(node);
        return node;
    }
    
    public void prepare() {
        if (null == nodes || nodes.isEmpty()) throw new IllegalStateException("graph has no node");
        
        heads = nodes.stream()
                .filter((node)->{return !node.hasPrev();})
                .collect(Collectors.toMap((node)->{return node.getInst();}, (node)->{return node;}));
        
        if (null == heads || heads.isEmpty()) throw new IllegalStateException("no head node for graph");
    }
    
    public Map<Integer, FjSessionNode> getHeads() {return heads;}
    
    public void dispatch(FjServer server, FjMessageWrapper wrapper) {
        if (!(wrapper.message() instanceof FjDscpMessage)) {
            logger.warn("can not dispatch non dscp message: " + wrapper.message());
            return;
        }
        
        FjDscpMessage msg  = (FjDscpMessage) wrapper.message();
        FjSessionPath path = paths.get(msg.sid());
        FjSessionNode curr = null;
        // old
        if (null != path) curr = path.getLast().getNext(msg.inst());
        // new
        else {
            curr = heads.get(msg.inst());
            if (null == curr) { // not match
                logger.error("message not match this graph: " + msg);
                return;
            }
            // match
            path = new FjSessionPath(this, msg.sid());
            paths.put(msg.sid(), path);
        }
        path.append(curr);
        path.context().prepare(server, msg);
        // execute task
        try {curr.getTask().onSession(path, wrapper);}
        catch (Exception e) {logger.error("error occurs when process session for message: " + msg, e);}
        // no next, end
        if (!curr.hasNext()) path.close();
    }
    
    void close(String sid) {
        if (null == paths || !paths.containsKey(sid)) {
            logger.error("session path not opened: " + sid);
            return;
        }
        paths.remove(sid);
    }
    
}
