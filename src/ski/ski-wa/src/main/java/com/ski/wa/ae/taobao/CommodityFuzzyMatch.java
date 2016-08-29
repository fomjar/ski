package com.ski.wa.ae.taobao;

import org.openqa.selenium.WebDriver;

import com.ski.common.CommonDefinition;
import com.ski.wa.AE;

import net.sf.json.JSONObject;

public class CommodityFuzzyMatch implements AE {
    
    private int     code;
    private String  desc;

    @Override
    public void execute(WebDriver driver, JSONObject args) {
        if (!(args.containsKey("key") && args.containsKey("cond"))) {
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal argument, there must be 'key' and 'cond'";
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

}
