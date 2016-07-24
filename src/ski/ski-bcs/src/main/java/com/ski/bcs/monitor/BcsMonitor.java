package com.ski.bcs.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ski.bcs.Notifier;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanCommodity;
import com.ski.common.bean.BeanGameAccount;
import com.ski.common.bean.BeanOrder;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.util.FjLoopTask;
import net.sf.json.JSONObject;

public class BcsMonitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(BcsMonitor.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BcsMonitor() {}
    
    public void start() {
        if (isRun()) {
            logger.warn("monitor-bcs has already started");
            return;
        }
        new Thread(this, "monitor-bcs").start();
    }
    
    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("bcs.monitor.bcs.interval"));
        setInterval(second * 1000);
    }

    @Override
    public void perform() {
        resetInterval();
        
        // check time
        CommonService.getPlatformAccountAll().values().forEach(puser->{
            CommonService.getOrderByPaid(puser.i_paid)
                    .stream()
                    .filter(o->!o.isClose())
                    .forEach(o->{
                        o.commodities.values()
                                .stream()
                                .filter(c->!c.isClose())
                                .forEach(c->{
                                    try {
                                        BeanGameAccount account = CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16));
                                        Date date   = sdf.parse(c.t_begin);
                                        long delta  = System.currentTimeMillis() - date.getTime();
                                        if (delta >= 15 * 24 * 60 * 60 * 1000L) {
                                            Notifier.notifyTime("bcs",
                                                    puser.i_paid,
                                                    String.format("%s(%s)", account.c_user, "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"),
                                                    "15天");
                                        } else if (delta >= 7 * 24 * 60 * 60 * 1000L) {
                                            Notifier.notifyTime("bcs",
                                                    puser.i_paid,
                                                    String.format("%s(%s)", account.c_user, "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"),
                                                    "7天");
                                        } else {
                                            // normal
                                        }
                                    } catch (Exception e) {logger.error("check commodity time failed: " + c.i_oid + ":" + c.i_csn, e);}
                                });
                    });
        });
        
        // check cash
        CommonService.getPlatformAccountAll().values().forEach(puser->{
            boolean isCommodityOpen = false;
            for (BeanOrder o : CommonService.getOrderByPaid(puser.i_paid)) {
                if (o.isClose()) continue;
                
                for (BeanCommodity c : o.commodities.values()) {
                    if (!c.isClose()) {
                        isCommodityOpen = true;
                        break;
                    }
                }
                if (isCommodityOpen) break;
            }
            if (isCommodityOpen) {
                float   deposite    = Float.parseFloat(FjServerToolkit.getServerConfig("bcs.deposite"))
                        * (CommonService.getGameAccountByPaid(puser.i_paid, CommonService.RENT_TYPE_A).size()
                                + CommonService.getGameAccountByPaid(puser.i_paid, CommonService.RENT_TYPE_B).size());
                float[] statement   = CommonService.prestatementByPaid(puser.i_paid);
                if (deposite >= statement[0]) {
                    Notifier.notifyCashNotEnough("bcs", puser.i_paid, "0元");
                } else if (deposite >= statement[0] - 5) {
                    Notifier.notifyCashNotEnough("bcs", puser.i_paid, "5元");
                } else if (deposite >= statement[0] - 10) {
                    Notifier.notifyCashNotEnough("bcs", puser.i_paid, "10元");
                } else {
                    // normal
                }
            }
        });
        
        // check bind
        CommonService.getOrderAll().values()
                .stream()
                .filter(o->!o.isClose())
                .forEach(o->{
                    o.commodities.values()
                            .stream()
                            .filter(c->!c.isClose())
                            .forEach(c->{
                                BeanGameAccount account = CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16));
                                int state_a = CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_A);
                                int state_b = CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_B);
                                
                                if ((state_a == CommonService.RENT_STATE_IDLE && state_b == CommonService.RENT_STATE_RENT)
                                        || (state_a == CommonService.RENT_STATE_RENT && state_b == CommonService.RENT_STATE_IDLE)) { // condition: renting a or b, only one
                                    try {
                                        Date date       = sdf.parse(c.t_begin);
                                        long past       = 1 * 60 * 60 * 1000L;
                                        long interval   = 10 * 60 * 1000L;
                                        long check_from = System.currentTimeMillis() - past - interval;
                                        long check_to   = check_from + interval;
                                        if (check_from <= date.getTime() && date.getTime() < check_to) { // need to check
                                            int paid = CommonService.getPlatformAccountByCaid(o.i_caid);
                                            JSONObject args = new JSONObject();
                                            args.put("user", account.c_user);
                                            args.put("pass", account.c_pass_curr);
                                            FjDscpMessage rsp = CommonService.send("wa", CommonDefinition.ISIS.INST_ECOM_APPLY_GAME_ACCOUNT_VERIFY, args);
                                            if (CommonService.isResponseSuccess(rsp)) {
                                                if ("A".equals(c.c_arg1) && rsp.toString().contains(" unbinded")) {
                                                    Notifier.notifyBindAbnormally("bcs",
                                                            paid,
                                                            String.format("%s(%s)", account.c_user, "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"),
                                                            false);
                                                } else if ("B".equals(c.c_arg1) && rsp.toString().contains(" binded")) {
                                                    Notifier.notifyBindAbnormally("bcs",
                                                            paid,
                                                            String.format("%s(%s)", account.c_user, "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"),
                                                            true);
                                                } else {
                                                    // normal
                                                }
                                            } else {
                                                Notifier.notifyModifyAbnormally("bcs",
                                                        paid,
                                                        String.format("%s(%s)", account.c_user, "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"),
                                                        "用户名或密码不正确");
                                            }
                                        }
                                    } catch (Exception e) {logger.error("check commodity time failed: " + c.i_oid + ":" + c.i_csn, e);}
                                }
                            });
                });
    }
}
