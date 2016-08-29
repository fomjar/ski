package fomjar.util.condition;

public class FjConditionOr extends FjPluralisticCondition {
    
    public FjConditionOr() {}
    public FjConditionOr(FjCondition... conditions) {setConditions(conditions);}

    @Override
    public boolean apply() {
        for (FjCondition c : conditions) {
            if (c.apply()) return true;
        }
        return false;
    }

}
