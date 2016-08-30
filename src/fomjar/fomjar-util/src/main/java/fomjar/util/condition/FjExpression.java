package fomjar.util.condition;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class FjExpression<T> implements FjCondition<T> {
    
    @SuppressWarnings("unchecked")
    public static <T> FjExpression<T> parse(String pattern, FjExpressionParser<T> parser) throws FjIllegalExpressionSyntaxException {
        pattern = pattern.trim();
        Stack<Object>   stack   = new Stack<Object>();
        int             begin   = 0;
        for (int end = 0; end < pattern.length(); end++) {
            char c = pattern.charAt(end);
            switch (c) {
            case '(':
            case ')':
                pushWord(stack, pattern.substring(begin, end).trim(), parser);
                pushWord(stack, new String(new char[] {c}), parser);
                begin = end + 1;
                break;
            case '&':
            case '|':
                if (c == pattern.charAt(end + 1)) {
                    pushWord(stack, pattern.substring(begin, end).trim(), parser);
                    pushWord(stack, new String(new char[] {c, c}), parser);
                    begin = end + 2;
                    end ++;
                }
                break;
            default: break;
            }
        }
        if (pattern.length() > begin)
            pushWord(stack, pattern.substring(begin).trim(), parser);
        
        if (stack.isEmpty()) return null;
        return (FjExpression<T>) stack.pop();
    }
    
    private static <T> void pushWord(Stack<Object> stack, String word, FjExpressionParser<T> parser) throws FjIllegalExpressionSyntaxException {
        if (0 == word.length()) return;
        
        switch (word) {
        case "(":
            stack.push(word);
            break;
        case ")": {
            if (2 > stack.size())  throw new FjIllegalExpressionSyntaxException("need ( and word before )");
            arrangeStack(stack, parser);
            Object expr = stack.pop();
            Object oper = stack.pop(); // operator (
            if (!"(".equals(oper)) throw new FjIllegalExpressionSyntaxException("need ( before word and )");
            stack.push(expr);
            arrangeStack(stack, parser);
            break;
        }
        case "&&":
            stack.push(word);
            break;
        case "||":
            stack.push(word);
            break;
        default: {
            stack.push(word);
            arrangeStack(stack, parser);
            break;
        }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> void arrangeStack(Stack<Object> stack, FjExpressionParser<T> parser) throws FjIllegalExpressionSyntaxException {
        if (stack.isEmpty()) return;
        
        Stack<Object> reverse = new Stack<Object>();
        while (!stack.isEmpty()) {
            Object o = stack.peek();
            if ("(".equals(o)) break;
            reverse.push(stack.pop());
        }
        
        FjExpression<T> expr = new FjExpression<T>();
        while (!reverse.isEmpty()) {
            Object o = reverse.pop();
            if (o instanceof String) {
                switch (o.toString()) {
                case OPER_AND:
                    if (reverse.isEmpty()) throw new FjIllegalExpressionSyntaxException("need expression after oper: " + o);
                    o = reverse.pop();
                    if (o instanceof FjCondition) expr.and((FjCondition<T>) o);
                    else if (o instanceof String) expr.and(parser.parseElement(o.toString()));
                    else throw new FjIllegalExpressionSyntaxException("need expression after oper: " + o);
                    break;
                case OPER_OR:
                    if (reverse.isEmpty()) throw new FjIllegalExpressionSyntaxException("need expression after oper: " + o);
                    o = reverse.pop();
                    if (o instanceof FjCondition) expr.or((FjCondition<T>) o);
                    else if (o instanceof String) expr.or(parser.parseElement(o.toString()));
                    else throw new FjIllegalExpressionSyntaxException("need expression after oper: " + o);
                    break;
                default:
                    expr.and(parser.parseElement(o.toString()));
                    break;
                }
            } else if (o instanceof FjCondition) {
                expr.and((FjCondition<T>) o);
            } else {
                throw new FjIllegalExpressionSyntaxException("unknown word: " + o);
            }
        }
        stack.push(expr);
    }
    
    public static interface FjExpressionParser<T> {
        FjCondition<T> parseElement(String element);
    }
    
    private static final String OPER_AND   = "&&";
    private static final String OPER_OR    = "||";
    
    private List<Entry<T>> entries;
    
    public FjExpression() {
        entries = new LinkedList<Entry<T>>();
    }
    
    private FjExpression<T> addEntry(String oper, FjCondition<T> cond) {
        entries.add(new Entry<T>(oper, cond));
        return this;
    }
    
    public FjExpression<T> and(FjCondition<T> condition) {
        addEntry(OPER_AND, condition);
        return this;
    }
    
    public FjExpression<T> or(FjCondition<T> condition) {
        addEntry(OPER_OR, condition);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object apply(T t) {
        FjCondition<T> condition = null;
        for (Entry<T> entry : entries) {
            if (null == condition) condition = entry.cond;
            else {
                switch (entry.oper) {
                case OPER_AND: condition = new FjConditionAnd   <T>(condition, entry.cond);    break;
                case OPER_OR:  condition = new FjConditionOr    <T>(condition, entry.cond);    break;
                }
            }
        }
        if (null == condition) return null;
        return condition.apply(t);
    }
    
    private static class Entry<T> {
        public String       oper;
        public FjCondition<T>  cond;
        
        public Entry(String oper, FjCondition<T> cond) {
            this.oper = oper;
            this.cond = cond;
        }
    }
}
