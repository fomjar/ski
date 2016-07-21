package com.ski.bcs.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanCommodity;
import com.ski.common.bean.BeanOrder;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.util.FjLoopTask;
import net.sf.json.JSONObject;

public class BcsMonitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(BcsMonitor.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BcsMonitor() {}

    @Override
    public void perform() {
        resetInterval();
        
        CommonService.updateGame();
        CommonService.updateGameAccount();
        CommonService.updateGameAccountGame();
        CommonService.updateGameAccountRent();
        
        CommonService.updateChannelAccount();
        CommonService.updatePlatformAccount();
        CommonService.updatePlatformAccountMap();
        CommonService.updateOrder();
        CommonService.updateNotification();
        
        CommonService.getPlatformAccountAll().values().forEach(puser->{
            CommonService.getOrderByPaid(puser.i_paid).forEach(o->{
                o.commodities.values().forEach(c->{
                    if (c.isClose()) return;
                    
                    try {
                        Date date   = sdf.parse(c.t_begin);
                        long delta  = System.currentTimeMillis() - date.getTime();
                        if (delta >= 15 * 24 * 60 * 60 * 1000L) {
                            notifyTime("bcs",
                                    puser.i_paid,
                                    String.format("%s(%s)", CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16)).c_user, "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"),
                                    "15天");
                        } else if (delta >= 7 * 24 * 60 * 60 * 1000L) {
                            notifyTime("bcs",
                                    puser.i_paid,
                                    String.format("%s(%s)", CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16)).c_user, "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"),
                                    "7天");
                        } else {
                            // normal
                        }
                    } catch (Exception e) {logger.error("parse commodity time failed: " + c.t_begin, e);}
                });
            });
        });
        
        CommonService.getPlatformAccountAll().values().forEach(puser->{
            boolean isCommodityOpen = false;
            for (BeanOrder o : CommonService.getOrderByPaid(puser.i_paid)) {
                for (BeanCommodity c : o.commodities.values()) {
                    if (!c.isClose()) {
                        isCommodityOpen = true;
                        break;
                    }
                }
                if (isCommodityOpen) break;
            }
            if (isCommodityOpen) {
                float   deposite    = 100.00f * (CommonService.getGameAccountByPaid(puser.i_paid, CommonService.RENT_TYPE_A).size()
                        + CommonService.getGameAccountByPaid(puser.i_paid, CommonService.RENT_TYPE_B).size());
                float[] statement   = CommonService.prestatementByPaid(puser.i_paid);
                if (deposite >= statement[0]) {
                    notifyMoney("bcs", puser.i_paid, "0元");
                } else if (deposite >= statement[0] - 5) {
                    notifyMoney("bcs", puser.i_paid, "5元");
                } else if (deposite >= statement[0] - 10) {
                    notifyMoney("bcs", puser.i_paid, "10元");
                } else {
                    // normal
                }
            }
        });
        
    }
    
    public void start() {
        if (isRun()) {
            logger.warn("monitor has already started");
            return;
        }
        new Thread(this, "monitor").start();
    }
    
    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("bcs.monitor.interval"));
        setInterval(second * 1000);
    }
    
    private static void notifyTime(String server, int paid, String account, String time) {
        CommonService.getChannelAccountByPaid(paid).forEach(user->{
            String notify_content = String.format("【定时提醒】 %s | %s", account, time);
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
            
            logger.info(String.format("notify time: paid=0x%08X, account=%s, time=%s", paid, account, time));
            switch (user.i_channel) {
            case CommonService.CHANNEL_WECHAT: {
                JSONObject data = new JSONObject();
                data.put("first",       String.format("尊敬的 %s ，您好！这是您的在租账号 %s 的定期提醒。", user.c_name, account));
                data.put("keyword1",    time); // 业务进度
                data.put("keyword2",    "正常"); // 业务状态
                data.put("remark",      "请关注您的账户余额，避免欠费。");
                JSONObject content = new JSONObject();
                content.put("template", "x013YAKdSrHnpNRxtzdpxRTyendpDrua-9tCnvCheq4");
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
            default: {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("type",    CommonService.TICKET_TYPE_MEMORY);
                args.put("title",   "【自动】【提醒】账号租期定时提醒");
                args.put("content", String.format("【账号租期定时提醒】游戏账号: %s | 租赁时长: %s",
                        account,
                        time));
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
    
    private static void notifyMoney(String server, int paid, String money_limit) {
        CommonService.getChannelAccountByPaid(paid).forEach(user->{
            String notify_content = String.format("【余额提醒】 %s", money_limit);
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
            
            logger.info(String.format("notify money: paid=0x%08X, money_limit=%s", paid, money_limit));
            switch (user.i_channel) {
            case CommonService.CHANNEL_WECHAT: {
                JSONObject data = new JSONObject();
                data.put("first",       String.format("尊敬的 %s ，您好！您的余额(除押金)已不足%s。", user.c_name, money_limit));
                data.put("keyword1",    "余额不足" + money_limit); // 业务进度
                data.put("keyword2",    "正常"); // 业务状态
                data.put("remark",      "请关注您的账户余额，避免欠费。");
                JSONObject content = new JSONObject();
                content.put("template", "x013YAKdSrHnpNRxtzdpxRTyendpDrua-9tCnvCheq4");
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
            default: {
                JSONObject args = new JSONObject();
                args.put("caid",    user.i_caid);
                args.put("type",    CommonService.TICKET_TYPE_MEMORY);
                args.put("title",   "【自动】【提醒】用户账户余额提醒");
                args.put("content", String.format("【用户账户余额提醒】 账户余额: %s",
                        money_limit));
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
