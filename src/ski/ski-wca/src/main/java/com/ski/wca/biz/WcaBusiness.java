package com.ski.wca.biz;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class WcaBusiness {
    
    private static WcaBusiness instance = null;
    
    public static WcaBusiness getInstance() {
        if (null == instance) instance = new WcaBusiness();
        return instance;
    }
    
    private String server;
    private Map<String, CacheUser> cache;
    
    private WcaBusiness() {
        cache = new LinkedHashMap<String, CacheUser>();
    }
    
    public void setServer(String server) {
        this.server = server;
    }
    
    public void dispatch(FjDscpMessage req) {
        String user = req.argsToJsonObject().getString("user");
        
        switch (req.inst()) {
        case CommonDefinition.ISIS.INST_ECOM_QUERY_CHANNEL_ACCOUNT:
            break;
        default:
            if (!verifyUser(user)) return;
            break;
        }
        
    }
    
    /**
     * 
     * @param user
     * @return true for pass
     */
    private boolean verifyUser(String user) {
        if (!CommonService.getChannelAccountByUserName(user).isEmpty()) {
            if (!cache.containsKey(user)) cache.put(user, new CacheUser(CommonService.getChannelAccountByUserName(user).get(0)));
            return true;
        }
        
        { // register
            JSONObject args = new JSONObject();
            args.put("channel", 1);
            args.put("user",    user);
            FjDscpMessage req = new FjDscpMessage();
            req.json().put("fs",    server);
            req.json().put("ts",    "cdb");
            req.json().put("inst",  CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT);
            req.json().put("args",  args);
            FjServerToolkit.getAnySender().send(req);
        }
        
        return false;
    }
    
    private static enum OperateMode {
        register,
        menu,
        search,
    };
    
    private static class CacheUser {
        
        public BeanChannelAccount   user;
        public OperateMode          mode = OperateMode.menu;
        
        public CacheUser(BeanChannelAccount user) {
            this.user = user;
        }
    }

}
