package fomjar.util.condition;

@SuppressWarnings("unchecked")
public class FjConditionAnd<T> extends FjPluralisticCondition<T> {
    
    public FjConditionAnd() {}
    public FjConditionAnd(FjCondition<T>... conditions) {setConditions(conditions);}

    @Override
    public Object apply(T t) {
        for (FjCondition<T> c : conditions) {
            if (!(boolean) c.apply(t)) return false;
        }
        return true;
    }

}
