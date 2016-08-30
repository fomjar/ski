package fomjar.util.condition;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class FjExpression implements FjCondition {
    
    public static FjExpression parse(String expression, FjExpressionParser parser) throws FjIllegalExpressionSyntaxException {
        expression = expression.trim();
        Stack<Object>   stack   = new Stack<Object>();
        int             begin   = 0;
        for (int end = 0; end < expression.length(); end++) {
            char c = expression.charAt(end);
            switch (c) {
            case '(':
            case ')':
                pushWord(stack, expression.substring(begin, end).trim(), parser);
                pushWord(stack, new String(new char[] {c}), parser);
                begin = end + 1;
                break;
            case '&':
            case '|':
                if (c == expression.charAt(end + 1)) {
                    pushWord(stack, expression.substring(begin, end).trim(), parser);
                    pushWord(stack, new String(new char[] {c, c}), parser);
                    begin = end + 2;
                    end ++;
                }
                break;
            default: break;
            }
        }
        if (expression.length() > begin)
            pushWord(stack, expression.substring(begin).trim(), parser);
        
        if (stack.isEmpty()) return null;
        return (FjExpression) stack.pop();
    }
    
    private static void pushWord(Stack<Object> stack, String word, FjExpressionParser parser) throws FjIllegalExpressionSyntaxException {
        if (0 == word.length()) return;
        
        switch (word) {
        case "(":
            stack.push(word);
            break;
        case ")": {
            if (2 > stack.size())  throw new FjIllegalExpressionSyntaxException("need ( and expression before )");
            arrangeStack(stack, parser);
            Object expr = stack.pop();
            Object oper = stack.pop(); // operator (
            if (!"(".equals(oper)) throw new FjIllegalExpressionSyntaxException("need ( before expression and )");
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
    
    private static void arrangeStack(Stack<Object> stack, FjExpressionParser parser) throws FjIllegalExpressionSyntaxException {
        if (stack.isEmpty()) return;
        
        Stack<Object> reverse = new Stack<Object>();
        while (!stack.isEmpty()) {
            Object o = stack.peek();
            if ("(".equals(o)) break;
            reverse.push(stack.pop());
        }
        
        FjExpression expr = new FjExpression();
        while (!reverse.isEmpty()) {
            Object o = reverse.pop();
            if (o instanceof String) {
                switch (o.toString()) {
                case OPER_AND:
                    if (reverse.isEmpty()) throw new FjIllegalExpressionSyntaxException("need expression after oper: " + o);
                    o = reverse.pop();
                    if (o instanceof FjCondition) expr.and((FjCondition) o);
                    else if (o instanceof String) expr.and(parser.parseElement(o.toString()));
                    else throw new FjIllegalExpressionSyntaxException("need expression after oper: " + o);
                    break;
                case OPER_OR:
                    if (reverse.isEmpty()) throw new FjIllegalExpressionSyntaxException("need expression after oper: " + o);
                    o = reverse.pop();
                    if (o instanceof FjCondition) expr.or((FjCondition) o);
                    else if (o instanceof String) expr.or(parser.parseElement(o.toString()));
                    else throw new FjIllegalExpressionSyntaxException("need expression after oper: " + o);
                    break;
                default:
                    expr.and(parser.parseElement(o.toString()));
                    break;
                }
            } else if (o instanceof FjCondition) {
                expr.and((FjCondition) o);
            } else {
                throw new FjIllegalExpressionSyntaxException("unknown word: " + o);
            }
        }
        stack.push(expr);
    }
    
    public static interface FjExpressionParser {
        FjCondition parseElement(String element);
    }
    
    private static final String OPER_AND   = "&&";
    private static final String OPER_OR    = "||";
    
    private List<Entry> entries;
    
    public FjExpression() {
        entries = new LinkedList<Entry>();
    }
    
    private FjExpression addEntry(String oper, FjCondition cond) {
        entries.add(new Entry(oper, cond));
        return this;
    }
    
    public FjExpression and(FjCondition condition) {
        addEntry(OPER_AND, condition);
        return this;
    }
    
    public FjExpression or(FjCondition condition) {
        addEntry(OPER_OR, condition);
        return this;
    }

    @Override
    public Object apply() {
        FjCondition condition = null;
        for (Entry entry : entries) {
            if (null == condition) condition = entry.cond;
            else {
                switch (entry.oper) {
                case OPER_AND: condition = new FjConditionAnd  (condition, entry.cond);    break;
                case OPER_OR:  condition = new FjConditionOr   (condition, entry.cond);    break;
                }
            }
        }
        if (null == condition) return null;
        return condition.apply();
    }
    
    private static class Entry {
        public String       oper;
        public FjCondition  cond;
        
        public Entry(String oper, FjCondition cond) {
            this.oper = oper;
            this.cond = cond;
        }
    }
}
