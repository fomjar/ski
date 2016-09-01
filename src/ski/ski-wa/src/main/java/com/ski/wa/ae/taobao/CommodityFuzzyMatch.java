package com.ski.wa.ae.taobao;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ski.common.CommonDefinition;
import com.ski.wa.AE;

import fomjar.util.condition.FjCondition;
import fomjar.util.condition.FjExpression;
import fomjar.util.condition.FjIllegalExpressionSyntaxException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CommodityFuzzyMatch implements AE {
    
    private static final Logger logger = Logger.getLogger(CommodityFuzzyMatch.class);
    
    private int     code;
    private String  desc;

    @Override
    public void execute(WebDriver driver, JSONObject args) {
        if (!(args.containsKey("preset") && args.containsKey("include") && args.containsKey("exclude"))) {
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal argument, there must be 'preset', 'include' and 'exclude'";
            return;
        }
        String  preset  = args.getString("preset");
        String  include = args.getString("include");
        String  exclude = args.getString("exclude");
        FjExpression<String> expr_include = null;
        FjExpression<String> expr_exclude = null;
        try {
            expr_include = FjExpression.parse(include, new FjExpression.FjExpressionParser<String>() {
                @Override
                public FjCondition<String> parseElement(String element) {return new ConditionInclude(element);}
            });
        } catch (FjIllegalExpressionSyntaxException e) {
            logger.error("parse include expression failed: " + include, e);
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal include syntax: " + include;
            return;
        }
        try {
            expr_exclude = FjExpression.parse(exclude, new FjExpression.FjExpressionParser<String>() {
                @Override
                public FjCondition<String> parseElement(String element) {return new ConditionExclude(element);}
            });
        } catch (FjIllegalExpressionSyntaxException e) {
            logger.error("parse exclude expression failed: " + include, e);
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal exclude syntax: " + exclude;
            return;
        }
        
        driver.get("https://www.taobao.com");
        driver.findElement(By.id("q")).clear();                             // 搜索框
        driver.findElement(By.id("q")).sendKeys(preset);                    // 搜索框
        driver.findElement(By.className("btn-search")).click();             // 搜索按钮
        
        {   // 判断是否有结果
            WebElement tips = driver.findElement(By.id("mainsrp-tips"));        // 结果提示
            try {   // no result
                tips.findElement(By.className("norestip-title"));               // 无结果样式
                code = CommonDefinition.CODE.CODE_SYS_SUCCESS;
                desc = new JSONArray().toString();
                return;
            } catch (NoSuchElementException e) {}   // 没有此样式表示有搜索结果
        }
        int pages = 1;  // 总页数
        {   // 获取页数
            WebElement pager = driver.findElement(By.id("mainsrp-pager"));  // 分页器
            try {
                WebElement total = pager.findElement(By.className("total"));    // 总页数
                pages = Integer.parseInt(total.getText().split(" ")[1]);
            } catch (NoSuchElementException e) {pages = 1;}    // 没有此样式表示只有1页
        }
        // 遍历每一页
        JSONArray items = new JSONArray();
        for (int page = 0; page < pages; page++) {
            if (0 < page) { // 翻页
                WebElement pager = driver.findElement(By.id("mainsrp-pager"));   // 分页器
                pager.findElement(By.className("next")).findElement(By.tagName("a")).click();  // 下一页
            }
            logger.error("next page: " + (page + 1));
            WebElement itemlist = driver.findElement(By.id("mainsrp-itemlist"));    // 项目列表
            for (WebElement item : itemlist.findElements(By.className("J_MouserOnverReq"))) {  // 遍历每个项
                WebElement a = item.findElement(By.className("title")).findElement(By.tagName("a"));
                String title = a.getAttribute("innerHTML")
                        .replace("<span class=\"H\">", "")
                        .replace("</span>", "")
                        .replace("<span class=\"baoyou-intitle icon-service-free\">", "")
                        .trim();
                if ((boolean) expr_include.apply(title) && (boolean) expr_exclude.apply(title)) {   // 符合条件的
                    try {
                        item.findElement(By.className("icon-service-tianmao")); // 图标天猫
                        try {items.add(parseTmall(driver, item));}
                        catch (NoSuchElementException e) {logger.error("error occurs when parse tmall", e);}
                    } catch (NoSuchElementException e) {                        // 非天猫
                        try {items.add(parseTaobao(driver, item));}
                        catch (NoSuchElementException e2) {logger.error("error occurs when parse taobao", e2);}
                    }
                }
            }
        }
        code = CommonDefinition.CODE.CODE_SYS_SUCCESS;
        desc = items.toString();
    }
    
    private JSONObject parseTaobao(WebDriver driver, WebElement element) {
        element.findElement(By.tagName("img")).click();
        JSONObject args = new JSONObject();
        // 商品信息
        args.put("item_url",    driver.getCurrentUrl());
        args.put("item_cover",  driver.findElement(By.id("J_ImgBooth")).getAttribute("src"));
        args.put("item_name",   driver.findElement(By.id("J_Title")).findElement(By.tagName("h3")).getText().trim());
        args.put("item_remark", driver.findElement(By.id("J_Title")).findElement(By.tagName("p")).getText().trim());
        args.put("item_sold",   Integer.parseInt(driver.findElement(By.id("J_SellCounter")).getText()));
        args.put("item_price",  Float.parseFloat(driver.findElement(By.id("J_StrPrice")).findElement(By.className("tb-rmb-num")).getText()));
        String express_str = driver.findElement(By.id("J_WlServiceTitle")).getText();
        if (express_str.contains("免运费")) args.put("express_price", 0.0f);
        else args.put("express_price", Float.parseFloat(express_str.replace("快递", "").trim()));
        
        args.put("shop_addr",   driver.findElement(By.id("J-From")).getText());
        
        driver.findElement(By.className("tb-shop-rank")).findElement(By.tagName("a")).click();
        
        // 店铺信息
        args.put("shop_url",    driver.getCurrentUrl());
        args.put("shop_name",   driver.findElement(By.id("header")).findElement(By.className("shop-name")).findElement(By.tagName("a")).getText());
        args.put("shop_owner",  driver.findElement(By.className("personal-info")).findElement(By.className("col-sub")).findElement(By.className("title")).getText().trim());
        args.put("shop_rate",   Integer.parseInt(driver.findElement(By.className("sep")).findElement(By.tagName("li")).getText().replace("卖家信用：", "").trim()));
        List<String> score = new LinkedList<String>();
        for (WebElement e : driver.findElement(By.id("dsr")).findElements(By.tagName("li"))) score.add(e.findElement(By.className("tb-title")).getText() + e.findElement(By.className("count")).getText());
        args.put("shop_score",  score.stream().collect(Collectors.joining(" ")));
        
        driver.close(); // shop
        driver.close(); // item
        return args;
    }
    
    private JSONObject parseTmall(WebDriver driver, WebElement element) {
        element.findElement(By.tagName("img")).click();
        JSONObject args = new JSONObject();
        // 商品信息
        args.put("item_url",    driver.getCurrentUrl());
        args.put("item_cover",  driver.findElement(By.id("J_ImgBooth")).getAttribute("src"));
        args.put("item_name",   driver.findElement(By.className("tb-detail-hd")).findElement(By.tagName("h1")).getText().trim());
        args.put("item_remark", driver.findElement(By.className("tb-detail-hd")).findElement(By.tagName("p")).getText().trim());
        args.put("item_sold",   Integer.parseInt(driver.findElement(By.className("tm-ind-sellCount")).findElement(By.className("tm-count")).getText()));
        args.put("item_price",  Float.parseFloat(driver.findElement(By.className("tm-price")).getText()));
        args.put("express_price", Float.parseFloat(driver.findElement(By.id("J_PostageToggleCont")).findElement(By.tagName("span")).getText().replace("快递:", "").trim()));
        
        args.put("shop_addr",   driver.findElement(By.id("J_deliveryAdd")).getText());
        
        driver.findElement(By.className("render-byjs")).findElement(By.tagName("a")).click();
        
        // 店铺信息
        args.put("shop_url",    driver.getCurrentUrl());
        args.put("shop_name",   driver.findElement(By.className("personal-info")).findElement(By.className("col-sub")).findElement(By.className("title")).getText().trim());
        args.put("shop_owner",  "tmall");
        args.put("shop_rate",   -1);
        List<String> score = new LinkedList<String>();
        for (WebElement e : driver.findElement(By.id("dsr")).findElements(By.tagName("li"))) score.add(e.findElement(By.className("tb-title")).getText() + e.findElement(By.className("count")).getText());
        args.put("shop_score",  score.stream().collect(Collectors.joining(" ")));
        
        driver.close(); // shop
        driver.close(); // item
        return args;
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
