package com.ski.wca.biz;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.wca.WechatInterface;
import com.ski.wca.WechatInterface.WechatCustomServiceException;
import com.ski.wca.WechatInterface.WechatInterfaceException;
import com.ski.wca.WechatInterface.WechatPermissionDeniedException;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class WcaBusiness {
    
    private static WcaBusiness instance = null;
    public static WcaBusiness getInstance() {
        if (null == instance) instance = new WcaBusiness();
        return instance;
    }
    
    private static final Logger logger = Logger.getLogger(WcaBusiness.class);
    
    private String server;
    private Map<String, CacheUser> cache;
    
    private WcaBusiness() {
        cache = new LinkedHashMap<String, CacheUser>();
    }
    
    public void setServer(String server) {
        this.server = server;
    }
    
    public void dispatch(FjDscpMessage req) {
        String user     = req.argsToJsonObject().getString("user");
        String content  = req.argsToJsonObject().has("content") ? req.argsToJsonObject().getString("content") : null;
        
        if (!verifyUser(user)) return;
        
        switch (req.inst()) {
        case CommonDefinition.ISIS.INST_USER_RESPONSE: {
            try {WechatInterface.customSendTextMessage(user, content);}
            catch (WechatInterfaceException e) {logger.error("send custom service message failed: " + content, e);}
            break;
        }
        case CommonDefinition.ISIS.INST_USER_REQUEST: {
            if (WcaCommand.isCommand(content)) WcaCommand.dispatch(server, user, content);
            else if (WcaMenu.isMenu(content)) WcaMenu.dispatch(server, user, content);
            else {
            }
            break;
        }
        case CommonDefinition.ISIS.INST_USER_SUBSCRIBE:
            break;
        case CommonDefinition.ISIS.INST_USER_UNSUBSCRIBE:
            break;
        case CommonDefinition.ISIS.INST_USER_GOTO:
            break;
        case CommonDefinition.ISIS.INST_USER_LOCATION:
            break;
        default:
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
        
        sendBindTip(user);
        
        return false;
    }
    
    private void sendBindTip(String user) {
        JSONObject args = new JSONObject();
        args.put("channel", 1);
        args.put("user",    user);
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",    server);
        req.json().put("ts",    "cdb");
        req.json().put("inst",  CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT);
        req.json().put("args",  args);
        FjServerToolkit.getAnySender().send(req);
        try {WechatInterface.customSendTextMessage(user, "系统检测到您是第一次操作微信。如果您曾光顾过我们“VC电玩”淘宝官方商店，请回复以下信息以便于我们为您自动绑定和关联账户。回复格式：“”");}
        catch (WechatPermissionDeniedException | WechatCustomServiceException e) {logger.error("send bind tip to " + user + " failed", e);}
    }
    
    private static enum OperateMode {
        none,
        search,
    };
    
    private static class CacheUser {
        
        public BeanChannelAccount   user;
        public OperateMode          mode = OperateMode.none;
        
        public CacheUser(BeanChannelAccount user) {
            this.user = user;
        }
    }

}
