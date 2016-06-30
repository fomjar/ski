package com.ski.wca.biz;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ski.wca.WechatInterface;
import com.ski.wca.WechatInterface.WechatInterfaceException;

public abstract class WcaMenu {
    
    private static final Logger logger = Logger.getLogger(WcaMenu.class);
    
    public String index;
    public String name;
    public String desc_s;   // 精简视图下的描述
    public String desc_c = "(请直接回复每项索引)"; // 完整视图下的描述
    
    private Map<String, WcaMenu> children;
    
    public WcaMenu(String index, String name) {
        this.index = index;
        this.name = name;
        this.children = new LinkedHashMap<String, WcaMenu>();
    }
    
    public void addChild(WcaMenu... children) {
        for (WcaMenu menu : children) {
            this.children.put(menu.index, menu);
        }
    }
    
    public void doSelect(String server, String user) {
        if (!children.isEmpty()) {
            String content = toCompleteString();
            try {WechatInterface.customSendTextMessage(user, content);}
            catch (WechatInterfaceException e) {logger.error("send custom service message failed: " + content, e);}
        } else {
            select(server, user);
        }
    }
    
    public abstract void select(String server, String user);
    
    protected void prepare() {}
    
    public String toSimpleString() {
        return String.format("%s - %s%s", index, name, null != desc_s ? desc_s : "");
    }
    
    public String toCompleteString() {
        StringBuilder sb = new StringBuilder();
        prepare();
        sb.append(toSimpleString() + "\n");
        for (WcaMenu menu : children.values()) {
            menu.prepare();
            sb.append(menu.toSimpleString() + "\n");
        }
        return sb.toString();
    }
    
    private static final Map<String, WcaMenu> menus = new HashMap<String, WcaMenu>();
    
    static {
        
    }
    
    public static boolean isMenu(String content) {
        if (null == menus) return false;
        return menus.containsKey(content.split(" ")[0]);
    }
    
    public static WcaMenu getMenu(String content) {
        if (null == menus) return null;
        return menus.get(content.split(" ")[0]);
    }
    
    public static void dispatch(String server, String user, String content) {
        WcaMenu menu = getMenu(content);
        if (null == menu) {
            logger.error("can not find a menu for: " + content);
            return;
        }
        menu.doSelect(server, user);
    }

}
