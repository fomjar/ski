package com.ski.wca.biz;

import java.util.List;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.wca.WechatInterface;
import com.ski.wca.WechatInterface.WechatInterfaceException;

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
    
    private WcaBusiness() {}
    
    public void setServer(String server) {
        this.server = server;
    }
    
    public void dispatch(FjDscpMessage req) {
        if (!req.argsToJsonObject().has("user")) return;
        
        String user     = req.argsToJsonObject().getString("user");
        String content  = req.argsToJsonObject().has("content") ? req.argsToJsonObject().getString("content") : null;
        
        if (!verifyUser(user)) return;
        
        switch (req.inst()) {
        case CommonDefinition.ISIS.INST_USER_RESPONSE: {
            try {WechatInterface.customSendTextMessage(user, content);}
            catch (WechatInterfaceException e) {logger.error("send custom service message failed: " + content, e);}
            break;
        }
        case CommonDefinition.ISIS.INST_USER_REQUEST:
        case CommonDefinition.ISIS.INST_USER_COMMAND: {
            if (WcMenu.isMenu(content)) WcMenu.dispatch(server, user, content);
            else WcFlow.dispatch(server, user, content);
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
        List<BeanChannelAccount> users = CommonService.getChannelAccountByUserName(user);
        if (!users.isEmpty()
                && CommonService.USER_TYPE_WECHAT == users.get(0).i_channel) {
            return true;
        }
        
        registerUser(user);
        logger.info("registered a new wechat user: " + user);
        
        return false;
    }
    
    private void registerUser(String user) {
        JSONObject args = new JSONObject();
        args.put("channel", CommonService.USER_TYPE_WECHAT);
        args.put("user",    user);
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",    server);
        req.json().put("ts",    "cdb");
        req.json().put("inst",  CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT);
        req.json().put("args",  args);
        FjServerToolkit.getAnySender().send(req);
    }
    
}
