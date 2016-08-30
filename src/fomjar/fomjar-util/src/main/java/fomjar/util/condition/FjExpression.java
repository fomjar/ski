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
            if (isWordChar(c)) continue;
            
            // meet operator char
            // push expression word
            pushWord(stack, pattern.substring(begin, end).trim(), parser);
            
            // push operator word
            if (('&' == c && '&' == pattern.charAt(end + 1))
                    || ('|' == c && '|' == pattern.charAt(end + 1))) {
                pushWord(stack, pattern.substring(end, end + 2).trim(), parser);
                end += 1;
            } else {
                pushWord(stack, pattern.substring(end, end + 1).trim(), parser);
            }
            begin = end + 1;
        }
        if (pattern.length() > begin)
            pushWord(stack, pattern.substring(begin).trim(), parser);
        
        if (stack.isEmpty()) return null;
        return (FjExpression<T>) stack.pop();
    }
    
    private static <T> void pushWord(Stack<Object> stack, String word, FjExpressionParser<T> parser) throws FjIllegalExpressionSyntaxException {
        if (0 == word.length()) return;
        
        char c = word.charAt(0);
        if (')' != c) stack.push(word);
        
        if (')' == c || isWordChar(c)) arrangeStack(stack, parser);
        
        if (')' == c) {
            Object expr = stack.pop();
            Object oper = stack.pop(); // operator (
            if (!"(".equals(oper)) throw new FjIllegalExpressionSyntaxException("need ( before expression and )");
            stack.push(expr);
            arrangeStack(stack, parser);
        }
    }
    
    private static boolean isWordChar(char c) {
        return ('0' <= c && c <= '9')
                || ('a' <= c && c <= 'z')
                || ('A' <= c && c <= 'Z')
                || '-' == c
                || '_' == c;
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
            Object o = reverse.pop();       // expression
            
            if (o instanceof String)            expr.and(parser.parseElement((String) o));  // set and as default
            else if (o instanceof FjCondition)  expr.and((FjCondition<T>) o);               // set and as default
            else                                throw new FjIllegalExpressionSyntaxException("unknown expression type: " + o.getClass());
            
            if (2 <= reverse.size()) {
                o = reverse.pop();
                if (!(o instanceof String))         throw new FjIllegalExpressionSyntaxException("need operator after expression");
                String oper = (String) o;   // operator
                
                o = reverse.pop();          // expression
                if (o instanceof FjCondition)       expr.addEntry(oper, (FjCondition<T>) o);
                else if (o instanceof String)       expr.addEntry(oper, parser.parseElement((String) o));
                else                                throw new FjIllegalExpressionSyntaxException("need expression after oper: " + oper);
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
        public String           oper;
        public FjCondition<T>   cond;
        
        public Entry(String oper, FjCondition<T> cond) {
            this.oper = oper;
            this.cond = cond;
        }
    }
}
