package fomjar.util.condition;

public class FjConditionOr extends FjPluralisticCondition {
    
    public FjConditionOr() {}
    public FjConditionOr(FjCondition... conditions) {setConditions(conditions);}

    @Override
    public Object apply() {
        for (FjCondition c : conditions) {
            if ((boolean) c.apply()) return true;
        }
        return false;
    }

}
