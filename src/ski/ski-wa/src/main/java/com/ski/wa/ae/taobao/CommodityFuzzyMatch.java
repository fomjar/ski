package com.ski.wa.ae.taobao;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.ski.common.CommonDefinition;
import com.ski.wa.AE;

import fomjar.util.condition.FjCondition;
import fomjar.util.condition.FjExpression;
import fomjar.util.condition.FjIllegalExpressionSyntaxException;
import net.sf.json.JSONObject;

public class CommodityFuzzyMatch implements AE {
    
    private static final Logger logger = Logger.getLogger(CommodityFuzzyMatch.class);
    
    private int     code;
    private String  desc;

    @Override
    public void execute(WebDriver driver, JSONObject args) {
        if (!(args.containsKey("preset") && args.containsKey("condition"))) {
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal argument, there must be 'preset' and 'condition'";
            return;
        }
        String      preset      = args.getString("preset");
        JSONObject  condition   = args.getJSONObject("condition");
        String      include     = condition.getString("include");
        String      exclude     = condition.getString("exclude");
        FjExpression<String> expr_include = null;
        FjExpression<String> expr_exclude = null;
        try {
            expr_include = FjExpression.parse(include, new FjExpression.FjExpressionParser<String>() {
                @Override
                public FjCondition<String> parseElement(String element) {return new ConditionInclude(element);}
            });
        } catch (FjIllegalExpressionSyntaxException e) {
            logger.error("parse include condition failed: " + include, e);
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;;
            desc = "illegal include condition syntax: " + include;
            return;
        }
        try {
            expr_exclude = FjExpression.parse(exclude, new FjExpression.FjExpressionParser<String>() {
                @Override
                public FjCondition<String> parseElement(String element) {return new ConditionExclude(element);}
            });
        } catch (FjIllegalExpressionSyntaxException e) {
            logger.error("parse exclude condition failed: " + include, e);
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;;
            desc = "illegal exclude condition syntax: " + exclude;
            return;
        }
    }
    
    @Override
    public int code() {
        return code;
    }

    @Override
    public String desc() {
        return desc;
    }
    
    private static class ConditionInclude implements FjCondition<String> {
        private String element;
        public ConditionInclude(String element) {
            this.element = element;
        }
        @Override
        public Object apply(String text) {
            return text.contains(element);
        }
    }
    
    private static class ConditionExclude implements FjCondition<String> {
        private String element;
        public ConditionExclude(String element) {
            this.element = element;
        }
        @Override
        public Object apply(String text) {
            return !text.contains(element);
        }
    }

}
