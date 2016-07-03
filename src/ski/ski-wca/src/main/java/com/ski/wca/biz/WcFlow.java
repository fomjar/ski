package com.ski.wca.biz;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.wca.WechatInterface;
import com.ski.wca.WechatInterface.WechatInterfaceException;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public abstract class WcFlow {
    
    private static final Logger logger = Logger.getLogger(WcFlow.class);
    
    public abstract String name();
    public String onActive  (CacheUser user) {return FjServerToolkit.getServerConfig(String.format("wca.flow.%s.%d", name(), 0));}
    public abstract String onRequest(String server, CacheUser user, String content);
    public String onDeactive(CacheUser user) {return FjServerToolkit.getServerConfig(String.format("wca.flow.%s.%d", name(), 9));}
    
    public String getTipByStep(CacheUser user) {
        return FjServerToolkit.getServerConfig(String.format("wca.flow.%s.%d", name(), user.step));
    }
    
    private static final Map<String, WcFlow>    flows = new HashMap<String, WcFlow>();
    private static final Map<String, CacheUser> cache = new HashMap<String, CacheUser>();
    
    private static final String FLOW_DEFAULT    = "0";
    private static final String FLOW_BIND       = "30";
    private static final String FLOW_APPEAL     = "322";
    private static final String FLOW_SUGGEST    = "323";
    private static final String FLOW_SEARCH     = "fsearch";
    
    public static void registerFlow(WcFlow flow) {
        flows.put(flow.name(), flow);
    }
    
    static {
        registerFlow(new FlowDefault());
        registerFlow(new FlowBind());
        registerFlow(new FlowAppeal());
        registerFlow(new FlowSuggest());
        registerFlow(new FlowSearch());
    }

    public static void dispatch(String server, String user, String content) {
        CacheUser cu = cache.get(user);
        if (null == cu) {
            cu = new CacheUser(user);
            cache.put(user, cu);
        }
        
        if (flows.containsKey(content)) {
            cu.toFlow(content);
            return;
        }
        
        String result = flows.get(cu.flow).onRequest(server, cu, content);
        if (null != result) {
            try {WechatInterface.customSendTextMessage(user, result);}
            catch (WechatInterfaceException e) {logger.error("send custom service message failed: " + result, e);}
        }
    }
    
    private static class CacheUser {
        public String   user;
        public String   flow;
        public int      step;
        public Map<String, Object> cache;
        
        public CacheUser(String user) {
            this.user = user;
            this.cache = new HashMap<String, Object>();
            toFlow(FLOW_DEFAULT);
        }
        
        public void toFlow(String flow) {
            WcFlow flow_old = flows.get(this.flow);
            if (null != flow_old) {
                String result = flow_old.onDeactive(this);
                if (null != result) {
                    try {WechatInterface.customSendTextMessage(user, result);}
                    catch (WechatInterfaceException e) {logger.error("send custom service message failed: " + result, e);}
                }
            }
            WcFlow flow_new = flows.get(flow);
            if (null != flow_new) {
                this.flow = flow;
                this.step = 0;
                String result = flow_new.onActive(this);
                if (null != result) {
                    try {WechatInterface.customSendTextMessage(user, result);}
                    catch (WechatInterfaceException e) {logger.error("send custom service message failed: " + result, e);}
                }
            }
            logger.debug(String.format("user: %s switch flow from: %s to: %s", user, null != flow_old ? flow_old.name() : null, null != flow_new ? flow_new.name() : null));
        }
    }
    
    private static class FlowDefault extends WcFlow {
        @Override
        public String name() {return FLOW_DEFAULT;}
        @Override
        public String onRequest(String server, CacheUser user, String content) {
            logger.info("on default flow request from user: " + user.user + " with content: " + content);
            return content;
        }
    }
    
    private static class FlowBind extends WcFlow {
        @Override
        public String name() {return FLOW_BIND;}
        @Override
        public String onActive(CacheUser user) {
            BeanChannelAccount user_wechat = CommonService.getChannelAccountByUser(user.user).get(0);    // 此处不会报错，微信用户肯定已创建
            if (1 < CommonService.getChannelAccountRelated(user_wechat.i_caid).size()) {    // 用户已经绑定过了，不能再绑定
                user.toFlow(FLOW_DEFAULT);
                return FjServerToolkit.getServerConfig("wca.error.bind-already");
            }
            return super.onActive(user);
        }
        @Override
        public String onRequest(String server, CacheUser user, String content) {
            switch (user.step) {
            case 0: {
                if (CommonService.getChannelAccountByUser(content).isEmpty()) return FjServerToolkit.getServerConfig("wca.error.wrong-taobao-user");    // 指定用户不存在
                BeanChannelAccount user_taobao = CommonService.getChannelAccountByUser(content).get(0);
                if (CommonService.CHANNEL_TAOBAO != user_taobao.i_channel)    return FjServerToolkit.getServerConfig("wca.error.wrong-taobao-user");      // 指定用户存在，但不是淘宝用户
                user.cache.put("user-taobao", user_taobao);
                user.step++;
                return getTipByStep(user);
            }
            case 1: {
                BeanChannelAccount user_taobao = (BeanChannelAccount) user.cache.get("user-taobao");
                if (!user_taobao.c_phone.equals(content)) { // 如果电话不匹配，尝试检查是否是支付宝账号
                    if (CommonService.getChannelAccountByUser(content).isEmpty()) return FjServerToolkit.getServerConfig("wca.error.wrong-phone-or-alipay");  // 淘宝电话和支付宝账号均不存在
                    BeanChannelAccount user_alipay = CommonService.getChannelAccountByUser(content).get(0);
                    if (CommonService.CHANNEL_ALIPAY != user_alipay.i_channel) return FjServerToolkit.getServerConfig("wca.error.wrong-phone-or-alipay");   // 指定用户存在，但不是支付宝用户
                }
                
                JSONObject args_cdb = new JSONObject();
                args_cdb.put("paid_to",     CommonService.getPlatformAccountByCaid(user_taobao.i_caid));
                args_cdb.put("paid_from",   CommonService.getPlatformAccountByCaid(CommonService.getChannelAccountByUser(user.user).get(0).i_caid));
                FjDscpMessage req = new FjDscpMessage();
                req.json().put("fs", server);
                req.json().put("ts", "cdb");
                req.json().put("inst", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE);
                req.json().put("args", args_cdb);
                FjServerToolkit.getAnySender().send(req);
                
                user.step++;
                String result = getTipByStep(user);
                user.toFlow(FLOW_DEFAULT);
                return result;
            }
            default:
                return null;
            }
            
        }
    }
    
    private static class FlowAppeal extends WcFlow {
        @Override
        public String name() {return FLOW_APPEAL;}
        @Override
        public String onRequest(String server, CacheUser user, String content) {
            user.step++;
            String result = getTipByStep(user);
            user.toFlow(FLOW_DEFAULT);
            return result;
        }
    }
    
    private static class FlowSuggest extends WcFlow {
        @Override
        public String name() {return FLOW_SUGGEST;}
        @Override
        public String onRequest(String server, CacheUser user, String content) {
            user.step++;
            String result = getTipByStep(user);
            user.toFlow(FLOW_DEFAULT);
            return result;
        }
    }
    
    private static class FlowSearch extends WcFlow {
        @Override
        public String name() {return FLOW_SEARCH;}
        @Override
        public String onRequest(String server, CacheUser user, String content) {
            String[] fields = content.split(" ");
            List<String> fields_list = new LinkedList<String>();
            for (String field : fields) if (0 < field.length()) fields_list.add(field);
            fields = fields_list.toArray(new String[fields_list.size()]);
            
            user.toFlow(FLOW_DEFAULT);
            return fields_list.toString();
        }
        
    }
    
}
