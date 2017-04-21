package fomjar.util.condition;

@SuppressWarnings("unchecked")
public class FjConditionOr<T> extends FjPluralisticCondition<T> {

    public FjConditionOr() {}
    public FjConditionOr(FjCondition<T>... conditions) {setConditions(conditions);}

    @Override
    public Object apply(T t) {
        for (FjCondition<T> c : conditions) {
            if ((boolean) c.apply(t)) return true;
        }
        return false;
    }

}
