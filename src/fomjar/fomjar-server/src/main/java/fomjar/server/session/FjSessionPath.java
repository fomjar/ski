package fomjar.server.session;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class FjSessionPath {
    
    private static final Logger logger = Logger.getLogger(FjSessionPath.class);

    private final FjSessionGraph        graph;
    private LinkedList<FjSessionNode>   nodes;
    private FjSessionContext            context;
    
    FjSessionPath(FjSessionGraph graph, String sid) {
        this.graph = graph;
        
        open(sid);
    }
    
    void append(FjSessionNode node) {
        if (null == nodes) nodes = new LinkedList<FjSessionNode>();
        nodes.add(node);
    }
    
    public FjSessionNode getFirst() {
        if (null == nodes || nodes.isEmpty()) return null;
        return nodes.getFirst();
    }
    
    public FjSessionNode getLast() {
        if (null == nodes || nodes.isEmpty()) return null;
        return nodes.getLast();
    }
    
    public FjSessionNode get(int index) {
        if (null == nodes || nodes.isEmpty()) return null;
        return nodes.get(index);
    }
    
    public List<FjSessionNode> get() {return nodes;}
    
    public FjSessionContext context() {return context;}
    
    public String sid() {return context().sid();}
    
    private void open(String sid) {
        logger.info("session open: " + sid);
        context = new FjSessionContext(sid);
        context.put("time.open", System.currentTimeMillis());
    }
    
    public FjSessionContext close() {
        if (null == context) {
            logger.error("session never opend");
            return null;
        }
        if (context.has("time.close")) {
            logger.error("session already closed: " + context.sid());
            return context;
        }
        logger.info("session close: " + context.sid());
        context.put("time.close", System.currentTimeMillis());
        graph.closePath(context.sid());
        return context;
    }
    
}
