package com.ski.wca.monitor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.ski.wca.WechatInterface;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjJsonMessage;
import fomjar.util.FjLoopTask;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MenuMonitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(MenuMonitor.class);
    
    private static MenuMonitor instance = null;
    public synchronized static MenuMonitor getInstance() {
    	if (null == instance) instance = new MenuMonitor();
    	return instance;
    }
    
    private MenuMonitor() {}
    
    public void start() {
        if (isRun()) {
            logger.warn("monitor-menu has already started");
            return;
        }
        new Thread(this, "monitor-menu").start();
    }
    
    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("wca.menu.reload-interval"));
        setInterval(second * 1000);
    }
    
    @Override
    public void perform() {
        resetInterval();
        
        boolean swich = "on".equalsIgnoreCase(FjServerToolkit.getServerConfig("wca.menu.reload-switch"));
        if (!swich) return;
        
        JSONObject menu = JSONObject.fromObject(FjServerToolkit.getServerConfig("wca.menu.content"));
        replaceMenuUrl(menu);
        
        FjJsonMessage rsp = WechatInterface.menuCreate(TokenMonitor.getInstance().token(), menu.toString());
        if (0 == rsp.json().getInt("errcode")) logger.info("menu update success");
        else logger.error("menu update failed: " + rsp);
    }
    
    @SuppressWarnings("unchecked")
    private static void replaceMenuUrl(Object menu) {
        if (menu instanceof JSONObject) {
            JSONObject json = (JSONObject) menu;
            if (json.has("url")) {
                Object url = json.get("url");
                if (url instanceof String) {
                	if (!url.toString().contains(FjServerToolkit.getSlb().getAddress("wca").host)) return;
                    try {
                        json.put("url", String.format("https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base#wechat_redirect",
                                FjServerToolkit.getServerConfig("wca.appid"),
                                URLEncoder.encode(url.toString(), "utf-8")));
                    } catch (UnsupportedEncodingException e) {logger.error("replace menu url failed: " + url, e);}
                }
            } else {
                json.values().forEach(v->replaceMenuUrl(v));
            }
        } else if (menu instanceof JSONArray) {
            JSONArray json = (JSONArray) menu;
            json.forEach(o->replaceMenuUrl(o));
        }
    }
    
}
