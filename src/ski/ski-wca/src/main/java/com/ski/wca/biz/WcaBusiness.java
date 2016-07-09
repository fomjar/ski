package com.ski.wca.biz;

import java.nio.channels.SocketChannel;
import java.util.List;

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
import fomjar.server.msg.FjJsonMessage;
import net.sf.json.JSONObject;

public class WcaBusiness {
    
    private static final Logger logger = Logger.getLogger(WcaBusiness.class);
    public static final String URL_KEY = "/ski-wca";
    
    private WcaBusiness() {}
    
    public static void dispatch(String server, FjDscpMessage req, SocketChannel conn) {
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
            break;
        case CommonDefinition.ISIS.INST_USER_COMMAND:
            dispatchMenu(server, user, content, conn);
            break;
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
    
    private static void dispatchMenu(String server, String user, String content, SocketChannel conn) {
        logger.debug(String.format("user: %s select menu: %s", user, content));
        BeanChannelAccount user_wechat = CommonService.getChannelAccountByUser(user).get(0);    // 此处不会报错，微信用户肯定已创建
        switch (content) {
        case "21":
            break;
        case "22":
            break;
        case "30":  // 关联淘宝
            try {
                WechatInterface.customSendNewsMessage(user, new WechatInterface.Article[] {
                        new WechatInterface.Article("关联淘宝账号", "为了能够更便捷地为您提供服务，如果您曾经光临过“VC电玩”淘宝店，可以在此进行账号关联",
                                WcWeb.generateUrl(server, CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, user_wechat.i_caid),
                                "http://findicons.com/icon/download/203236/stock_people/128/png?id=378556"),
                });
            } catch (WechatPermissionDeniedException | WechatCustomServiceException e) {logger.error("send custom service news message failed", e);}
            break;
        case "31":  //
            try {
                WechatInterface.customSendNewsMessage(user, new WechatInterface.Article[] {
                        new WechatInterface.Article("账户信息", "查看账户余额、优惠券、在租账号等信息", 
                                WcWeb.generateUrl(server, CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT, user_wechat.i_caid), "http://findicons.com/icon/download/203236/stock_people/128/png?id=378556"),
                        new WechatInterface.Article("我要充值", "起租游戏之前需要先充值",
                                WcWeb.generateUrl(server, CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, user_wechat.i_caid), "http://findicons.com/icon/download/177279/currency_yuan_blue/128/png?id=177539"),
                        new WechatInterface.Article("我要退款", "申请将账户中的余额全额退款", "https://www.baidu.com/", "http://findicons.com/icon/download/28731/coins/128/png?id=271105"),
                        new WechatInterface.Article("消费记录", "查看过去的消费记录", "https://www.baidu.com/", "http://findicons.com/icon/download/93344/type_list/128/png?id=94878"),
                });
            } catch (WechatPermissionDeniedException | WechatCustomServiceException e) {logger.error("send custom service news message failed", e);}
            break;
        }
    }    
}
