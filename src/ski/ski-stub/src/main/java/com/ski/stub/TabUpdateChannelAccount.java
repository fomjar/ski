package com.ski.stub;

import com.ski.common.SkiCommon;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class TabUpdateChannelAccount extends TabPaneBase {

    private static final long serialVersionUID = 4478338967110972515L;
    
    public TabUpdateChannelAccount() {
        addField(CommonUI.createPanelLabelField("CA      ID  (十六进制/自动生成)"));
        addField(CommonUI.createPanelLabelField("用  户  名  (字符串)"));
        addField(CommonUI.createPanelLabelCombo("渠道  来源  (字符串)", new String[] {"0 - 淘宝", "1 - 微信"}));
        addField(CommonUI.createPanelLabelField("昵      称  (字符串)"));
        addField(CommonUI.createPanelLabelCombo("性      别  (字符串)", new String[] {"0 - 女", "1 - 男", "2 - 人妖"}));
        addField(CommonUI.createPanelLabelField("电      话  (字符串)"));
        addField(CommonUI.createPanelLabelField("地      址  (字符串)"));
        addField(CommonUI.createPanelLabelField("邮      编  (字符串)"));
        addField(CommonUI.createPanelLabelField("出生  日期  (yyyy-mm-dd)"));
    }

    @Override
    protected void submit() {
        JSONObject args = new JSONObject();
        String caid     = getFieldToField(0).getText();
        if (0 < caid.length())      args.put("caid", Integer.parseInt(caid, 16));
        String user     = getFieldToField(1).getText();
        if (0 < user.length())      args.put("user", user);
        int    channel  = getFieldToCombo(2).getSelectedIndex();
        args.put("channel", channel);
        String nick     = getFieldToField(3).getText();
        if (0 < nick.length())      args.put("nick", nick);
        int    gender   = getFieldToCombo(4).getSelectedIndex();
        args.put("gender", gender);
        String phone    = getFieldToField(5).getText();
        if (0 < phone.length())     args.put("phone", phone);
        String address  = getFieldToField(6).getText();
        if (0 < address.length())   args.put("address", address);
        String zipcode  = getFieldToField(7).getText();
        if (0 < zipcode.length())   args.put("zipcode", zipcode);
        String birth    = getFieldToField(8).getText();
        if (0 < birth.length())     args.put("birth", birth);
        
        FjDscpMessage rsp = null;
        if (Service.isResponseSuccess(rsp = Service.send(SkiCommon.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args)))
            setStatus("操作成功");
        else setStatus(rsp.args().toString());
    }

}
