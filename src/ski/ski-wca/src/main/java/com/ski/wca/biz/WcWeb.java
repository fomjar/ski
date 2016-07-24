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
import com.ski.common.bean.BeanGame;
import com.ski.common.bean.BeanGameAccount;
import com.ski.common.bean.BeanPlatformAccount;
import com.ski.wca.WechatForm;
import com.ski.wca.WechatInterface;

import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjXmlMessage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class WcWeb {
    
    private static final Logger logger = Logger.getLogger(WcWeb.class);
    public  static final String URL_KEY = "/ski-wcweb";
    private static final String ROOT = "conf/form";
    private static final String STEP_PREPARE    = "prepare";
    private static final String STEP_SETUP      = "setup";
    private static final String STEP_APPLY      = "apply";
    private static final String STEP_SUCCESS    = "success";
    
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
        
        String url = req.url().contains("?") ? req.url().substring(0, req.url().indexOf("?")) : req.url();
        WcwResponse response = new WcwResponse();
        if (url.equals(URL_KEY + "/pay/recharge/success")) {
            Document xml = req.contentToXml();
            processPayRechargeSuccess(response, server, xml);
        } else {
            JSONObject args = req.contentToJson();
            if (null != req.urlArgs()) args.putAll(req.urlArgs());
            WcwRequest request = new WcwRequest();
            request.server = server;
            if (args.has("inst")) request.inst = Integer.parseInt(args.getString("inst"), 16);
            if (args.has("user")) request.user = Integer.parseInt(args.getString("user"), 16);
            request.step = args.has("step") ? args.getString("step") : STEP_SETUP;
            request.args = args;
            request.conn = conn;
            switch (url) {
            case URL_KEY: {
                switch(request.inst) {
                case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE:
                    processApplyPlatformAccountMerge(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT:
                    processQueryPlatformAccount(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_GAME:
                    processQueryGame(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT:
                    processUpdateChannelAccount(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_ORDER:
                    processQueryOrder(response, request);
                }
                break;
            }
            case URL_KEY + "/pay/recharge": {
                switch(request.inst) {
                case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY:
                    processApplyPlatformAccountMoney_Recharge(response, request);
                    break;
                }
                break;
            }
            case URL_KEY + "/pay/refund": {
                switch(request.inst) {
                case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY:
                    processApplyPlatformAccountMoney_Refund(response, request);
                    break;
                }
                break;
            }
            default: {
                fetchFile(response, req.url());
                break;
            }
            }
        }
        
        if (null != response.content) {
            logger.debug("user response data: " + response.content);
            WechatInterface.sendResponse(response.type, response.content, conn);
        } else {
            try {conn.close();}
            catch (IOException e) {e.printStackTrace();}
        }
    }
    
    private static void processApplyPlatformAccountMerge(WcwResponse response, WcwRequest request) {
        if (!CommonService.getChannelAccountRelatedByCaidNChannel(request.user, CommonService.CHANNEL_TAOBAO).isEmpty()) {  // 用户已经绑定过了
            response.type       = FjHttpRequest.CT_HTML;
            response.content    = WechatForm.createFormMessage(WechatForm.MESSAGE_SUCCESS, "关联成功", "您的微信和淘宝已成功关联，现在可以到“我的账户”中查看相关信息，感谢您的支持", null, null);
            return;
        }
        
        switch (request.step) {
        case STEP_SETUP:
            fetchFile(response, "/apply_platform_account_merge.html");
            response.content = String.format(response.content,
                    CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE,
                    request.user);
            break;
        case STEP_APPLY:
            response.type = FjHttpRequest.CT_JSON;
            JSONObject content = new JSONObject();
            String to_user  = request.args.getString("to_user");
            String to_phone = request.args.getString("to_phone");
            List<BeanChannelAccount> user_taobaos = CommonService.getChannelAccountByUser(to_user);
            if (user_taobaos.isEmpty()) {
                content.put("code", -1);
                content.put("desc", "我们没有招待过此淘宝用户，请重新输入");
                response.content = content.toString();
                break;
            }
            
            BeanChannelAccount user_taobao = user_taobaos.get(0);
            if (CommonService.CHANNEL_TAOBAO != user_taobao.i_channel) {
                content.put("code", -1);
                content.put("desc", "我们没有招待过此淘宝用户，请重新输入");
                response.content = content.toString();
                break;
            }
            if (!user_taobao.c_phone.equals(to_phone)) {
                content.put("code", -1);
                content.put("desc", "填写的手机号跟淘宝上使用的手机不匹配，请重新输入");
                response.content = content.toString();
                break;
            }
            
            {
                JSONObject args_cdb = new JSONObject();
                args_cdb.put("paid_to",     CommonService.getPlatformAccountByCaid(user_taobao.i_caid));
                args_cdb.put("paid_from",   CommonService.getPlatformAccountByCaid(request.user));
                FjDscpMessage req_cdb = new FjDscpMessage();
                req_cdb.json().put("fs", "wcweb");
                req_cdb.json().put("ts", "cdb");
                req_cdb.json().put("inst", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE);
                req_cdb.json().put("args", args_cdb);
                FjServerToolkit.getAnySender().send(req_cdb);
                content.put("code", 0);
                response.content = content.toString();
            }
            break;
        case STEP_SUCCESS:
            response.type       = FjHttpRequest.CT_HTML;
            response.content    = WechatForm.createFormMessage(WechatForm.MESSAGE_SUCCESS, "关联成功", "您的微信和淘宝已成功关联，现在可以到“我的账户”中查看相关信息，感谢您的支持", null, null);
            break;
        }
    }
    
    private static void processQueryPlatformAccount(WcwResponse response, WcwRequest request) {
        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(request.user));
        StringBuilder sb = new StringBuilder();
        sb.append(WechatForm.createFormHead(WechatForm.FORM_CELL, "账户明细"));
        
        {
            List<String[]> cells = new LinkedList<String[]>();
            cells.add(new String[] {"现金", puser.i_cash + "元"});
            cells.add(new String[] {"优惠券", puser.i_coupon + "元"});
            float[] prestatement = CommonService.prestatementByCaid(request.user);
            cells.add(new String[] {"现金(实时)", prestatement[0] + "元"});
            cells.add(new String[] {"优惠券(实时)", prestatement[1] + "元"});
            sb.append(WechatForm.createFormCellGroup("我的账户", cells, null));
        }
        
        CommonService.getChannelAccountRelatedAll(request.user)
                .stream()
                .filter(u->u.i_caid != request.user)
                .forEach(u->{
                    List<String[]> cells = new LinkedList<String[]>();
                    cells.add(new String[] {"账号", u.c_user});
                    cells.add(new String[] {"姓名", u.c_name});
                    sb.append(WechatForm.createFormCellGroup("关联账号：" + getChannelDesc(u.i_channel), cells, null));
                });
        
        CommonService.getChannelAccountRelatedAll(request.user)
                .forEach(u->{
                    CommonService.getOrderByCaid(u.i_caid)
                            .stream()
                            .filter(o->!o.isClose())
                            .forEach(o->{
                                o.commodities.values()
                                        .stream()
                                        .filter(c->!c.isClose())
                                        .forEach(c->{
                                            BeanGameAccount account = CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16));
                                            String games = CommonService.getGameByGaid(account.i_gaid).stream().map(g->g.getDiaplayName()).collect(Collectors.joining("; "));
                                            
                                            List<String[]> cells = new LinkedList<String[]>();
                                            cells.add(new String[] {"在租游戏", games});
                                            cells.add(new String[] {"游戏账号", account.c_user});
                                            cells.add(new String[] {"当前密码", account.c_pass_curr});
                                            cells.add(new String[] {"租赁类型", "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"});
                                            cells.add(new String[] {"租赁单价", c.i_price + "元/天"});
                                            cells.add(new String[] {"起租时间", c.t_begin});
                                            sb.append(WechatForm.createFormCellGroup("在租游戏：" + games, cells, null));
                                            sb.append(WechatForm.createFormButton(WechatForm.BUTTON_WARN, "退租", "javascript:;"));
                                        });
                            });
                });
        sb.append(WechatForm.createFormFoot());
        
        response.type       = FjHttpRequest.CT_HTML;
        response.content    = sb.toString();
    }
    
    private static void processQueryGame(WcwResponse response, WcwRequest request) {
        switch (request.step) {
        case STEP_SETUP: {
            fetchFile(response, "/query_game.html");
            response.content = String.format(response.content,
                    CommonDefinition.ISIS.INST_ECOM_QUERY_GAME,
                    request.user);
            break;
        }
        case STEP_APPLY: {
            if (request.args.has("word")) {
                String word = request.args.getString("word");
                JSONObject content = new JSONObject();
                content.put("code", 0);
                JSONArray  args_desc = new JSONArray();
                CommonService.getGameAll().values()
                        .stream()
                        .filter(game->game.c_name_zh.contains(word) || game.c_name_en.contains(word))
                        .forEach(game->{
                            JSONObject args_game = new JSONObject();
                            args_game.put("gid",  Integer.toHexString(game.i_gid));
                            args_game.put("icon", game.c_url_icon);
                            args_game.put("name", game.getDiaplayName());
                            args_desc.add(args_game);
                        });
                content.put("desc", args_desc);
                response.type       = FjHttpRequest.CT_JSON;
                response.content    = content.toString();
            } else if (request.args.has("gid")) {
                int      gid  = Integer.parseInt(request.args.getString("gid"), 16);
                BeanGame game = CommonService.getGameByGid(gid);
                fetchFile(response, "/query_game_by_gid.html");
                response.content = String.format(response.content,
                        CommonDefinition.ISIS.INST_ECOM_QUERY_GAME,
                        request.user,
                        gid,
                        game.c_url_poster,
                        game.c_name_zh,
                        game.c_name_en,
                        game.c_country,
                        game.t_sale,
                        game.c_platform,
                        CommonService.getGameRentPriceByGid(gid, CommonService.RENT_TYPE_A).i_price,
                        CommonService.getGameRentPriceByGid(gid, CommonService.RENT_TYPE_B).i_price);
            }
            break;
        }
        }
    }
    
    private static void processUpdateChannelAccount(WcwResponse response, WcwRequest request) {
        JSONObject content = new JSONObject();
        
        response.type = FjHttpRequest.CT_JSON;
        if (!request.args.has("_channel") || !request.args.has("_user") || !request.args.has("_name")) {
            content.put("code", CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS);
            content.put("desc", "参数不完整");
            response.content = content.toString();
            return;
        }
        
        FjDscpMessage rsp = null;
        {   // 创建用户
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("channel", request.args.getInt("_channel"));
            args_cdb.put("user",    request.args.getString("_user"));
            args_cdb.put("name",    request.args.getString("_name"));
            rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args_cdb);
            CommonService.updateChannelAccount();
            CommonService.updatePlatformAccount();
            CommonService.updatePlatformAccountMap();
            
            content.put("code", CommonService.getResponseCode(rsp));
            content.put("desc", CommonService.getResponseDesc(rsp));
        }
        {   // 合并用户
            boolean merge = request.args.has("_merge") ? request.args.getBoolean("_merge") : false;
            if (merge) {
                int puser_alipay = CommonService.getPlatformAccountByCaid(Integer.parseInt(content.getString("desc"), 16));
                JSONObject args_cdb = new JSONObject();
                args_cdb.put("paid_to", CommonService.getPlatformAccountByCaid(request.user));
                args_cdb.put("paid_from", puser_alipay);
                rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, args_cdb);
                CommonService.updatePlatformAccount();
                CommonService.updatePlatformAccountMap();
                
                content.put("code", CommonService.getResponseCode(rsp));
                content.put("desc", CommonService.getResponseDesc(rsp));
            }
        }
        response.content = content.toString();
    }
    
    private static void processQueryOrder(WcwResponse response, WcwRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(WechatForm.createFormHead(WechatForm.FORM_CELL, "消费信息"));
        CommonService.getChannelAccountRelatedAll(request.user)
                .forEach(u->{
                    CommonService.getOrderByCaid(u.i_caid)
                            .stream()
                            .forEach(o->{
                                o.commodities.values()
                                        .stream()
                                        .filter(c->c.isClose())
                                        .forEach(c->{
                                            BeanGameAccount account = CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16));
                                            String games = CommonService.getGameByGaid(account.i_gaid).stream().map(g->g.getDiaplayName()).collect(Collectors.joining("; "));
                                            
                                            List<String[]> cells = new LinkedList<String[]>();
                                            cells.add(new String[] {"账号", account.c_user});
                                            cells.add(new String[] {"类型", "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"});
                                            cells.add(new String[] {"单价", c.i_price + "元/天"});
                                            cells.add(new String[] {"起租时间", c.t_begin});
                                            cells.add(new String[] {"退租时间", c.t_end});
                                            cells.add(new String[] {"总价", c.i_expense + "元/天"});
                                            sb.append(WechatForm.createFormCellGroup("租赁游戏：" + games, cells, null));
                                        });
                            });
                });
        sb.append(WechatForm.createFormFoot());
        response.type       = FjHttpRequest.CT_HTML;
        response.content    = sb.toString();
    }
    
    private static void processApplyPlatformAccountMoney_Recharge(WcwResponse response, WcwRequest request) {
        switch (request.step) {
        case STEP_SETUP: {
            fetchFile(response, "/apply_platform_account_money_recharge.html");
            long timestamp  = System.currentTimeMillis() / 1000;
            String nonceStr = Long.toHexString(System.currentTimeMillis());
            response.content = String.format(response.content,
                    CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY,
                    request.user,
                    FjServerToolkit.getServerConfig("wca.appid"),
                    timestamp,
                    nonceStr,
                    WechatInterface.createSignature4Config(nonceStr, timestamp, WcWeb.generateUrl(request.server, CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, request.user)));
            break;
        }
        case STEP_APPLY: {
            String money = request.args.getString("money");
            String terminal = "127.0.0.1";
            try {terminal = ((InetSocketAddress) request.conn.getRemoteAddress()).getAddress().getHostAddress();}
            catch (IOException e) {logger.error("get user terminal address failed", e);}
            String url = generateUrl(request.server, URL_KEY + "/pay/recharge/success", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, request.user);
            url = url.substring(0, url.indexOf("?"));
            FjXmlMessage rsp = WechatInterface.prepay(
                    "VC电玩-充值",
                    "您已成功充值" + money + "元",
                    (int) (Float.parseFloat(money) * 100),
                    terminal,
                    url,
                    CommonService.getChannelAccountByCaid(request.user).c_user);
            
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
            response.type       = FjHttpRequest.CT_JSON;
            response.content    = json_prepay.toString();
            break;
        }
        case STEP_SUCCESS: {
            response.type       = FjHttpRequest.CT_HTML;
            response.content    = WechatForm.createFormMessage(WechatForm.MESSAGE_SUCCESS, "充值成功", "现在可以到“我的账户”中查看账户余额，感谢您的支持", null, null);
            break;
        }
        }
    }
    
    private static void processApplyPlatformAccountMoney_Refund(WcwResponse response, WcwRequest request) {
        BeanPlatformAccount puser       = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(request.user));
        List<BeanChannelAccount> users  = CommonService.getChannelAccountRelatedByCaidNChannel(request.user, CommonService.CHANNEL_ALIPAY);
        if (users.isEmpty()) request.step = STEP_PREPARE;
        
        switch (request.step) {
        case STEP_PREPARE: {
            fetchFile(response, "/update_channel_account.html");
            response.content = String.format(response.content,
                    CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT,
                    request.user,
                    CommonService.CHANNEL_ALIPAY,
                    true,   // merge
                    generateUrl(request.server, URL_KEY + "/pay/refund", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, request.user));
            break;
        }
        case STEP_SETUP: {
            String error = null;
            if (null != (error = checkRefund(response, puser))) {
                response.type       = FjHttpRequest.CT_HTML;
                response.content    = WechatForm.createFormMessage(WechatForm.MESSAGE_WARN, "退款失败", error, null, null);
                break;
            }
            
            BeanChannelAccount user_alipay = users.get(0);
            fetchFile(response, "/apply_platform_account_money_refund.html");
            response.content = String.format(response.content,
                    CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY,
                    request.user,
                    user_alipay.c_user,
                    user_alipay.c_name,
                    puser.i_cash);
            break;
        }
        case STEP_APPLY: {
            String error = null;
            if (null != (error = checkRefund(response, puser))) {
                response.type       = FjHttpRequest.CT_JSON;
                response.content    = String.format("{\"code\":-1,\"desc\":\"%s\"}", error);
                break;
            }
            
            String _user    = request.args.getString("_user");
            String _name    = request.args.getString("_name");
            float  _money   = Float.valueOf(request.args.getString("_money"));
            {   // 扣款
                JSONObject args = new JSONObject();
                args.put("paid",    puser.i_paid);
                args.put("remark",  "【自动退款】");
                args.put("type",    CommonService.MONEY_CASH);
                args.put("money",   -_money);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   request.server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                CommonService.updatePlatformAccountMoney();
            }
            {   // 提工单
                JSONObject args = new JSONObject();
                args.put("caid",    request.user);
                args.put("type",    CommonService.TICKET_TYPE_REFUND);
                args.put("title",   "【自动】【退款】");
                args.put("content", String.format("支付宝账号: %s | 真实姓名: %s | 退款金额: %.2f 元 | 退款备注: %s",
                        _user,
                        _name,
                        _money,
                        "VC电玩游戏退款"));
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   request.server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                logger.error("user pay refund: " + args);
            }
            {   // 反馈
                response.type       = FjHttpRequest.CT_JSON;
                response.content    = String.format("{\"code\":0,\"desc\":\"%s\"}", generateUrl(request.server, URL_KEY + "/pay/refund", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, request.user) + "&step=success");
            }
            break;
        }
        case STEP_SUCCESS:
            response.type       = FjHttpRequest.CT_HTML;
            response.content    = WechatForm.createFormMessage(WechatForm.MESSAGE_SUCCESS, "退款成功", null, null, null);
        }
    }
    
    private static String checkRefund(WcwResponse response, BeanPlatformAccount puser) {
        if (0 == CommonService.getChannelAccountByPaid(puser.i_paid)
                .stream()
                .filter(user->user.i_channel == CommonService.CHANNEL_ALIPAY)
                .count()) { // 检查账户
            return "尚未关联支付宝账户，无法退款";
        }
        // 检查余额
        if (0.00f == puser.i_cash) {
            return "您的账户里没有可退现金";
        }
        // 检查订单
        if (0 < CommonService.getChannelAccountByPaid(puser.i_paid)
                .stream()
                .map(user->
                    CommonService.getOrderByCaid(user.i_caid)
                        .stream()
                        .map(o->o.commodities.values()
                                .stream()
                                .filter(c->!c.isClose())
                                .count())
                        .collect(Collectors.summingLong(l->l))
                        .intValue())
                .collect(Collectors.summingLong(l->l))
                .intValue()) {
            return "您的账户里仍有未关闭的订单";
        }
        return null;
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
    private static void processPayRechargeSuccess(WcwResponse response, String server, Document xml) {
        JSONObject args = new JSONObject();
        NodeList nodes = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (null == node.getFirstChild()) continue;
            args.put(node.getNodeName(), node.getFirstChild().getNodeValue());
        }

        logger.error("user pay recharge: " + args);
        
        if (!cache_trade.contains(args.getString("out_trade_no"))) {
            cache_trade.add(args.getString("out_trade_no"));
            BeanChannelAccount user = CommonService.getChannelAccountByUser(args.getString("openid")).get(0);
            float money = ((float) args.getInt("total_fee")) / 100;
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("caid",    user.i_caid);
            args_cdb.put("money",   money);
            FjDscpMessage msg_cdb = new FjDscpMessage();
            msg_cdb.json().put("fs",   server);
            msg_cdb.json().put("ts",   "bcs");
            msg_cdb.json().put("inst", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY);
            msg_cdb.json().put("args", args_cdb);
            FjServerToolkit.getAnySender().send(msg_cdb);
        }
        
        response.type       = FjHttpRequest.CT_XML;
        response.content    = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }
    
    private static void fetchFile(WcwResponse response, String url) {
        File file = new File(ROOT + (url.startsWith(URL_KEY) ? url.substring(URL_KEY.length()) : url));
        if (!file.isFile()) {
            logger.warn("not such file to fetch: " + file.getPath());
            response.type   = FjHttpRequest.CT_TEXT;
            return;
        }
        
        FileInputStream         fis = null;
        ByteArrayOutputStream   baos = null;
        try {
            byte[]  buf = new byte[1024 * 4];
            int     len = -1;
            fis     = new FileInputStream(file);
            baos    = new ByteArrayOutputStream();
            while (0 < (len = fis.read(buf))) baos.write(buf, 0, len);
            response.type       = getFileMime(file.getName());
            response.content    = baos.toString("utf-8");
        } catch (IOException e) {logger.error("fetch file failed, url: " + url, e);}
        finally {
            try {
                fis.close();
                baos.close();
            } catch (IOException e) {e.printStackTrace();}
        }
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
    
    private static String getChannelDesc(int channel) {
        switch (channel) {
        case CommonService.CHANNEL_TAOBAO: return "淘宝";
        case CommonService.CHANNEL_WECHAT: return "微信";
        case CommonService.CHANNEL_ALIPAY: return "支付宝";
        default: return "未  知";
        }
    }
    
    private static class WcwRequest {
        public String           server;
        public int              inst;
        public int              user;
        public String           step;
        public JSONObject       args;
        public SocketChannel    conn;
    }
    
    private static class WcwResponse {
        public String type;
        public String content;
    }
}
