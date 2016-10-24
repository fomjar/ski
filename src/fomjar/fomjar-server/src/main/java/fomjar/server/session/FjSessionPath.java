package fomjar.server.session;

import java.util.LinkedList;
import java.util.List;

public class FjSessionPath {

    private final FjSessionGraph        graph;
    private LinkedList<FjSessionNode>   nodes;

    FjSessionPath(FjSessionGraph graph, String sid) {
        this.graph = graph;
    }

    void append(FjSessionNode node) {
        if (null == nodes) nodes = new LinkedList<FjSessionNode>();
        nodes.add(node);
    }

    FjSessionNode removeLast() {
        if (null == nodes || nodes.isEmpty()) return null;

        return nodes.removeLast();
    }

    public FjSessionNode getFirst() {
        if (null == nodes || nodes.isEmpty()) return null;
        return nodes.getFirst();
    }

    public FjSessionNode getLast() {
        if (null == nodes || nodes.isEmpty()) return null;
        return nodes.getLast();
    }

    public FjSessionNode getCurrent() {return getLast();}

    public FjSessionNode get(int index) {
        if (null == nodes || nodes.isEmpty()) return null;
        return nodes.get(index);
    }

    public List<FjSessionNode> get() {return nodes;}

    public boolean isEmpty() {return null == nodes || nodes.isEmpty();}

    public FjSessionGraph getGraph() {return graph;}

    @Override
    public String toString() {
        if (null == nodes || nodes.isEmpty()) return null;

        StringBuffer sb = new StringBuffer();
        for (FjSessionNode node : nodes) {
            String s = String.format("0x%08X", node.getInst());
            if (0 == sb.length()) sb.append(s);
            else sb.append("->" + s);
        }
        return sb.toString();
    }

}
