package fomjar.server.session;

import java.util.HashMap;
import java.util.Map;

public class FjSessionNode {
    
    private int inst;
    private FjSessionTask task;
    private Map<Integer, FjSessionNode> node_prev;
    private Map<Integer, FjSessionNode> node_next;
    
    FjSessionNode(int inst, FjSessionTask task) {
        if (null == task) throw new NullPointerException("session task is null");
        
        this.inst = inst;
        this.task = task;
    }
    
    public int              inst() {return inst;}
    public FjSessionTask    task() {return task;}
    
    public FjSessionNode prev(int inst) {
        if (null == node_prev) return null;
        return node_prev.get(inst);
    }
    
    public Map<Integer, FjSessionNode> prev() {return node_prev;}
    
    public FjSessionNode next(int inst) {
        if (null == node_next) return null;
        return node_next.get(inst);
    }
    
    public Map<Integer, FjSessionNode> next() {return node_next;}
    
    public void addPrev(FjSessionNode prev) {
        if (null == this.node_prev) this.node_prev = new HashMap<Integer, FjSessionNode>();
        this.node_prev.put(prev.inst(), prev);
        if (null == prev.node_next) prev.node_next = new HashMap<Integer, FjSessionNode>();
        prev.node_next.put(this.inst(), this);
    }
    
    public void addNext(FjSessionNode next) {
        if (null == this.node_next) this.node_next = new HashMap<Integer, FjSessionNode>();
        this.node_next.put(next.inst(), next);
        if (null == next.node_prev) next.node_prev = new HashMap<Integer, FjSessionNode>();
        next.node_prev.put(this.inst(), this);
    }
    
    public boolean hasPrev() {return null != node_prev && !node_prev.isEmpty();}
    public boolean hasNext() {return null != node_next && !node_next.isEmpty();}
    
    public FjSessionNode append(FjSessionNode next) {
        addNext(next);
        return next;
    }
    
}