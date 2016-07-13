package com.ski.wca.biz;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanGameAccount;
import com.ski.common.bean.BeanPlatformAccount;
import com.ski.wca.WechatForm;
import com.ski.wca.WechatInterface;

import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjJsonMessage;
import fomjar.server.msg.FjXmlMessage;
import net.sf.json.JSONObject;

public class WcWeb {
    
    private static final Logger logger = Logger.getLogger(WcWeb.class);
    public static final String URL_KEY = "/ski-wcweb";
    private static final String ROOT = "conf/form";
    
    private WcWeb() {}

    public static String generateUrl(String server, int inst, int user) {
        return generateUrl(server, URL_KEY, inst, user);
    }

    public static String generateUrl(String server, String url, int inst, int user) {
        FjAddress addr = FjServerToolkit.getSlb().getAddress(server);
        return String.format("http://%s%s%s?inst=%s&user=%s",
                addr.host,
                80 == addr.port ? "" : (":" + addr.port),
                url,
                Integer.toHexString(inst),
                Integer.toHexString(user));
    }
    
    public static void dispatch(String server, FjHttpRequest req, SocketChannel conn) {
        logger.info("user request url: " + req.url());
        logger.debug("user request data: " + req.content());
        
        String[]  form = null;
        String url = req.url().contains("?") ? req.url().substring(0, req.url().indexOf("?")) : req.url();
        switch (url) {
        case URL_KEY: {
            FjJsonMessage jreq = req.toJsonMessage(conn);
            if (jreq.json().has("inst") && jreq.json().has("user")) {
                int inst = Integer.parseInt(jreq.json().getString("inst"), 16);
                int user = Integer.parseInt(jreq.json().getString("user"), 16);
                switch(inst) {
                case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE:
                    form = processApplyPlatformAccountMerge(user, jreq.json());
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT:
                    form = processQueryPlatformAccount(user);
                    break;
                }
            }
            break;
        }
        case URL_KEY + "/pay/recharge": {
            FjJsonMessage jreq = req.toJsonMessage(conn);
            if (jreq.json().has("inst") && jreq.json().has("user")) {
                int inst = Integer.parseInt(jreq.json().getString("inst"), 16);
                int user = Integer.parseInt(jreq.json().getString("user"), 16);
                switch(inst) {
                case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY:
                    String terminal = "127.0.0.1";
                    try {terminal = ((InetSocketAddress) conn.getRemoteAddress()).getAddress().getHostAddress();}
                    catch (IOException e) {logger.error("get user terminal address failed", e);}
                    form = processApplyPlatformAccountMoney(server, terminal, user, jreq.json());
                    break;
                }
            }
            break;
        }
        case URL_KEY + "/pay/recharge/success": {
            FjXmlMessage xml = req.toXmlMessage(conn);
            logger.error("user pay recharge: " + xml);
            form = processPayRechargeSuccess(server, xml.xml());
            break;
        }
        default: {
            form = fetchFile(req.url());
            break;
        }
        }
        
        if (null != form) {
            logger.debug("user response data: " + form[1]);
            WechatInterface.sendResponse(form[0], form[1], conn);
        } else {
            try {conn.close();}
            catch (IOException e) {e.printStackTrace();}
        }
    }
    
    private static String[] processApplyPlatformAccountMerge(int user, JSONObject args) {
        String step = args.has("step") ? args.getString("step") : "setup";
        String ct   = null;
        String form = null;
        if (1 < CommonService.getChannelAccountRelated(user).size()) {  // 用户已经绑定过了
            ct = FjHttpRequest.CT_HTML;
            form = WechatForm.createFormMessage(WechatForm.MESSAGE_SUCCESS, "关联成功", "您的微信和淘宝已成功关联，现在可以到“我的账户”中查看相关信息，感谢您的支持", null, null);
        } else {
            switch (step) {
            case "setup":
                ct = FjHttpRequest.CT_HTML;
                String[] file = fetchFile("/apply_platform_account_merge.html");
                ct      = file[0];
                form    = String.format(file[1], CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, user);
                break;
            case "apply":
                ct = FjHttpRequest.CT_JSON;
                JSONObject form_args = new JSONObject();
                String to_user  = args.getString("to_user");
                String to_phone = args.getString("to_phone");
                List<BeanChannelAccount> user_taobaos = CommonService.getChannelAccountByUser(to_user);
                if (user_taobaos.isEmpty()) {
                    form_args.put("code", -1);
                    form_args.put("desc", "我们没有招待过此淘宝用户，请重新输入");
                    form = form_args.toString();
                    break;
                }
                
                BeanChannelAccount user_taobao = user_taobaos.get(0);
                if (CommonService.CHANNEL_TAOBAO != user_taobao.i_channel) {
                    form_args.put("code", -1);
                    form_args.put("desc", "我们没有招待过此淘宝用户，请重新输入");
                    form = form_args.toString();
                    break;
                }
                if (!user_taobao.c_phone.equals(to_phone)) {
                    form_args.put("code", -1);
                    form_args.put("desc", "填写的手机号跟淘宝上使用的手机不匹配，请重新输入");
                    form = form_args.toString();
                    break;
                }
                
                {
                    JSONObject args_cdb = new JSONObject();
                    args_cdb.put("paid_to",     CommonService.getPlatformAccountByCaid(user_taobao.i_caid));
                    args_cdb.put("paid_from",   CommonService.getPlatformAccountByCaid(user));
                    FjDscpMessage req_cdb = new FjDscpMessage();
                    req_cdb.json().put("fs", "wcweb");
                    req_cdb.json().put("ts", "cdb");
                    req_cdb.json().put("inst", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE);
                    req_cdb.json().put("args", args_cdb);
                    FjServerToolkit.getAnySender().send(req_cdb);
                    form_args.put("code", 0);
                    form = form_args.toString();
                }
                break;
            case "success":
                ct = FjHttpRequest.CT_HTML;
                form = WechatForm.createFormMessage(WechatForm.MESSAGE_SUCCESS, "关联成功", "您的微信和淘宝已成功关联，现在可以到“我的账户”中查看相关信息，感谢您的支持", null, null);
                break;
            }
        }
        return new String[] {ct, form};
    }
    
    private static String[] processQueryPlatformAccount(int user) {
        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(user));
        StringBuilder sb = new StringBuilder();
        sb.append(WechatForm.createFormHead(WechatForm.FORM_CELL, "账户明细"));
        {
            List<String[]> cells = new LinkedList<String[]>();
            cells.add(new String[] {"现金", puser.i_cash + "元"});
            cells.add(new String[] {"优惠券", puser.i_coupon + "元"});
            float[] prestatement = CommonService.prestatement(user);
            cells.add(new String[] {"现金(实时)", prestatement[0] + "元"});
            cells.add(new String[] {"优惠券(实时)", prestatement[1] + "元"});
            sb.append(WechatForm.createFormCellGroup("我的账户", cells, null));
        }
        {
            CommonService.getOrderByCaid(user)
                    .stream()
                    .filter(o->!o.isClose())
                    .forEach(o->{
                        o.commodities.values()
                                .stream()
                                .filter(c->!c.isClose())
                                .forEach(c->{
                                    BeanGameAccount account = CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16));
                                    String games = CommonService.getGameByGaid(account.i_gaid).stream().map(g->g.c_name_zh).collect(Collectors.joining("; "));
                                    {
                                        List<String[]> cells = new LinkedList<String[]>();
                                        cells.add(new String[] {"游戏账号", account.c_user});
                                        cells.add(new String[] {"当前密码", account.c_pass_curr});
                                        cells.add(new String[] {"租赁类型", "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"});
                                        cells.add(new String[] {"包含游戏", games});
                                        cells.add(new String[] {"租赁单价", c.i_price + "元/天"});
                                        cells.add(new String[] {"起租时间", c.t_begin});
                                        sb.append(WechatForm.createFormCellGroup("在租游戏：" + games, cells, null));
                                        cells.clear();
                                        cells.add(new String[] {"账号操作", "退租", "javascript:;"});
                                        sb.append(WechatForm.createFormCellAccessGroup(null, cells, null));
                                    }
                                });
                    });
        }
        sb.append(WechatForm.createFormFoot());
        return new String[] {FjHttpRequest.CT_HTML, sb.toString()};
    }
    
    private static String[] processApplyPlatformAccountMoney(String server, String terminal, int user, JSONObject args) {
        String step = args.has("step") ? args.getString("step") : "setup";
        String ct   = null;
        String form = null;
        switch (step) {
        case "setup": {
            String[] file = fetchFile("/apply_platform_account_money.html");
            ct              = file[0];
            long timestamp  = System.currentTimeMillis() / 1000;
            String nonceStr = Long.toHexString(System.currentTimeMillis());
            ct      = FjHttpRequest.CT_HTML;
            form    = String.format(file[1],
                    CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY,
                    user,
                    FjServerToolkit.getServerConfig("wca.appid"),
                    timestamp,
                    nonceStr,
                    WechatInterface.createSignature4Config(nonceStr, timestamp, WcWeb.generateUrl(server, CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, user)));
            break;
        }
        case "apply": {
            String money = args.getString("money");
            String url = generateUrl(server, URL_KEY + "/pay/recharge/success", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, user);
            url = url.substring(0, url.indexOf("?"));
            FjXmlMessage rsp = WechatInterface.prepay(
                    "VC电玩-充值",
                    "您已成功充值" + money + "元",
                    (int) (Float.parseFloat(money) * 100),
                    terminal,
                    url,
                    CommonService.getChannelAccountByCaid(user).c_user);
            
            String timeStamp    = String.valueOf(System.currentTimeMillis() / 1000);
            String nonceStr     = Long.toHexString(System.currentTimeMillis());
            JSONObject json_prepay = xml2json(rsp.xml());
            
            Map<String, String> map = new HashMap<String, String>();
            map.put("appId",        FjServerToolkit.getServerConfig("wca.appid"));
            map.put("timeStamp",    timeStamp);
            map.put("nonceStr",     nonceStr);
            map.put("package",      "prepay_id=" + json_prepay.getString("prepay_id"));
            map.put("signType",     "MD5");
            String paySign = WechatInterface.createSignature4Pay(map);
            
            JSONObject json_pay = new JSONObject();
            json_pay.put("appId",       FjServerToolkit.getServerConfig("wca.appid"));
            json_pay.put("timeStamp",   timeStamp);
            json_pay.put("nonceStr",    nonceStr);
            json_pay.put("package",     "prepay_id=" + json_prepay.getString("prepay_id"));
            json_pay.put("signType",    "MD5");
            json_pay.put("paySign",     paySign);
            
            json_prepay.put("pay", json_pay);
            ct      = FjHttpRequest.CT_JSON;
            form    = json_prepay.toString();
            break;
        }
        case "success": {
            ct = FjHttpRequest.CT_HTML;
            form = WechatForm.createFormMessage(WechatForm.MESSAGE_SUCCESS, "充值成功", "现在可以到“我的账户”中查看账户余额，感谢您的支持", null, null);
            break;
        }
        }
        return new String[] {ct, form};
    }
    
    private static Set<String> cache_trade = new HashSet<String>();
    /**
     * <xml>
     * <appid><![CDATA[wx9c65a26e4f512fd4]]></appid>
     * <attach><![CDATA[您已成功充值1元]]></attach>
     * <bank_type><![CDATA[CFT]]></bank_type>
     * <cash_fee><![CDATA[100]]></cash_fee>
     * <device_info><![CDATA[WEB]]></device_info>
     * <fee_type><![CDATA[CNY]]></fee_type>
     * <is_subscribe><![CDATA[Y]]></is_subscribe>
     * <mch_id><![CDATA[1364744702]]></mch_id>
     * <nonce_str><![CDATA[155e55b5e2f4ec1f9d155793]]></nonce_str>
     * <openid><![CDATA[oRojEwPTK3o2cYrLsXuuX-FuypBM]]></openid>
     * <out_trade_no><![CDATA[20160714014338288]]></out_trade_no>
     * <result_code><![CDATA[SUCCESS]]></result_code>
     * <return_code><![CDATA[SUCCESS]]></return_code>
     * <sign><![CDATA[5B66E346DF7FAE2A508A933092AB6590]]></sign>
     * <time_end><![CDATA[20160714014342]]></time_end>
     * <total_fee>100</total_fee>
     * <trade_type><![CDATA[JSAPI]]></trade_type>
     * <transaction_id><![CDATA[4003922001201607148930843003]]></transaction_id>
     * </xml>
     * 
     * @param xml
     */
    private static String[] processPayRechargeSuccess(String server, Document xml) {
        String user     = null;
        float  money    = 0.00f;
        String trade    = null;
        NodeList nodes = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (null == node.getFirstChild()) continue;
            
            if (node.getNodeName().equals("openid"))        user    = node.getFirstChild().getNodeValue();
            if (node.getNodeName().equals("cash_fee"))      money   = Float.parseFloat(node.getFirstChild().getNodeValue()) / 100;
            if (node.getNodeName().equals("out_trade_no"))  trade = node.getFirstChild().getNodeValue();
        }
        if (!cache_trade.contains(trade)) {
            cache_trade.add(trade);
            int paid = CommonService.getPlatformAccountByCaid(CommonService.getChannelAccountByUser(user).get(0).i_caid);
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("paid",    paid);
            args_cdb.put("remark",  "【微信充值】公众号充值" + money + "元");
            args_cdb.put("type",    CommonService.MONEY_CASH);
            args_cdb.put("money",   money);
            FjDscpMessage msg_cdb = new FjDscpMessage();
            msg_cdb.json().put("fs",    server);
            msg_cdb.json().put("ts",    "cdb");
            msg_cdb.json().put("inst",  CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY);
            msg_cdb.json().put("args",  args_cdb);
            FjServerToolkit.getAnySender().send(msg_cdb);
        }
        
        return new String[] {FjHttpRequest.CT_XML, "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>"};
    }
    
    private static String[] fetchFile(String url) {
        
        File file = new File(ROOT + (url.startsWith(URL_KEY) ? url.substring(URL_KEY.length()) : url));
        if (!file.isFile()) {
            logger.warn("not such file to fetch: " + file.getPath());
            return new String[] {FjHttpRequest.CT_TEXT, ""};
        }
        
        FileInputStream         fis = null;
        ByteArrayOutputStream   baos = null;
        try {
            byte[]  buf = new byte[1024];
            int     len = -1;
            fis     = new FileInputStream(file);
            baos    = new ByteArrayOutputStream();
            while (0 < (len = fis.read(buf))) baos.write(buf, 0, len);
            return new String[] {getFileMime(file.getName()), baos.toString("utf-8")};
        } catch (IOException e) {logger.error("fetch file failed, url: " + url, e);}
        finally {
            try {
                fis.close();
                baos.close();
            } catch (IOException e) {e.printStackTrace();}
        }
        return new String[] {FjHttpRequest.CT_TEXT, ""};
    }
    
    private static String getFileMime (String name) {
        if (!name.contains(".")) return FjHttpRequest.CT_TEXT;
        
        String ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        switch (ext) {
        case "html":
        case "htm":     return FjHttpRequest.CT_HTML;
        case "js":      return FjHttpRequest.CT_JS;
        case "css":
        case "less":    return FjHttpRequest.CT_CSS;
        case "xml":     return FjHttpRequest.CT_XML;
        case "json":    return FjHttpRequest.CT_JSON;
        default:    return FjHttpRequest.CT_TEXT;
        }
    }
    
    private static JSONObject xml2json(Document xml) {
        JSONObject json = new JSONObject();
        NodeList nodes = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (null == node.getFirstChild()) continue;
            json.put(node.getNodeName(), node.getFirstChild().getNodeValue());
        }
        return json;
    }
}
