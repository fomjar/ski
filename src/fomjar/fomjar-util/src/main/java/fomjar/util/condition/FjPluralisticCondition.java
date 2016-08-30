package fomjar.util.condition;

@SuppressWarnings("unchecked")
public abstract class FjPluralisticCondition<T> implements FjCondition<T> {
    
    protected FjCondition<T>[] conditions;
    
    public FjPluralisticCondition() {}
    public FjPluralisticCondition(FjCondition<T>... conditions) {setConditions(conditions);}
    
    public void setConditions(FjCondition<T>... conditions) {
        this.conditions = conditions;
    }
    
}
