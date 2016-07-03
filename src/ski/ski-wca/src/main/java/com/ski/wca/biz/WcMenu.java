package com.ski.wca.biz;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.wca.WechatInterface;
import com.ski.wca.WechatInterface.WechatCustomServiceException;
import com.ski.wca.WechatInterface.WechatInterfaceException;
import com.ski.wca.WechatInterface.WechatPermissionDeniedException;

public class WcMenu {
    
    private static final Logger logger = Logger.getLogger(WcMenu.class);
    
    public String index;
    public String name;
    public String desc_s;   // 精简视图下的描述
    public String desc_c = "(请直接回复每项索引)"; // 完整视图下的描述
    
    private Map<String, WcMenu> children;
    
    public WcMenu(String index, String name) {
        this.index = index;
        this.name = name;
        this.children = new LinkedHashMap<String, WcMenu>();
    }
    
    public void addChild(WcMenu... children) {
        for (WcMenu menu : children) {
            this.children.put(menu.index, menu);
        }
    }
    
    public String doSelect(String server, String user) {
        if (!children.isEmpty()) return toCompleteString();
        else return select(server, user);
    }
    
    protected void prepare() {}
    
    protected String select(String server, String user) {return null;}
    
    public String toSimpleString() {
        prepare();
        return String.format("%s - %s%s", index, name, null != desc_s ? desc_s : "");
    }
    
    public String toCompleteString() {
        StringBuilder sb = new StringBuilder();
        sb.append(toSimpleString() + "\n");
        for (WcMenu menu : children.values()) {
            sb.append(menu.toSimpleString() + "\n");
        }
        return sb.toString();
    }
    
    private static final Map<String, WcMenu> menus = new HashMap<String, WcMenu>();
    
    static {
        WcMenu menu_oc = new WcMenu("31", "我的账户"){
            @Override
            protected String select(String server, String user) {
                try {
                    WechatInterface.customSendNewsMessage(user, new WechatInterface.Article[] {
                            new WechatInterface.Article("账户明细", "查看账户余额、优惠券等信息", 
                                    WcWeb.generateUrl(server, CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT, CommonService.getChannelAccountByUser(user).get(0).i_caid), "http://findicons.com/icon/download/203236/stock_people/128/png?id=378556"),
                            new WechatInterface.Article("我要充值", "起租游戏之前需要先充值", "https://www.baidu.com/", "http://findicons.com/icon/download/177279/currency_yuan_blue/128/png?id=177539"),
                            new WechatInterface.Article("我要退款", "申请将账户中的余额全额退款", "https://www.baidu.com/", "http://findicons.com/icon/download/28731/coins/128/png?id=271105"),
                            new WechatInterface.Article("消费记录", "查看过去的消费记录", "https://www.baidu.com/", "http://findicons.com/icon/download/93344/type_list/128/png?id=94878"),
                    });
                } catch (WechatPermissionDeniedException | WechatCustomServiceException e) {
                    logger.error("send custom service message failed", e);
                }
                return null;
            }
        };
        menus.put(menu_oc.index, menu_oc);
    }
    
    public static boolean isMenu(String content) {
        return menus.containsKey(content);
    }
    
    public static WcMenu getMenu(String content) {
        return menus.get(content);
    }
    
    public static void dispatch(String server, String user, String content) {
        WcMenu menu = getMenu(content);
        if (null == menu) {
            logger.error("can not find a menu for: " + content);
            return;
        }
        logger.debug(String.format("user: %s select menu: %s", user, menu.toSimpleString()));
        String result = menu.doSelect(server, user);
        if (null != result) {
            try {WechatInterface.customSendTextMessage(user, content);}
            catch (WechatInterfaceException e) {logger.error("send custom service message failed: " + result, e);}
        }
    }

}
