package com.ski.vcg.bcs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ski.vcg.bcs.monitor.CacheMonitor;
import com.ski.vcg.bcs.monitor.CheckMonitor;
import com.ski.vcg.bcs.monitor.DataMonitor;
import com.ski.vcg.common.CommonDefinition;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanChannelAccount;
import com.ski.vcg.common.bean.BeanCommodity;
import com.ski.vcg.common.bean.BeanGameAccount;
import com.ski.vcg.common.bean.BeanOrder;
import com.ski.vcg.common.bean.BeanPlatformAccount;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class BcsTask implements FjServerTask {

    private static final Logger logger = Logger.getLogger(BcsTask.class);

    private DataMonitor     mon_data     = new DataMonitor();
    private CheckMonitor     mon_check     = new CheckMonitor();
    private CacheMonitor     mon_cache     = new CacheMonitor();

    @Override
    public void initialize(FjServer server) {
        mon_data.start();
        mon_check.start();
        mon_cache.start();
    }

    @Override
    public void destroy(FjServer server) {
        mon_data.close();
        mon_check.close();
        mon_cache.close();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }

        FjDscpMessage dmsg = (FjDscpMessage) msg;
        switch (dmsg.inst()) {
        case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY:
            logger.error("request platform account money: " + msg);
            processApplyPlatformAccountMoney(server.name(), dmsg);
            break;
        case CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_BEGIN:
            logger.error("request rent begin: " + msg);
            processApplyRentBegin(server.name(), dmsg);
            break;
        case CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_END:
            logger.error("request rent end: " + msg);
            processApplyRentEnd(server.name(), dmsg);
            break;
        default:
            break;
        }
    }

    private static void processApplyRentBegin(String server, FjDscpMessage request) {
        JSONObject args = request.argsToJsonObject();
        if (!args.has("platform") || !args.has("caid") || !args.has("gid") || !args.has("type")) {
            logger.error(String.format("illegal arguments: " + args));
            response(request, server, CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS, "非法参数");
            return;
        }

        int platform= args.getInt("platform");
        int caid    = args.getInt("caid");
        int gid     = args.getInt("gid");
        int type    = args.getInt("type"); // class a or class b
        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(caid));

        // check deposit
        CommonService.updateGameAccountRent();
        {
            float deposite    = Float.parseFloat(FjServerToolkit.getServerConfig("bcs.deposite"))
                    * (CommonService.getGameAccountByPaid(puser.i_paid, CommonService.RENT_TYPE_A).size()
                            + CommonService.getGameAccountByPaid(puser.i_paid, CommonService.RENT_TYPE_B).size()
                            + 1);
            if (puser.i_cash < deposite) {
                logger.error(String.format("not enough cash for rent begin: args %s need %.2f but have %.2f", args, deposite, puser.i_cash));
                response(request, server, CommonDefinition.CODE.CODE_USER_NOT_ENOUGH_DEPOSIT, "余额不足无法起租");
                return;
            }
        }

        // choose account
        int gaid = -1;
        {
            List<Integer> gaid_all = new LinkedList<>();  // 全空闲
            List<Integer> gaid_sin = new LinkedList<>();  // 单类别空闲
            for (BeanGameAccount account : CommonService.getGameAccountByGid(gid)) {
                int rent_a = CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_A);
                int rent_b = CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_B);
                if (CommonService.RENT_STATE_IDLE == rent_a && CommonService.RENT_STATE_IDLE == rent_b)
                    gaid_all.add(account.i_gaid);
                if ((CommonService.RENT_STATE_IDLE == rent_a && type == CommonService.RENT_TYPE_A)
                        || (CommonService.RENT_STATE_IDLE == rent_b && type == CommonService.RENT_TYPE_B))
                    gaid_sin.add(account.i_gaid);
            }
            if (!gaid_all.isEmpty()) gaid = gaid_all.get(0);
            else if (!gaid_sin.isEmpty()) {
                for (int id : gaid_sin) {
                    BeanGameAccount account = CommonService.getGameAccountByGaid(id);
                    JSONObject args_wa = new JSONObject();
                    args_wa.put("user", account.c_user);
                    args_wa.put("pass", account.c_pass);
                    FjDscpMessage rsp = CommonService.send("wa", CommonDefinition.ISIS.INST_ECOM_APPLY_GAME_ACCOUNT_VERIFY, args_wa);
                    if (!CommonService.isResponseSuccess(rsp)) {
                        logger.error("user or pass incorrect for rent begin:" + account.c_user + ", try next");
                        continue;
                    }
                    if (type == CommonService.RENT_TYPE_A && rsp.toString().contains(" binded")) {
                        logger.error("account binded before a rent begin:" + account.c_user + ", try next");
                        continue;
                    }
                    if (type == CommonService.RENT_TYPE_B && rsp.toString().contains(" unbinded")) {
                        logger.error("account unbinded before b rent begin:" + account.c_user + ", try next");
                        continue;
                    }
                    gaid = id;
                    break;
                }
            }

            if (-1 == gaid) {
                logger.error(String.format("game account not enough for rent begin: args", args));
                response(request, server, CommonDefinition.CODE.CODE_USER_NOT_ENOUGH_ACCOUNT, "该游戏已没有剩余帐号可租");
                return;
            }
        }

        // submit order
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("caid",        caid);
            args_cdb.put("platform",    platform);
            args_cdb.put("open",        sdf.format(new Date()));
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_ORDER, args_cdb);
            if (!CommonService.isResponseSuccess(rsp)){
                logger.error(String.format("submit order failed for rent begin: args = %s, rsp = %s", args, rsp));
                response(request, server, CommonDefinition.CODE.CODE_USER_OPEN_ORDER_FAILED, "提交订单失败，请稍后重试");
                return;
            }
            int oid = Integer.parseInt(CommonService.getResponseDesc(rsp), 16);

            args_cdb.clear();
            args_cdb.put("oid", oid);
//            args_cdb.put("remark", "");
            args_cdb.put("price", CommonService.getGameRentPriceByGid(gid, type).i_price);
            args_cdb.put("count", 1);
            args_cdb.put("begin", sdf.format(new Date()));
            args_cdb.put("arg0", Integer.toHexString(gaid));
            args_cdb.put("arg1", CommonService.RENT_TYPE_A == type ? "A" : CommonService.RENT_TYPE_B == type ? "B" : "U");
            rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_COMMODITY, args_cdb);
            if (!CommonService.isResponseSuccess(rsp)) {
                logger.error(String.format("submit commodity failed for rent begin: args = %s, rsp = %s", args, rsp));
                response(request, server, CommonDefinition.CODE.CODE_USER_OPEN_COMMODITY_FAILED, "提交订单失败，请稍后重试");
                return;
            }
        }
        response(request, server, CommonDefinition.CODE.CODE_SYS_SUCCESS, null);
    }

    private void processApplyRentEnd(String server, FjDscpMessage request) {
        JSONObject args = request.argsToJsonObject();
        if (!args.has("caid") || !args.has("oid") || !args.has("csn")) {
            logger.error("illegal arguments: " + args);
            response(request, server, CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS, "非法参数");
            return;
        }

        int caid = args.getInt("caid");
        int oid  = args.getInt("oid");
        int csn  = args.getInt("csn");
        BeanChannelAccount user = CommonService.getChannelAccountByCaid(caid);

        // check order
        CommonService.updateOrder();
        BeanCommodity c = null;
        {
            boolean isOrderMatch = false;
            for (BeanOrder o : CommonService.getOrderByPaid(CommonService.getPlatformAccountByCaid(caid))) {
                if (o.i_oid == oid && o.commodities.containsKey(csn)) {
                    isOrderMatch = true;
                    c = o.commodities.get(csn);
                    break;
                }
            }
            if (!isOrderMatch) {
                logger.error("order not found for rent end: args = " + args);
                response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_ORDER, "没有找到此订单");
                return;
            }
        }
        if (CommonService.getOrderByOid(oid).commodities.get(csn).isClose()) {
            response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_ORDER, "订单已关闭");
            return;
        }
        // check cache
        {
            if (mon_cache.isCacheRentEndFailForPass(user)) {
                logger.error("user or pass incorrect for rent end, try later after 10 minutes: args = " + args);
                response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_GAME_ACCOUNT_STATE, "用户名或密码错误，请10分钟之后再试");
                return;
            }
        }
        // check account
        CommonService.updateGameAccount();
        BeanGameAccount account = CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16));
        {
            JSONObject args_wa = new JSONObject();
            args_wa.put("user", account.c_user);
            args_wa.put("pass", account.c_pass);
            FjDscpMessage rsp = CommonService.send("wa", CommonDefinition.ISIS.INST_ECOM_APPLY_GAME_ACCOUNT_VERIFY, args_wa);
            if (!CommonService.isResponseSuccess(rsp)) {
                logger.error("user or pass incorrect for rent end, try later after 10 minutes: args = " + args);
                response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_GAME_ACCOUNT, "用户名或密码错误");
                if (CommonDefinition.CODE.CODE_WEB_PSN_USER_OR_PASS_INCORRECT == CommonService.getResponseCode(rsp)) {
                    if (!mon_cache.isCacheRentEndFailForPass(user))
                        mon_cache.putCacheRentEndFailForPass(user);
                }
                return;
            }

            switch (c.c_arg1) {
            case "A":
                if (rsp.toString().contains(" binded")) {
                    logger.error("unbind account before A rent end: args = " + args);
                    response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_GAME_ACCOUNT_STATE, "退租前请先解绑帐号");
                    return;
                }
                break;
            case "B":
                if (CommonService.RENT_STATE_IDLE == CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_A)
                        && rsp.toString().contains(" binded")) {
                    logger.error("unbind account before only B rent end: args = " + args);
                    response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_GAME_ACCOUNT_STATE, "退租前请先解绑帐号");
                    return;
                }
                break;
            }
        }
        // change password
        {
            String pass_new = CommonService.createGameAccountPassword();
            BeanChannelAccount user_a = null;
            BeanChannelAccount user_b = null;
            switch (c.c_arg1) {
            case "A":
                if (CommonService.RENT_STATE_IDLE != CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_B)) {
                    user_b = CommonService.getChannelAccountByCaid(CommonService.getChannelAccountByGaid(account.i_gaid, CommonService.RENT_TYPE_B));
                }
                break;
            case "B":
                if (CommonService.RENT_STATE_IDLE != CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_A)) {
                    user_a = CommonService.getChannelAccountByCaid(CommonService.getChannelAccountByGaid(account.i_gaid, CommonService.RENT_TYPE_A));
                }
                break;
            }

            { // modify to psn
                JSONObject args_cdb = new JSONObject();
                args_cdb.put("user",     account.c_user);
                args_cdb.put("pass",     account.c_pass);
                args_cdb.put("pass_new", pass_new);
                FjDscpMessage rsp = CommonService.send("wa", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args_cdb);
                if (!CommonService.isResponseSuccess(rsp)) {
                    logger.error(String.format("change password to psn failed: args = %s, rsp = %s", args, rsp));
                    response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_GAME_ACCOUNT_STATE, "操作失败，请稍后重试");
                    return;
                }
            }

            { // notify to db
                JSONObject args_cdb = new JSONObject();
                args_cdb.put("gaid", account.i_gaid);
                args_cdb.put("pass", pass_new);
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args_cdb);
                if (!CommonService.isResponseSuccess(rsp)) {
                    logger.error(String.format("change password to cdb failed: args = %s, rsp = %s", args, rsp));
                    response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_GAME_ACCOUNT_STATE, "操作失败，请稍后重试");
                    return;
                }
            }

            { // notify
                if (null != user_b) {
                    Notifier.notifyModifyNormally(server,
                            CommonService.getPlatformAccountByCaid(user_b.i_caid),
                            account.c_user + "(不认证)",
                            "密码已更新为：" + pass_new);
                }
                if (null != user_a) {
                    Notifier.notifyModifyNormally(server,
                            CommonService.getPlatformAccountByCaid(user_a.i_caid),
                            account.c_user + "(认证)",
                            "密码已更新为：" + pass_new);
                }
            }
        }
        // submit order
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("oid", oid);
            args_cdb.put("csn", csn);
            args_cdb.put("end", sdf.format(new Date()));
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_COMMODITY, args_cdb);
            if (!CommonService.isResponseSuccess(rsp)) {
                logger.error(String.format("submit commodity failed: args = %s, rsp = %s", args, rsp));
                response(request, server, CommonDefinition.CODE.CODE_USER_CLOSE_COMMODITY_FAILED, "提交订单失败，请稍后重试");
                return;
            }
        }
        response(request, server, CommonDefinition.CODE.CODE_SYS_SUCCESS, null);
    }

    private static void processApplyPlatformAccountMoney(String server, FjDscpMessage request) {
        JSONObject args = request.argsToJsonObject();
        if (!args.has("caid") || !args.has("money")) {
            logger.error("illegal arguments: " + args);
            response(request, server, CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS, "非法参数");
            return;
        }

        float money = Float.parseFloat(args.getString("money"));

        if (money > 0)      processApplyPlatformAccountMoney_Recharge(server, request);
        else if (money < 0) processApplyPlatformAccountMoney_Refund(server, request);
        else {
            logger.error("illegal money: args = " + args);
            response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_MONEY, "无效金额");
        }
    }

    private static void processApplyPlatformAccountMoney_Recharge(String server, FjDscpMessage request) {
        JSONObject args = request.argsToJsonObject();
        int     caid    = args.getInt("caid");
        float   money   = Float.parseFloat(args.getString("money"));
        int     paid    = CommonService.getPlatformAccountByCaid(caid);
        JSONObject args_cdb = new JSONObject();
        args_cdb.put("paid",    paid);
        args_cdb.put("remark",  "【自动充值】");
        args_cdb.put("type",    CommonService.MONEY_CASH);
        args_cdb.put("money",   money);
        FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, args_cdb);
        if (!CommonService.isResponseSuccess(rsp)) {
            logger.error(String.format("recharge failed: args = %s, rsp = %s", args, rsp));
            response(request, server, CommonDefinition.CODE.CODE_USER_MONEY_RECHARGE_FAILED, "充值失败，请稍后重试");
            return;
        }
        response(request, server, CommonDefinition.CODE.CODE_SYS_SUCCESS, null);
        Notifier.notifyCashRecharge(server, paid, String.format("%.2f元", money));
    }

    private static void processApplyPlatformAccountMoney_Refund(String server, FjDscpMessage request) {
        JSONObject args = request.argsToJsonObject();
        int     caid    = args.getInt("caid");
        float   money   = Float.parseFloat(args.getString("money"));
        int     paid    = CommonService.getPlatformAccountByCaid(caid);

        // 校验订单
        CommonService.updateOrder();
        {
            boolean isAllClose = true;
            for (BeanOrder order : CommonService.getOrderByPaid(paid)) {
                if (!order.isClose()) {
                    isAllClose = false;
                    break;
                }
            }
            if (!isAllClose) {
                logger.error("close all order before rent end: args = " + args);
                response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_ORDER_STATE, "退款前请先关闭所有订单");
                return;
            }
        }

        // 校验余额
        CommonService.updatePlatformAccount();
        {
            BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(paid);
            if (0 >= puser.i_cash) {
                logger.error("no cash to refund: args = " + args);
                response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_MONEY, "余额已为0元，无法退款");
                return;
            }
            if (-money > puser.i_cash) {
                logger.error("refund must be smaller than cash: args = " + args);
                response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_MONEY, "退款金额不能大于余额");
                return;
            }
        }
//        // 校验用户
//        {
//            if (0 == CommonService.getChannelAccountByPaidNChannel(paid, CommonService.CHANNEL_ALIPAY).size()) {
//                response(request, server, CommonDefinition.CODE.CODE_USER_ILLEGAL_CHANNEL_ACCOUNT_STATE, "bind a alipay user first");
//                return;
//            }
//        }
//        // 提交退款工单
//        {
//            BeanChannelAccount user_alipay = CommonService.getChannelAccountByPaidNChannel(paid, CommonService.CHANNEL_ALIPAY).get(0);
//            JSONObject args_cdb = new JSONObject();
//            args_cdb.put("caid",    user_alipay.i_caid);
//            args_cdb.put("type",    CommonService.TICKET_TYPE_REFUND);
//            args_cdb.put("title",   "【自动】【退款】");
//            args_cdb.put("content", String.format("支付宝账号: %s|真实姓名: %s|退款金额: %.2f 元|退款备注: %s",
//                    user_alipay.c_user,
//                    user_alipay.c_name,
//                    -money,
//                    "VC电玩游戏退款"));
//            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET, args_cdb);
//            if (!CommonService.isResponseSuccess(rsp)) {
//                response(request, server, CommonDefinition.CODE.CODE_USER_MONEY_REFUND_FAILED, CommonService.getResponseDesc(rsp));
//                return;
//            }
//        }
        { // 修改账户金额
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("paid",    paid);
            args_cdb.put("remark",  "【自动退款】");
            args_cdb.put("type",    CommonService.MONEY_CASH);
            args_cdb.put("money",   money);
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, args_cdb);
            if (!CommonService.isResponseSuccess(rsp)) {
                logger.error(String.format("pay refund failed, args = %s, rsp = %s", args, rsp));
                response(request, server, CommonDefinition.CODE.CODE_USER_MONEY_REFUND_FAILED, "退款失败，请稍后重试");
                return;
            }
        }
        response(request, server, CommonDefinition.CODE.CODE_SYS_SUCCESS, null);
        Notifier.notifyCashRefund(server, paid, String.format("%.2f元", -money));
    }

    private static void response(FjDscpMessage request, String server, int code, String desc) {
        JSONObject args = new JSONObject();
        args.put("code", code);
        args.put("desc", desc);
        if (CommonDefinition.CODE.CODE_SYS_SUCCESS == code) logger.debug("response success: " + args);
        else logger.error("response fail: " + args);

        FjDscpMessage response = new FjDscpMessage();
        response.json().put("fs",   server);
        response.json().put("ts",   request.fs());
        response.json().put("sid",  request.sid());
        response.json().put("inst", request.inst());
        response.json().put("args", args);
        FjServerToolkit.getAnySender().send(response);
    }

//    private static void processMoneyTransfer(String server, JSONObject args) {
//        String  user    = args.getString("user");
//        String  name    = args.getString("name");
//        String  money   = args.getString("money");
//        String  remark  = args.getString("remark");
//
//        Map<String, String> params = new HashMap<String, String>();
//        // 基本参数
//        params.put("service",           "batch_trans_notify");    // 接口名称
//        params.put("partner",           FjServerToolkit.getServerConfig("bcs.alipay.pid"));   // 合作身份者ID
//        params.put("_input_charset",    "utf-8");  // 参数编码字符集
//        params.put("sign_type",         "MD5"); // 签名方式
//        params.put("sign",              "");  // 签名
//        FjServerToolkit.FjAddress addr = FjServerToolkit.getSlb().getAddress(server);
//        params.put("notify_url",        String.format("http://%s:%d/", addr.host, addr.port));   // 服务器异步通知页面路径
//        // 业务参数
//        params.put("account_name",      FjServerToolkit.getServerConfig("bcs.alipay.displayname")); // 付款账号名
//        params.put("detail_data",       String.format("%d^%s^%s^%s^%s",
//                System.currentTimeMillis(),
//                user,
//                name,
//                money,
//                remark));   // 付款详细数据
//        params.put("batch_no",          String.valueOf(System.currentTimeMillis()));   // 批量付款批次号
//        params.put("batch_num",         "1");   // 付款总笔数
//        params.put("batch_fee",         money); // 付款总金额
//        params.put("email",             FjServerToolkit.getServerConfig("bcs.alipay.user"));    // 付款账号
//        params.put("pay_date",          new SimpleDateFormat("yyyyMMdd").format(new Date()));   // 支付日期
//        // 签名
//        params.put("sign", createSignature(params, FjServerToolkit.getServerConfig("bcs.alipay.key")));
//
//        String url = "https://mapi.alipay.com/gateway.do?" + params.entrySet()
//                .stream()
//                .map(entry->entry.getKey() + "=" + entry.getValue())
//                .collect(Collectors.joining("&"));
//        FjMessage rsp = FjSender.sendHttpRequest(new FjHttpRequest("GET", url));
//        logger.error("pay response: " + rsp);
//    }
//
//    private static String createSignature(Map<String, String> params, String key) {
//        List<String> keys = new LinkedList<String>(params.keySet());
//        Collections.sort(keys);
//        StringBuilder sb = new StringBuilder();
//        keys.forEach(k->{
//            if (k.equals("sing")) return;
//            if (k.equals("sign_type")) return;
//
//            if (0 < sb.length()) sb.append("&");
//            sb.append(k);
//            sb.append("=");
//            sb.append(params.get(k));
//        });
//        sb.append(key);
//        try {
//            MessageDigest digest = MessageDigest.getInstance("MD5");
//            digest.update(sb.toString().getBytes());
//            byte [] digestbyte = digest.digest();
//            StringBuilder result = new StringBuilder();
//            for (byte db : digestbyte) {
//                String dbh = Integer.toHexString(db & 0xFF);
//                if (dbh.length() < 2) result.append("0");
//                result.append(dbh);
//            }
//            return result.toString();
//        } catch (NoSuchAlgorithmException e) {logger.error("create signature by md5 failed", e);}
//        return "";
//    }

}
