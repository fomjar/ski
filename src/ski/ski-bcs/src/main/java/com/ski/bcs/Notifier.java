package com.ski.bcs;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class Notifier {
    
    private static final Logger logger = Logger.getLogger(Notifier.class);

    public static void notifyTime(String server, int paid, String accountInfo, String time) {
        CommonService.getChannelAccountByPaid(paid).forEach(user->{
            String notify_content = String.format("【账号租期定时提醒】|游戏账号: %s|租赁时长: %s", accountInfo, time);
            if (CommonService.isNotificationNotified(user.i_caid, notify_content)) {
                return;
            } else {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_NOTIFICATION);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
            }
            
            logger.info(String.format("notify time: paid=0x%08X, account=%s, time=%s", paid, accountInfo, time));
            switch (user.i_channel) {
            case CommonService.CHANNEL_WECHAT: {
                JSONObject data = new JSONObject();
                data.put("first",       String.format("尊敬的 %s ，您好！这是您的在租账号 %s 的定期提醒。", user.c_name, accountInfo));
                data.put("keyword1",    time); // 业务进度
                data.put("keyword2",    "正常"); // 业务状态
                data.put("remark",      "请关注您的账户余额，避免欠费。");
                JSONObject content = new JSONObject();
                content.put("template", FjServerToolkit.getServerConfig("bcs.notify.template.time"));
                content.put("data",     data);
                JSONObject args = new JSONObject();
                args.put("user",    user.c_user);
                args.put("type",    "notify");
                args.put("content", content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "wca");
                msg.json().put("inst", CommonDefinition.ISIS.INST_USER_RESPONSE);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            case CommonService.CHANNEL_TAOBAO: {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("type",    CommonService.TICKET_TYPE_NOTIFY);
                args.put("title",   "【自动】【提醒】账号租期定时提醒");
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            }
        });
    }
    
    public static void notifyCashNotEnough(String server, int paid, String money_limit) {
        CommonService.getChannelAccountByPaid(paid).forEach(user->{
            String notify_content = String.format("【用户账户余额提醒】|账户余额: %s", money_limit);
            if (CommonService.isNotificationNotified(user.i_caid, notify_content)) {
                return;
            } else {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_NOTIFICATION);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
            }
            
            logger.info(String.format("notify cash not enough: paid=0x%08X, money_limit=%s", paid, money_limit));
            switch (user.i_channel) {
            case CommonService.CHANNEL_WECHAT: {
                JSONObject data = new JSONObject();
                data.put("first",       String.format("尊敬的 %s ，您好！您的余额(除押金)已不足 %s。", user.c_name, money_limit));
                data.put("keyword1",    "余额不足" + money_limit); // 业务进度
                data.put("keyword2",    "正常"); // 业务状态
                data.put("remark",      "请关注您的账户余额，避免欠费。");
                JSONObject content = new JSONObject();
                content.put("template", FjServerToolkit.getServerConfig("bcs.notify.template.cash.notenough"));
                content.put("data",     data);
                JSONObject args = new JSONObject();
                args.put("user",    user.c_user);
                args.put("type",    "notify");
                args.put("content", content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "wca");
                msg.json().put("inst", CommonDefinition.ISIS.INST_USER_RESPONSE);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            case CommonService.CHANNEL_TAOBAO: {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("type",    CommonService.TICKET_TYPE_NOTIFY);
                args.put("title",   "【自动】【提醒】用户账户余额提醒");
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            }
        });
    }
    
    public static void notifyCashRecharge(String server, int paid, String moneyInfo) {
        CommonService.getChannelAccountByPaid(paid).forEach(user->{
            String notify_content = String.format("【用户账户充值提醒】|充值金额: %s", moneyInfo);
//            if (CommonService.isNotificationNotified(user.i_caid, notify_content)) {
//                return;
//            } else {
            {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_NOTIFICATION);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
            }
//            }
            
            logger.info(String.format("notify cash recharge: paid=0x%08X, money=%s", paid, moneyInfo));
            switch (user.i_channel) {
            case CommonService.CHANNEL_WECHAT: {
                JSONObject data = new JSONObject();
                data.put("first",       String.format("尊敬的 %s ，您好！您已成功充值 %s。", user.c_name, moneyInfo));
                data.put("accountType", "充值账户");
                data.put("account",     user.c_name);
                data.put("amount",      moneyInfo);
                data.put("result",      "充值成功");
                data.put("remark",      "您可以到账户信息中查看账户余额，祝您游戏愉快！");
                JSONObject content = new JSONObject();
                content.put("template", FjServerToolkit.getServerConfig("bcs.notify.template.cash.recharge"));
                content.put("data",     data);
                JSONObject args = new JSONObject();
                args.put("user",    user.c_user);
                args.put("type",    "notify");
                args.put("content", content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "wca");
                msg.json().put("inst", CommonDefinition.ISIS.INST_USER_RESPONSE);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            case CommonService.CHANNEL_TAOBAO: {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("type",    CommonService.TICKET_TYPE_NOTIFY);
                args.put("title",   "【自动】【提醒】用户账户充值提醒");
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            }
        });
    }

    public static void notifyCashRefund(String server, int paid, String moneyInfo) {
        CommonService.getChannelAccountByPaid(paid).forEach(user->{
            String notify_content = String.format("【用户账户退款提醒】|退款金额: %s", moneyInfo);
//            if (CommonService.isNotificationNotified(user.i_caid, notify_content)) {
//                return;
//            } else {
            {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_NOTIFICATION);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
            }
//            }
            
            logger.info(String.format("notify cash refund: paid=0x%08X, money=%s", paid, moneyInfo));
            switch (user.i_channel) {
            case CommonService.CHANNEL_WECHAT: {
                JSONObject data = new JSONObject();
                data.put("first",   String.format("尊敬的 %s ，您好！您已成功退款 %s。", user.c_name, moneyInfo));
                data.put("reason",  "自动退款");
                data.put("refund",  moneyInfo);
                data.put("remark",  "退款将在2小时内到账，欢迎您下次光临！");
                JSONObject content = new JSONObject();
                content.put("template", FjServerToolkit.getServerConfig("bcs.notify.template.cash.refund"));
                content.put("data",     data);
                JSONObject args = new JSONObject();
                args.put("user",    user.c_user);
                args.put("type",    "notify");
                args.put("content", content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "wca");
                msg.json().put("inst", CommonDefinition.ISIS.INST_USER_RESPONSE);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            case CommonService.CHANNEL_TAOBAO: {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("type",    CommonService.TICKET_TYPE_NOTIFY);
                args.put("title",   "【自动】【提醒】用户账户退款提醒");
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            }
        });
    }

    public static void notifyBindAbnormally(String server, int paid, String accountInfo, boolean nowIsBind) {
        CommonService.getChannelAccountByPaid(paid).forEach(user->{
            String notify_content = String.format("【账号绑定状态提醒】|游戏账号: %s|当前状态: %s", accountInfo, nowIsBind ? "已绑定" : "未绑定");
            if (CommonService.isNotificationNotified(user.i_caid, notify_content)) {
                return;
            } else {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_NOTIFICATION);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
            }
            
            logger.info(String.format("notify bind: paid=0x%08X, account=%s, nowIsBind=%b", paid, accountInfo, nowIsBind));
            switch (user.i_channel) {
            case CommonService.CHANNEL_WECHAT: {
                JSONObject data = new JSONObject();
                data.put("first",       String.format("尊敬的 %s ，您好！您的在租账号 %s 状态异常。", user.c_name, accountInfo));
                data.put("keyword1",    "账号状态检测"); // 业务进度
                data.put("keyword2",    nowIsBind ? "已被绑定" : "尚未绑定"); // 业务状态
                data.put("remark",      String.format("请及时按照租赁类型%s您的账号，以免造成不必要的麻烦，谢谢合作。", nowIsBind ? "解绑" : "绑定"));
                JSONObject content = new JSONObject();
                content.put("template", FjServerToolkit.getServerConfig("bcs.notify.template.bind.abnormally"));
                content.put("data",     data);
                JSONObject args = new JSONObject();
                args.put("user",    user.c_user);
                args.put("type",    "notify");
                args.put("content", content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "wca");
                msg.json().put("inst", CommonDefinition.ISIS.INST_USER_RESPONSE);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            case CommonService.CHANNEL_TAOBAO: {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("type",    CommonService.TICKET_TYPE_NOTIFY);
                args.put("title",   "【自动】【提醒】账号绑定状态提醒");
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            }
        });
    }
    
    public static void notifyModifyAbnormally(String server, int paid, String accountInfo, String modifyInfo) {
        CommonService.getChannelAccountByPaid(paid).forEach(user->{
            String notify_content = String.format("【账号异常修改提醒】|游戏账号: %s|修改内容: %s", accountInfo, modifyInfo);
//            if (CommonService.isNotificationNotified(user.i_caid, notify_content)) {
//                return;
//            } else {
            {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_NOTIFICATION);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
            }
//            }
            
            logger.info(String.format("notify modify: paid=0x%08X, account=%s, modify=%s", paid, accountInfo, modifyInfo));
            switch (user.i_channel) {
            case CommonService.CHANNEL_WECHAT: {
                JSONObject data = new JSONObject();
                data.put("first",       String.format("尊敬的 %s ，您好！您的在租账号 %s %s。", user.c_name, accountInfo, modifyInfo));
                data.put("keyword1",    "账号状态检测"); // 业务进度
                data.put("keyword2",    modifyInfo); // 业务状态
                data.put("remark",      "请及时将账号信息改回，以免造成不必要的麻烦，谢谢合作。");
                JSONObject content = new JSONObject();
                content.put("template", FjServerToolkit.getServerConfig("bcs.notify.template.modify.abnormally"));
                content.put("data",     data);
                JSONObject args = new JSONObject();
                args.put("user",    user.c_user);
                args.put("type",    "notify");
                args.put("content", content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "wca");
                msg.json().put("inst", CommonDefinition.ISIS.INST_USER_RESPONSE);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            case CommonService.CHANNEL_TAOBAO: {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("type",    CommonService.TICKET_TYPE_NOTIFY);
                args.put("title",   "【自动】【提醒】账号异常修改提醒");
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            }
        });
    }
    
    public static void notifyModifyNormally(String server, int paid, String accountInfo, String modifyInfo) {
        CommonService.getChannelAccountByPaid(paid).forEach(user->{
            String notify_content = String.format("【账号信息修改提醒】|游戏账号: %s|修改内容: %s", accountInfo, modifyInfo);
//            if (CommonService.isNotificationNotified(user.i_caid, notify_content)) {
//                return;
//            } else {
            {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_NOTIFICATION);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
            }
//            }
            
            logger.info(String.format("notify modify: paid=0x%08X, account=%s, modify=%s", paid, accountInfo, modifyInfo));
            switch (user.i_channel) {
            case CommonService.CHANNEL_WECHAT: {
                JSONObject data = new JSONObject();
                data.put("first",       String.format("尊敬的 %s ，您好！您的在租账号 %s %s。", user.c_name, accountInfo, modifyInfo));
                data.put("keyword1",    "账号状态检测"); // 业务进度
                data.put("keyword2",    modifyInfo); // 业务状态
                data.put("remark",      "请重新登陆账号以便继续游戏，对您带来的不变深表歉意。");
                JSONObject content = new JSONObject();
                content.put("template", FjServerToolkit.getServerConfig("bcs.notify.template.modify.normally"));
                content.put("data",     data);
                JSONObject args = new JSONObject();
                args.put("user",    user.c_user);
                args.put("type",    "notify");
                args.put("content", content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "wca");
                msg.json().put("inst", CommonDefinition.ISIS.INST_USER_RESPONSE);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            case CommonService.CHANNEL_TAOBAO: {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("type",    CommonService.TICKET_TYPE_NOTIFY);
                args.put("title",   "【自动】【提醒】账号信息修改提醒");
                args.put("content", notify_content);
                FjDscpMessage msg = new FjDscpMessage();
                msg.json().put("fs",   server);
                msg.json().put("ts",   "cdb");
                msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET);
                msg.json().put("args", args);
                FjServerToolkit.getAnySender().send(msg);
                break;
            }
            }
        });
    }
}
