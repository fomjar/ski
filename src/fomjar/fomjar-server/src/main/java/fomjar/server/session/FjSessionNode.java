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

    public int              getInst() {return inst;}
    public FjSessionTask    getTask() {return task;}
    void setInst(int inst) {this.inst = inst;}

    public FjSessionNode getPrev(int inst) {
        if (null == node_prev) return null;
        return node_prev.get(inst);
    }

    public Map<Integer, FjSessionNode> getPrev() {return node_prev;}

    public FjSessionNode getNext(int inst) {
        if (null == node_next) return null;
        return node_next.get(inst);
    }

    public Map<Integer, FjSessionNode> getNext() {return node_next;}

    public FjSessionNode addPrev(FjSessionNode... prevs) {
        if (null == prevs || 0 == prevs.length) throw new NullPointerException();

        if (null == this.node_prev) this.node_prev = new HashMap<Integer, FjSessionNode>();
        for (FjSessionNode prev : prevs) {
            this.node_prev.put(prev.getInst(), prev);
            if (null == prev.node_next) prev.node_next = new HashMap<Integer, FjSessionNode>();
            prev.node_next.put(this.getInst(), this);
        }

        return this;
    }

    public FjSessionNode addNext(FjSessionNode... nexts) {
        if (null == nexts || 0 == nexts.length) throw new NullPointerException();

        if (null == this.node_next) this.node_next = new HashMap<Integer, FjSessionNode>();
        for (FjSessionNode next : nexts) {
            this.node_next.put(next.getInst(), next);
            if (null == next.node_prev) next.node_prev = new HashMap<Integer, FjSessionNode>();
            next.node_prev.put(this.getInst(), this);
        }

        return this;
    }

    public boolean hasPrev() {return null != node_prev && !node_prev.isEmpty();}
    public boolean hasNext() {return null != node_next && !node_next.isEmpty();}

    public FjSessionNode append(FjSessionNode next) {
        addNext(next);
        return next;
    }

}