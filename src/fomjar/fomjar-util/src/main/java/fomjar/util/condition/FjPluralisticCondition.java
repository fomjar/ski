package fomjar.util.condition;

public abstract class FjPluralisticCondition implements FjCondition {
    
    protected FjCondition[] conditions;
    
    public FjPluralisticCondition() {}
    public FjPluralisticCondition(FjCondition... conditions) {setConditions(conditions);}
    
    public void setConditions(FjCondition... conditions) {
        this.conditions = conditions;
    }
    
}
