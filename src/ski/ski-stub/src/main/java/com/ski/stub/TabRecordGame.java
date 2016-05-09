package com.ski.stub;

import com.ski.common.SkiCommon;

import net.sf.json.JSONObject;

public class TabRecordGame extends TabPaneBase {

    private static final long serialVersionUID = -5672971309710316580L;
    
    public TabRecordGame() {
        addField(CommonUI.createPanelLabelField("G       ID  (十六进制)"));
        addField(CommonUI.createPanelLabelCombo("平      台  (字符串)", new String[] {"PS4", "XBOX ONE", "PS3", "XBOX 360"}));
        addField(CommonUI.createPanelLabelCombo("国      家  (字符串)", new String[] {"美国", "日本", "韩国", "中国"}));
        addField(CommonUI.createPanelLabelField("图      标  (URL)"));
        addField(CommonUI.createPanelLabelField("海      报  (URL)"));
        addField(CommonUI.createPanelLabelField("采      购  (URL)"));
        addField(CommonUI.createPanelLabelField("发售  日期  (yyyy-mm-dd)"));
        addField(CommonUI.createPanelLabelField("简体中文名  (字符串)"));
        addField(CommonUI.createPanelLabelField("英  文  名  (字符串)"));
    }

    @Override
    protected void submit() {
        JSONObject args = new JSONObject();
        String gid          = getFieldToField(0).getText();
        if (0 < gid.length())           args.put("gid",         Integer.parseInt(gid, 16));
        String platform     = getFieldToCombo(1).getSelectedItem().toString();
        if (0 < platform.length())      args.put("platform",    platform);
        String country      = getFieldToCombo(2).getSelectedItem().toString();
        if (0 < country.length())       args.put("country",     country);
        String url_icon     = getFieldToField(3).getText();
        if (0 < url_icon.length())      args.put("url_icon",    url_icon);
        String url_poster   = getFieldToField(4).getText();
        if (0 < url_poster.length())    args.put("url_poster",  url_poster);
        String url_buy      = getFieldToField(5).getText();
        if (0 < url_buy.length())       args.put("url_buy",     url_buy);
        String sale         = getFieldToField(6).getText();
        if (0 < sale.length())          args.put("sale",        sale);
        String name_zh      = getFieldToField(7).getText();
        if (0 < name_zh.length())       args.put("name_zh",     name_zh);
        String name_en      = getFieldToField(8).getText();
        if (0 < name_en.length())       args.put("name_en",     name_en);
        
        String response = Service.send(SkiCommon.ISIS.INST_ECOM_UPDATE_GAME, args);
        setStatus(response.replaceAll("<[^>]+>", ""));
    }

}
