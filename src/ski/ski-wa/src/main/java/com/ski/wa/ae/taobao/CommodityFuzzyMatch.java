package com.ski.wa.ae.taobao;

import org.openqa.selenium.WebDriver;

import com.ski.common.CommonDefinition;
import com.ski.wa.AE;

import fomjar.util.condition.FjCondition;
import fomjar.util.condition.FjExpression;
import net.sf.json.JSONObject;

public class CommodityFuzzyMatch implements AE {
    
    private int     code;
    private String  desc;

    @Override
    public void execute(WebDriver driver, JSONObject args) {
        if (!(args.containsKey("preset") && args.containsKey("condition"))) {
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal argument, there must be 'preset' and 'condition'";
            return;
        }
        String      preset  = args.getString("preset");
        JSONObject  condition   = args.getJSONObject("condition");
        FjCondition cond_include    = FjExpression.parse(condition.getString("include"));
        FjCondition cond_exclude    = FjExpression.parse(condition.getString("exclude"));
    }
    
    @Override
    public int code() {
        return code;
    }

    @Override
    public String desc() {
        return desc;
    }

}
