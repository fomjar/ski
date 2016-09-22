package com.ski.web.wechat;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.web.wechat.WechatInterface.WechatCustomServiceException;
import com.ski.web.wechat.WechatInterface.WechatInterfaceException;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjJsonMessage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class WechatBusiness {
    
    private static final Logger logger = Logger.getLogger(WechatBusiness.class);
    public static final String URL_KEY = "/ski-wechat";
    
    private static final String RESPONSE_TYPE_TEXT      = "text";
    private static final String RESPONSE_TYPE_NOTIFY    = "notify";
    private static final String RESPONSE_TYPE_NEWS      = "news";
    
    private WechatTokenMonitor  mon_token   = new WechatTokenMonitor();
    private WechatMenuMonitor   mon_menu    = new WechatMenuMonitor(mon_token);
    
    public WechatBusiness() {
        mon_token   = new WechatTokenMonitor();
        mon_menu    = new WechatMenuMonitor(mon_token);
    }
    
    public void open() {
        mon_token.start();
        mon_menu.start();
    }
    
    public void close() {
        mon_token.close();
        mon_menu.close();
    }
    
    public WechatTokenMonitor token_monitor() {
        return mon_token;
    }
    
    public void dispatch(FjDscpMessage req) {
        JSONObject  args    = req.argsToJsonObject();
        if (!args.has("user")) return;
        
        String user = args.getString("user");
        verifyUser(user);
        
        switch (req.inst()) {
        case CommonDefinition.ISIS.INST_USER_RESPONSE:
            dispatchResponse(user, args);
            break;
        case CommonDefinition.ISIS.INST_USER_REQUEST:
            break;
        case CommonDefinition.ISIS.INST_USER_COMMAND:
            dispatchCommand(user, args);
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
    private void verifyUser(String user) {
        List<BeanChannelAccount> users = CommonService.getChannelAccountByUserNChannel(user, CommonService.CHANNEL_WECHAT);
        int caid = -1;
        if (!users.isEmpty()) caid = users.get(0).i_caid;
        
        updateUser(caid, user);
        if (-1 == caid) CommonService.updateChannelAccount();
    }
    
    private void updateUser(int caid, String user) {
        FjJsonMessage ui = null;
        ui = WechatInterface.userInfo(mon_token.token(), user);
        
        JSONObject args = new JSONObject();
        if (-1 != caid) args.put("caid", caid);
        args.put("channel", CommonService.CHANNEL_WECHAT);
        args.put("user",    user);
        args.put("name",    ui.json().getString("nickname"));
        args.put("gender",  convertSex(ui.json().getInt("sex")));
        args.put("address", String.format("%s %s %s", ui.json().getString("country"), ui.json().getString("province"), ui.json().getString("city")));
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",    FjServerToolkit.getAnyServer().name());
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
    private void dispatchResponse(String user, JSONObject args) {
        String  type    = args.has("type") ? args.getString("type") : RESPONSE_TYPE_TEXT;
        Object  content = args.get("content");
        
        switch (type) {
        case RESPONSE_TYPE_TEXT: {
            try {WechatInterface.messageCustomSendText(mon_token.token(), user, content.toString());}
            catch (WechatInterfaceException e) {logger.error("send custom message failed: " + content, e);}
            break;
        }
        case RESPONSE_TYPE_NOTIFY: {
            JSONObject content_json = (JSONObject) content;
            String template = content_json.getString("template");
            String url      = content_json.has("url") ? content_json.getString("url") : null;
            JSONObject data = content_json.getJSONObject("data");
            WechatInterface.messageTemplateSend(mon_token.token(), user, template, url, data);
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
            try {WechatInterface.messageCustomSendNews(mon_token.token(), user, articles.toArray(new WechatInterface.Article[articles.size()]));}
            catch (WechatCustomServiceException e) {logger.error("send news message failed: " + content, e);}
            break;
        }
        default:
            logger.error("unknown user response type: " + type);
            break;
        }
    }
    
    private static void dispatchCommand(String user, JSONObject args) {
        switch (args.getString("cmd")) {
        }
    }    
}
