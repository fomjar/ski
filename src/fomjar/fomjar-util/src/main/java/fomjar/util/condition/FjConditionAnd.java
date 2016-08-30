package fomjar.util.condition;

public class FjConditionAnd extends FjPluralisticCondition {
    
    public FjConditionAnd() {}
    public FjConditionAnd(FjCondition... conditions) {setConditions(conditions);}

    @Override
    public Object apply() {
        for (FjCondition c : conditions) {
            if (!(boolean) c.apply()) return false;
        }
        return true;
    }

}
