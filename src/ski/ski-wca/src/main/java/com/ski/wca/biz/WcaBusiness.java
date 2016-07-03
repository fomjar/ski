package com.ski.wca.biz;

import java.util.List;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.wca.WechatInterface;
import com.ski.wca.WechatInterface.WechatInterfaceException;
import com.ski.wca.WechatInterface.WechatPermissionDeniedException;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjJsonMessage;
import net.sf.json.JSONObject;

public class WcaBusiness {
    
    private static final Logger logger = Logger.getLogger(WcaBusiness.class);
    public static final String URL_KEY = "/ski-wca";
    
    private WcaBusiness() {}
    
    public static void dispatch(String server, FjDscpMessage req) {
        if (!req.argsToJsonObject().has("user")) return;
        
        String user     = req.argsToJsonObject().getString("user");
        String content  = req.argsToJsonObject().has("content") ? req.argsToJsonObject().getString("content") : null;
        
        verifyUser(server, user);
        
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
    private static void verifyUser(String server, String user) {
        List<BeanChannelAccount> users = CommonService.getChannelAccountByUser(user);
        if (!users.isEmpty()
                && CommonService.CHANNEL_WECHAT == users.get(0).i_channel)
            return;
        
        registerUser(server, user);
        CommonService.updateChannelAccount();
    }
    
    private static void registerUser(String server, String user) {
        FjJsonMessage ui = null;
        try {
            ui = WechatInterface.userInfo(user);
            logger.info("register a new wechat user: " + ui);
        } catch (WechatPermissionDeniedException e) {
            logger.error("get user info failed: " + user, e);
            return;
        }
        
        JSONObject args = new JSONObject();
        args.put("channel", CommonService.CHANNEL_WECHAT);
        args.put("user",    user);
        args.put("name",    ui.json().getString("nickname"));
        args.put("gender",  convertSex(ui.json().getInt("sex")));
        args.put("address", String.format("%s %s %s", ui.json().getString("country"), ui.json().getString("province"), ui.json().getString("city")));
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",    server);
        req.json().put("ts",    "cdb");
        req.json().put("inst",  CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT);
        req.json().put("args",  args);
        FjServerToolkit.getAnySender().send(req);
    }
    
    private static int convertSex(int sex) {
        switch (sex) {
        case 1: return CommonService.GENDER_MALE;
        case 2: return CommonService.GENDER_FEMALE;
        case 0:
        default: return CommonService.GENDER_UNKNOWN;
        }
    }
    
}
