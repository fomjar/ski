package com.ski.wca.biz;

import java.util.LinkedList;
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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class WcaBusiness {
    
    private static final Logger logger = Logger.getLogger(WcaBusiness.class);
    public static final String URL_KEY = "/ski-wca";
    
    private static final String RESPONSE_TYPE_TEXT      = "text";
    private static final String RESPONSE_TYPE_NOTIFY    = "notify";
    private static final String RESPONSE_TYPE_NEWS      = "news";
    
    private WcaBusiness() {}
    
    public static void dispatch(String server, FjDscpMessage req) {
        JSONObject  args    = req.argsToJsonObject();
        if (!args.has("user")) return;
        
        String      user    = args.getString("user");
        verifyUser(server, user);
        
        switch (req.inst()) {
        case CommonDefinition.ISIS.INST_USER_RESPONSE:
            dispatchResponse(user, args);
            break;
        case CommonDefinition.ISIS.INST_USER_REQUEST:
            break;
        case CommonDefinition.ISIS.INST_USER_COMMAND:
            dispatchCommand(server, user, args);
            break;
        case CommonDefinition.ISIS.INST_USER_SUBSCRIBE:
            break;
        case CommonDefinition.ISIS.INST_USER_UNSUBSCRIBE:
            break;
        case CommonDefinition.ISIS.INST_USER_VIEW:
            break;
        case CommonDefinition.ISIS.INST_USER_LOCATION:
            break;
        case CommonDefinition.ISIS.INST_USER_NOTIFY:
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
    
    @SuppressWarnings("unchecked")
    private static void dispatchResponse(String user, JSONObject args) {
        String  type    = args.has("type") ? args.getString("type") : RESPONSE_TYPE_TEXT;
        Object  content = args.get("content");
        
        switch (type) {
        case RESPONSE_TYPE_TEXT: {
            try {WechatInterface.messageCustomSendText(user, content.toString());}
            catch (WechatInterfaceException e) {logger.error("send custom message failed: " + content, e);}
            break;
        }
        case RESPONSE_TYPE_NOTIFY: {
            JSONObject content_json = (JSONObject) content;
            String template = content_json.getString("template");
            String url      = content_json.has("url") ? content_json.getString("url") : null;
            JSONObject data = content_json.getJSONObject("data");
            try {WechatInterface.messageTemplateSend(user, template, url, data);}
            catch (WechatPermissionDeniedException e) {logger.error("send template message failed: " + content, e);}
            break;
        }
        case RESPONSE_TYPE_NEWS: {
            JSONArray content_json = (JSONArray) content;
            List<WechatInterface.Article> articles = new LinkedList<WechatInterface.Article>();
            content_json.forEach(arg->{
                JSONObject article = (JSONObject) arg;
                articles.add(new WechatInterface.Article(article.getString("title"),
                        article.getString("description"),
                        article.getString("url"),
                        article.getString("picurl")));
            });
            try {WechatInterface.messageCustomSendNews(user, articles.toArray(new WechatInterface.Article[articles.size()]));}
            catch (WechatPermissionDeniedException | WechatCustomServiceException e) {logger.error("send news message failed: " + content, e);}
            break;
        }
        default:
            logger.error("unknown user response type: " + type);
            break;
        }
    }
    
    private static void dispatchCommand(String server, String user, JSONObject args) {
        String              cmd         = args.getString("cmd");
        BeanChannelAccount  user_wechat = CommonService.getChannelAccountByUser(user).get(0);    // 此处不会报错，微信用户肯定已创建
        
        switch (cmd) {
        case "21":  // 所有游戏
            try {
                WechatInterface.messageCustomSendNews(user, new WechatInterface.Article[] {
                        new WechatInterface.Article("热门游戏", "热门游戏", "https://www.baidu.com/", "http://findicons.com/icon/download/203236/stock_people/128/png?id=378556"),
                        new WechatInterface.Article("最新大作", "最新大作", "https://www.baidu.com/", "http://findicons.com/icon/download/177279/currency_yuan_blue/128/png?id=177539"),
                });
            } catch (WechatPermissionDeniedException | WechatCustomServiceException e) {logger.error("send custom service news message failed", e);}
            break;
        case "22":  // 搜索游戏
            try {
                WechatInterface.messageCustomSendNews(user, new WechatInterface.Article[] {
                        new WechatInterface.Article("搜索游戏", "搜索游戏",
                                WcWeb.generateUrl(server, CommonDefinition.ISIS.INST_ECOM_QUERY_GAME, user_wechat.i_caid),
                                "http://findicons.com/icon/download/203236/stock_people/128/png?id=378556"),
                });
            } catch (WechatPermissionDeniedException | WechatCustomServiceException e) {logger.error("send custom service news message failed", e);}
            break;
        case "30":  // 关联淘宝
            try {
                WechatInterface.messageCustomSendNews(user, new WechatInterface.Article[] {
                        new WechatInterface.Article("关联淘宝账号", "为了能够更便捷地为您提供服务，如果您曾经光临过“VC电玩”淘宝店，可以在此进行账号关联",
                                WcWeb.generateUrl(server, CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, user_wechat.i_caid),
                                "http://findicons.com/icon/download/203236/stock_people/128/png?id=378556"),
                });
            } catch (WechatPermissionDeniedException | WechatCustomServiceException e) {logger.error("send custom service news message failed", e);}
            break;
        case "31":  // 我的账户
            try {
                WechatInterface.messageCustomSendNews(user, new WechatInterface.Article[] {
                        new WechatInterface.Article("账户信息", "查看账户余额、优惠券、在租账号等信息", 
                                WcWeb.generateUrl(server, CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT, user_wechat.i_caid), "http://findicons.com/icon/download/203236/stock_people/128/png?id=378556"),
                        new WechatInterface.Article("我要充值", "起租游戏之前需要先充值",
                                WcWeb.generateUrl(server, WcWeb.URL_KEY + "/pay/recharge", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, user_wechat.i_caid), "http://findicons.com/icon/download/177279/currency_yuan_blue/128/png?id=177539"),
                        new WechatInterface.Article("我要退款", "申请将账户中的余额全额退款",
                                WcWeb.generateUrl(server, WcWeb.URL_KEY + "/pay/refund", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, user_wechat.i_caid), "http://findicons.com/icon/download/28731/coins/128/png?id=271105"),
                        new WechatInterface.Article("消费记录", "查看过去的消费记录",
                                WcWeb.generateUrl(server, CommonDefinition.ISIS.INST_ECOM_QUERY_ORDER, user_wechat.i_caid), "http://findicons.com/icon/download/93344/type_list/128/png?id=94878"),
                });
            } catch (WechatPermissionDeniedException | WechatCustomServiceException e) {logger.error("send custom service news message failed", e);}
            break;
        }
    }    
}
