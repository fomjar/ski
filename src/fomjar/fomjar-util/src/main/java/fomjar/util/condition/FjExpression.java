package fomjar.util.condition;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class FjExpression<T> implements FjCondition<T> {
    
//    public static void main(String[] args) throws FjIllegalExpressionSyntaxException {
//        String text = "abcdefghijklmnopqrstuvwxyz";
//        System.out.println(FjExpression.parse("(ab && cd) || (ef || 123 && abd)", new FjExpressionParser<String>() {
//            @Override
//            public FjCondition<String> parseElement(String element) {
//                return new FjCondition<String>() {
//                    @Override
//                    public Object apply(String t) {return t.contains(element);}
//                };
//            }
//        }).apply(text));
//    }
    
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
        
        eval(stack, parser);
        
        if (stack.isEmpty()) return null;
        return (FjExpression<T>) stack.pop();
    }
    
    private static <T> void pushWord(Stack<Object> stack, String word, FjExpressionParser<T> parser) throws FjIllegalExpressionSyntaxException {
        if (0 == word.length()) return;
        
        char c = word.charAt(0);
        if (')' != c) stack.push(word);
        
        if (')' == c) {
            eval(stack, parser);
            if (2 > stack.size())   throw new FjIllegalExpressionSyntaxException("need ( and expression before )");
            Object expr = stack.pop();
            Object oper = stack.pop(); // operator (
            if (!"(".equals(oper))  throw new FjIllegalExpressionSyntaxException("need ( before expression and )");
            stack.push(expr);
        }
    }
    
    private static boolean isWordChar(char c) {
        return     c != '('
                && c != ')'
                && c != OPER_AND.charAt(0)
                && c != OPER_OR.charAt(0);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> void eval(Stack<Object> stack, FjExpressionParser<T> parser) throws FjIllegalExpressionSyntaxException {
        if (stack.isEmpty()) return;
        
        Stack<Object> reverse = new Stack<Object>();
        while (!stack.isEmpty()) {
            Object o = stack.peek();
            if ("(".equals(o)) break;
            reverse.push(stack.pop());
        }
        
        if (reverse.isEmpty()) return;
        
        FjExpression<T> expr = new FjExpression<T>();
        Object o = reverse.pop();       // expression
        
        if (o instanceof String)            expr.and(parser.parseElement((String) o));  // set and as default
        else if (o instanceof FjCondition)  expr.and((FjCondition<T>) o);               // set and as default
        else                                throw new FjIllegalExpressionSyntaxException("unknown expression type: " + o.getClass());
        
        while (2 <= reverse.size()) {
            o = reverse.pop();
            if (!(o instanceof String))         throw new FjIllegalExpressionSyntaxException("need operator after expression");
            String oper = (String) o;   // operator
            if (!FjExpression.isOperator(oper)) throw new FjIllegalExpressionSyntaxException("illegal operator: " + oper);
            
            o = reverse.pop();          // expression
            if (o instanceof FjCondition)       expr.addEntry(oper, (FjCondition<T>) o);
            else if (o instanceof String)       expr.addEntry(oper, parser.parseElement((String) o));
            else                                throw new FjIllegalExpressionSyntaxException("need expression after oper: " + oper);
        }
        stack.push(expr);
    }
    
    public static interface FjExpressionParser<T> {
        FjCondition<T> parseElement(String element);
    }
    
    private static final String OPER_AND   = "&&";
    private static final String OPER_OR    = "||";
    private static boolean isOperator(String oper) {
        switch (oper) {
        case OPER_AND:
        case OPER_OR:
            return true;
        default:
            return false;
        }
    }
    
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
