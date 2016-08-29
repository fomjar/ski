package fomjar.util.condition;

import java.util.LinkedList;
import java.util.List;

public class FjExpression implements FjCondition {
    
    private static final String CONDITION_AND   = "and";
    private static final String CONDITION_OR    = "or";
    
    private List<Entry> entries;
    
    public FjExpression() {
        entries = new LinkedList<Entry>();
    }
    
    public FjExpression and(FjCondition condition) {
        entries.add(new Entry(CONDITION_AND, condition));
        return this;
    }
    
    public FjExpression or(FjCondition condition) {
        entries.add(new Entry(CONDITION_OR, condition));
        return this;
    }

    @Override
    public boolean apply() {
        FjCondition condition = null;
        for (Entry entry : entries) {
            if (null == condition) condition = entry.cond;
            else {
                switch (entry.oper) {
                case CONDITION_AND: condition = new FjConditionAnd  (condition, entry.cond);    break;
                case CONDITION_OR:  condition = new FjConditionOr   (condition, entry.cond);    break;
                }
            }
        }
        if (null == condition) return false;
        return condition.apply();
    }
    
    private static class Entry {
        public String       oper;
        public FjCondition  cond;
        
        public Entry(String oper, FjCondition cond) {
            this.oper = oper;
            this.cond = cond;
        }
    }

}
