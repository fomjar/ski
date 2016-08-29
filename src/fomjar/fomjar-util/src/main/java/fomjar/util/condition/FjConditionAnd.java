package fomjar.util.condition;

public class FjConditionAnd extends FjPluralisticCondition {
    
    public FjConditionAnd() {}
    public FjConditionAnd(FjCondition... conditions) {setConditions(conditions);}

    @Override
    public boolean apply() {
        for (FjCondition c : conditions) {
            if (!c.apply()) return false;
        }
        return true;
    }

}
