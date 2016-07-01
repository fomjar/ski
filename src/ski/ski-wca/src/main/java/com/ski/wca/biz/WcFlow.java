package com.ski.wca.biz;

import java.util.HashMap;
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
    
    private static final String FLOW_DEFAULT    = "fdefault";
    private static final String FLOW_BIND       = "fbind";
    
    static {
        WcFlow flow_default = new FlowDefault();
        flows.put(flow_default.name(), flow_default);
        WcFlow flow_bind    = new FlowBind();
        flows.put(flow_bind.name(), flow_bind);
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
        public String onRequest(String server, CacheUser user, String content) {return null;}
    }
    
    private static class FlowBind extends WcFlow {
        @Override
        public String name() {return FLOW_BIND;}
        @Override
        public String onActive(CacheUser user) {
            BeanChannelAccount user_wechat = CommonService.getChannelAccountByUserName(user.user).get(0);    // 此处不会报错，微信用户肯定已创建
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
                if (CommonService.getChannelAccountByUserName(content).isEmpty()) return FjServerToolkit.getServerConfig("wca.error.wrong-taobao-user");    // 指定用户不存在
                BeanChannelAccount user_taobao = CommonService.getChannelAccountByUserName(content).get(0);
                if (CommonService.USER_TYPE_TAOBAO != user_taobao.i_channel)    return FjServerToolkit.getServerConfig("wca.error.wrong-taobao-user");      // 指定用户存在，但不是淘宝用户
                user.cache.put("user-taobao", user_taobao);
                user.step++;
                return getTipByStep(user);
            }
            case 1: {
                BeanChannelAccount user_taobao = (BeanChannelAccount) user.cache.get("user-taobao");
                if (!user_taobao.c_phone.equals(content)) { // 如果电话不匹配，尝试检查是否是支付宝账号
                    if (CommonService.getChannelAccountByUserName(content).isEmpty()) return FjServerToolkit.getServerConfig("wca.error.wrong-phone-or-alipay");  // 淘宝电话和支付宝账号均不存在
                    BeanChannelAccount user_alipay = CommonService.getChannelAccountByUserName(content).get(0);
                    if (CommonService.USER_TYPE_ALIPAY != user_alipay.i_channel) return FjServerToolkit.getServerConfig("wca.error.wrong-phone-or-alipay");   // 指定用户存在，但不是支付宝用户
                }
                
                JSONObject args_cdb = new JSONObject();
                args_cdb.put("paid_to",     CommonService.getPlatformAccountByCaid(user_taobao.i_caid));
                args_cdb.put("paid_from",   CommonService.getPlatformAccountByCaid(CommonService.getChannelAccountByUserName(user.user).get(0).i_caid));
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
    
}
