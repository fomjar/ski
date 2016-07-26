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
import com.ski.wca.monitor.TokenMonitor;

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
        ui = WechatInterface.userInfo(TokenMonitor.getInstance().token(), user);
        logger.info("register a new wechat user: " + ui);
        
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
            try {WechatInterface.messageCustomSendText(TokenMonitor.getInstance().token(), user, content.toString());}
            catch (WechatInterfaceException e) {logger.error("send custom message failed: " + content, e);}
            break;
        }
        case RESPONSE_TYPE_NOTIFY: {
            JSONObject content_json = (JSONObject) content;
            String template = content_json.getString("template");
            String url      = content_json.has("url") ? content_json.getString("url") : null;
            JSONObject data = content_json.getJSONObject("data");
            WechatInterface.messageTemplateSend(TokenMonitor.getInstance().token(), user, template, url, data);
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
            try {WechatInterface.messageCustomSendNews(TokenMonitor.getInstance().token(), user, articles.toArray(new WechatInterface.Article[articles.size()]));}
            catch (WechatCustomServiceException e) {logger.error("send news message failed: " + content, e);}
            break;
        }
        default:
            logger.error("unknown user response type: " + type);
            break;
        }
    }
    
    private static void dispatchCommand(String server, String user, JSONObject args) {

    }    
}
