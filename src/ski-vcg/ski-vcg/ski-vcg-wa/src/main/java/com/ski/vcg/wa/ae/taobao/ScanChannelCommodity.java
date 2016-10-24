package com.ski.vcg.wa.ae.taobao;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ski.vcg.common.CommonDefinition;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.wa.AE;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.util.condition.FjCondition;
import fomjar.util.condition.FjExpression;
import fomjar.util.condition.FjIllegalExpressionSyntaxException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ScanChannelCommodity implements AE {

    private static final Logger logger = Logger.getLogger(ScanChannelCommodity.class);

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
        int     osn     = args.getInt("osn");
        int     cid     = args.getInt("cid");
        int     channel = args.getInt("channel");
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
        for (int page = 0; page < pages; page++) {
            if (0 < page) { // 翻页
                WebElement pager = driver.findElement(By.id("mainsrp-pager"));   // 分页器
                pager.findElement(By.className("next")).findElement(By.tagName("a")).click();  // 下一页
            }
            logger.error("next page: " + (page + 1));
            WebElement itemlist = driver.findElement(By.id("mainsrp-itemlist"));    // 项目列表
            List<WebElement> items = itemlist.findElements(By.className("J_MouserOnverReq"));
            for (WebElement item : items) {  // 遍历每个项
                JSONObject commodity = null;
                WebElement a = item.findElement(By.className("title")).findElement(By.tagName("a"));
                String title = a.getAttribute("innerHTML")
                        .replace("<span class=\"H\">", "")
                        .replace("</span>", "")
                        .replace("<span class=\"baoyou-intitle icon-service-free\">", "")
                        .trim();
                if ((boolean) expr_include.apply(title) && (boolean) expr_exclude.apply(title)) {   // 符合条件的
                    try {
                        item.findElement(By.className("icon-service-tianmao")); // 图标天猫
//                        try {commodity = parseTmall(driver, item);}
//                        catch (Exception e) {logger.error("error occurs when parse tmall", e);}
                    } catch (NoSuchElementException e) {                        // 非天猫
                        try {commodity = parseTaobao(driver, item);}
                        catch (Exception e2) {logger.error("error occurs when parse taobao", e2);}
                    }
                    if (null == commodity) {
                        logger.error("error occurs when parse commodity, skip: " + title);
                        continue;
                    }
                    logger.info("report commodity: " + title);
                    reportCommodity(commodity, osn, cid, channel);
                }
                switchToSearchWindow(driver);
            }
        }
        code = CommonDefinition.CODE.CODE_SYS_SUCCESS;
        desc = null;
    }

    private void reportCommodity(JSONObject commodity, long osn, int cid, int channel) {
        commodity.put("osn", osn);
        commodity.put("cid", cid);
        commodity.put("channel", CommonService.CHANNEL_TAOBAO);
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs","wa-scc");
        req.json().put("ts", "cdb");
        req.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_COMMODITY);
        req.json().put("args", commodity);
        FjServerToolkit.getAnySender().send(req);
    }

    private JSONObject parseTaobao(WebDriver driver, WebElement element) {
        JSONObject args = new JSONObject();
        element.findElement(By.className("J_ItemPicA")).click();
        switchToItemWindow(driver);
        try {Thread.sleep(3000L);} catch (InterruptedException e) {e.printStackTrace();}
        try {
            // 商品信息
            args.put("item_url",    driver.getCurrentUrl());
            args.put("item_cover",  driver.findElement(By.id("J_ImgBooth")).getAttribute("src"));
            args.put("item_name",   driver.findElement(By.id("J_Title")).findElement(By.tagName("h3")).getText().trim());
            args.put("item_remark", driver.findElement(By.id("J_Title")).findElement(By.tagName("p")).getText().trim());
            args.put("item_sold",   Integer.parseInt(driver.findElement(By.id("J_SellCounter")).getText()));
            args.put("item_price",  driver.findElement(By.id("J_StrPrice")).findElement(By.className("tb-rmb-num")).getText());
            String express_str = driver.findElement(By.id("J_WlServiceTitle")).getText();
            if (express_str.contains("免运费")) args.put("express_price", 0.0f);
            else args.put("express_price", Float.parseFloat(express_str.replace("快递", "").replaceAll("EMS", "").replace("¥", "").trim()));

            // 店铺信息
            args.put("shop_url",    driver.findElement(By.className("tb-shop-name")).findElement(By.tagName("a")).getAttribute("href"));
            args.put("shop_name",   driver.findElement(By.className("tb-shop-name")).findElement(By.tagName("a")).getText());
            args.put("shop_owner",  driver.findElement(By.className("tb-seller-name")).getText());
            WebElement r = driver.findElement(By.className("tb-shop-rank"));
            args.put("shop_rate",   r.getAttribute("class").split(" ")[1] + " " + r.findElements(By.tagName("i")).size());
            args.put("shop_score",  driver.findElement(By.className("tb-shop-rate")).findElements(By.tagName("a")).stream().map(e->e.getText()).collect(Collectors.joining("|")));
            args.put("shop_addr",   driver.findElement(By.id("J-From")).getText());
        } finally { // item
            if (driver.getWindowHandles().size() > 1) {
                switchToItemWindow(driver);
                driver.close();
            }
            switchToSearchWindow(driver);
        }

        return args;
    }

//    private JSONObject parseTmall(WebDriver driver, WebElement element) {
//        JSONObject args = new JSONObject();
//        element.findElement(By.tagName("a")).click();
//        switchToLastWindow(driver);
//        try {Thread.sleep(3000L);} catch (InterruptedException e) {e.printStackTrace();}
//        try {
//            // 商品信息
//            args.put("item_url",    driver.getCurrentUrl());
//            args.put("item_cover",  driver.findElement(By.id("J_ImgBooth")).getAttribute("src"));
//            args.put("item_name",   driver.findElement(By.className("tb-detail-hd")).findElement(By.tagName("h1")).getText().trim());
//            args.put("item_remark", driver.findElement(By.className("tb-detail-hd")).findElement(By.tagName("p")).getText().trim());
//            args.put("item_sold",   Integer.parseInt(driver.findElement(By.className("tm-ind-sellCount")).findElement(By.className("tm-count")).getText()));
//            args.put("item_price",  Float.parseFloat(driver.findElement(By.className("tm-price")).getText()));
//            args.put("express_price", Float.parseFloat(driver.findElement(By.id("J_PostageToggleCont")).findElement(By.tagName("span")).getText().replace("快递:", "").trim()));
//
//            args.put("shop_addr",   driver.findElement(By.id("J_deliveryAdd")).getText());
//
//            driver.findElement(By.className("render-byjs")).findElement(By.tagName("a")).click();
//            switchToLastWindow(driver);
//            try {Thread.sleep(3000L);} catch (InterruptedException e) {e.printStackTrace();}
//            try {
//                // 店铺信息
//                args.put("shop_url",    driver.getCurrentUrl());
//                args.put("shop_name",   driver.findElement(By.className("personal-info")).findElement(By.className("col-sub")).findElement(By.className("title")).getText().trim());
//                args.put("shop_owner",  "tmall");
//                args.put("shop_rate",   -1);
//                List<String> score = new LinkedList<String>();
//                for (WebElement e : driver.findElement(By.id("dsr")).findElements(By.tagName("li"))) score.add(e.findElement(By.className("tb-title")).getText() + e.findElement(By.className("count")).getText());
//                args.put("shop_score",  score.stream().collect(Collectors.joining(" ")));
//            } finally { // shop
//                switchToLastWindow(driver);
//                driver.close();
//            }
//        } finally { // item
//            switchToLastWindow(driver);
//            driver.close();
//            switchToFirstWindow(driver);
//        }
//        return args;
//    }

    private void switchToItemWindow(WebDriver driver) {
        for (String window : driver.getWindowHandles()) {
            driver.switchTo().window(window);
            if (!driver.getTitle().contains("_淘宝搜索")) break;
        }
    }

    private void switchToSearchWindow(WebDriver driver) {
        for (String window : driver.getWindowHandles()) {
            driver.switchTo().window(window);
            if (driver.getTitle().contains("_淘宝搜索")) break;
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
