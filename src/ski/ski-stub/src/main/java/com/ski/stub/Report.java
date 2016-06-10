package com.ski.stub;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ski.stub.bean.BeanChannelAccount;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;
import com.ski.stub.bean.BeanOrder;
import com.ski.stub.bean.BeanOrderItem;

public class Report {
    
    public static Map<String, Object> createUCD(int caid) {
        if (!Service.map_channel_account.containsKey(caid)) return null;
        
        float buy_total         = 0.0f;
        Set<Map<String, Object>>            buy_object      = new LinkedHashSet<Map<String, Object>>();
        float recharge_total    = 0.0f;
        Set<Map<String, Object>>            recharge_object = new LinkedHashSet<Map<String, Object>>();
        float rent_total        = 0.0f;
        Map<String, Map<String, Object>>    rent_object     = new LinkedHashMap<String, Map<String, Object>>(); // key = user + type
        float coupon_total      = 0.0f;
        Set<Map<String, Object>>            coupon_object   = new LinkedHashSet<Map<String, Object>>();
        float balance     = 0.0f;
        float refund      = 0.0f;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<BeanOrder> orders = Service.map_order.values()
                .stream()
                .filter(order->{return order.i_caid == caid;})
                .collect(Collectors.toList());
        for (BeanOrder order : orders) {
            List<BeanOrderItem> items = order.order_items.values()
                    .stream()
                    .sorted((item1, item2)->{
                        try {return (int) (sdf.parse(item1.t_oper_time).getTime() - sdf.parse(item2.t_oper_time).getTime());}
                        catch (Exception e) {e.printStackTrace();}
                        return 0;
                    })
                    .collect(Collectors.toList());
            for (BeanOrderItem item : items) {
                switch (item.i_oper_type) {
                    case Service.OPER_TYPE_BUY: {
                        float money = Float.parseFloat(item.c_oper_arg0);
                        buy_total += money;
                        Map<String, Object> buy = new LinkedHashMap<String, Object>();
                        buy_object.add(buy);
                        buy.put("time",     item.t_oper_time);
                        buy.put("remark",   item.c_remark);
                        buy.put("money",    money);
                        buy.put("product",  item.c_oper_arg1);
                        break;
                    }
                    case Service.OPER_TYPE_RECHARGE: {
                        float money = Float.parseFloat(item.c_oper_arg0);
                        recharge_total += money;
                        Map<String, Object> recharge = new LinkedHashMap<String, Object>();
                        recharge_object.add(recharge);
                        recharge.put("time",    item.t_oper_time);
                        recharge.put("remark",  item.c_remark);
                        recharge.put("money",   money);
                        break;
                    }
                    case Service.OPER_TYPE_RENT_BEGIN: {
                        BeanGameAccount account = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
                        Map<String, Object> rent = new HashMap<String, Object>();
                        rent_object.put(item.c_oper_arg1 + item.c_oper_arg2, rent);
                        rent.put("account", account);
                        rent.put("type",    item.c_oper_arg2);
                        rent.put("games",   Service.getGameAccountGames(account.i_gaid));
                        rent.put("begin",   item.t_oper_time);
                        rent.put("uprice",  Float.parseFloat(item.c_oper_arg0));
                        rent.put("time",    0l);
                        rent.put("money",   0.0f);
                        break;
                    }
                    case Service.OPER_TYPE_RENT_END: {
                        Map<String, Object> rent = rent_object.get(item.c_oper_arg1 + item.c_oper_arg2);
                        if (!rent.containsKey("end")) {
                            rent.put("end", item.t_oper_time);
                            rentStatement(rent);
                        }
                        break;
                    }
                    case Service.OPER_TYPE_RENT_PAUSE: {
                        Map<String, Object> rent = rent_object.get(item.c_oper_arg1 + item.c_oper_arg2);
                        if (!rent.containsKey("lastpause")) {
                            rent.put("lastpause", item.t_oper_time);
                            rentStatement(rent);
                        }
                        break;
                    }
                    case Service.OPER_TYPE_RENT_RESUME: {
                        Map<String, Object> rent = rent_object.get(item.c_oper_arg1 + item.c_oper_arg2);
                        if (rent.containsKey("lastpause") && !rent.containsKey("lastresume")) {
                            rent.remove("lastpause");
                            rent.put("lastresume", item.t_oper_time);
                        }
                        break;
                    }
                    case Service.OPER_TYPE_RENT_SWAP: {
                        Map<String, Object> rent_from = rent_object.get(item.c_oper_arg3 + item.c_oper_arg4);
                        if (!rent_from.containsKey("end")) {
                            rent_from.put("end", item.t_oper_time);
                            rentStatement(rent_from);
                        }
                        BeanGameAccount account_to = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
                        Map<String, Object> rent_to = new HashMap<String, Object>();
                        rent_object.put(item.c_oper_arg1 + item.c_oper_arg2, rent_to);
                        rent_to.put("account", account_to);
                        rent_to.put("type",    item.c_oper_arg2);
                        rent_to.put("games",   Service.getGameAccountGames(account_to.i_gaid));
                        rent_to.put("begin",   item.t_oper_time);
                        rent_to.put("money",   0.0f);
                        rent_to.put("uprice",  Float.parseFloat(item.c_oper_arg0));
                        rent_to.put("time",    0l);
                        break;
                    }
                    case Service.OPER_TYPE_COUPON: {
                        float money = Float.parseFloat(item.c_oper_arg0);
                        coupon_total += money;
                        Map<String, Object> coupon = new LinkedHashMap<String, Object>();
                        coupon_object.add(coupon);
                        coupon.put("time",     item.t_oper_time);
                        coupon.put("remark",   item.c_remark);
                        coupon.put("money",    money);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        
        for (Map<String, Object> rent : rent_object.values()) {
            // 未退租的以当前时间结算
            if (rent.containsKey("begin") && !rent.containsKey("lastpause") && !rent.containsKey("lastresume") && !rent.containsKey("end")) {
                rentStatement(rent);
            }
            // 租金总计
            rent_total += (float) rent.get("money");
        }
        
        balance = recharge_total + coupon_total - buy_total - rent_total;
        refund  = coupon_total > (buy_total + rent_total) ? recharge_total : balance;
        
        Map<String, Object> ucd = new HashMap<String, Object>();
        ucd.put("user",             Service.map_channel_account.get(caid));
        ucd.put("buy.total",        buy_total);
        ucd.put("buy.object",       buy_object);
        ucd.put("recharge.total",   recharge_total);
        ucd.put("recharge.object",  recharge_object);
        ucd.put("rent.total",       rent_total);
        ucd.put("rent.object",      rent_object);
        ucd.put("coupon.total",     coupon_total);
        ucd.put("coupon.object",    coupon_object);
        ucd.put("balance",          balance);
        ucd.put("refund",           refund);
        return ucd;
    }
    
    /**
     * 租赁结算，不修改或删除时刻字段
     * 
     * @param rent 租赁账号上下文
     */
    private static void rentStatement(Map<String, Object> rent) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long time = 0l;
            long now = 0l, last = 0l;
            if (rent.containsKey("end")) {
                now  = sdf.parse((String) rent.get("end")).getTime();
                last = rent.containsKey("lastresume") ? sdf.parse((String) rent.get("lastresume")).getTime() : sdf.parse((String) rent.get("begin")).getTime();
            } else if (rent.containsKey("lastpause")) {
                now  = sdf.parse((String) rent.get("lastpause")).getTime();
                last = rent.containsKey("lastresume") ? sdf.parse((String) rent.get("lastresume")).getTime() : sdf.parse((String) rent.get("begin")).getTime();
            } else {
                now  = System.currentTimeMillis();
                last = rent.containsKey("lastresume") ? sdf.parse((String) rent.get("lastresume")).getTime() : sdf.parse((String) rent.get("begin")).getTime();
            }
            time = now - last;
            rent.put("time", (long) rent.get("time") + time);
            calculateMoney(rent);
        } catch (ParseException e) {e.printStackTrace();}
    }
    
    private static void calculateMoney(Map<String, Object> rent) {
        long time = (long) rent.get("time");
        float uprice = (float) rent.get("uprice");
        rent.put("money", (uprice / 2) * (time / 1000 / 60 / 60 / 12)); // 半天的维度
    }
    
    @SuppressWarnings("unchecked")
    public static String createUCR(int caid) {
        Map<String, Object> ucd = createUCD(caid);
        
        StringBuilder ucr = new StringBuilder(1024);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("###,###,###.##");
        
        BeanChannelAccount user = (BeanChannelAccount) ucd.get("user");
        ucr.append(createReportHead(String.format("%s的消费报告", user.c_user)));
        
        List<Object> buy = new LinkedList<Object>();
        buy.add(new String[] {"购买时间", "购买商品", "购买价格", "备注"});
        for (Map<String, Object> buy_object : (Set<Map<String, Object>>)ucd.get("buy.object")) {
            buy.add(new String[] {(String) buy_object.get("time"),
                    (String) buy_object.get("product"),
                    df.format(buy_object.get("money")) + "元",
                    0 == ((String) buy_object.get("remark")).length() ? "-" : (String) buy_object.get("remark")});
        }
        buy.add(String.format("购买小计：%s元", df.format(ucd.get("buy.total"))));
        ucr.append(createReportTable("购买", buy, 4));
        
        List<Object> recharge = new LinkedList<Object>();
        recharge.add(new String[] {"充值时间", "充值金额", "备注"});
        for (Map<String, Object> recharge_object : (Set<Map<String, Object>>)ucd.get("recharge.object")) {
            recharge.add(new String[] {(String) recharge_object.get("time"),
                    df.format(recharge_object.get("money")) + "元",
                    0 == ((String) recharge_object.get("remark")).length() ? "-" : (String) recharge_object.get("remark")});
        }
        recharge.add(String.format("充值小计：%s元", df.format(ucd.get("recharge.total"))));
        ucr.append(createReportTable("充值", recharge, 3));
        
        List<Object> rent = new LinkedList<Object>();
        rent.add(new String[] {"游戏账号", "租赁类型", "游戏名称", "起租时间", "退租时间", "有效时长", "单价", "小计"});
        ((Map<String, Map<String, Object>>)ucd.get("rent.object")).values()
                .stream()
                .sorted((rent1, rent2)->{
                    try {
                        long begin1 = sdf.parse((String) rent1.get("begin")).getTime();
                        long begin2 = sdf.parse((String) rent2.get("begin")).getTime();
                        return (int) (begin1 - begin2);
                    } catch (ParseException e) {e.printStackTrace();}
                    return 0;
                })
                .forEach(rent_object->{
                    List<BeanGame> games = (List<BeanGame>) rent_object.get("games");
                    long time = (long) rent_object.get("time");
                    rent.add(new String[] {((BeanGameAccount) rent_object.get("account")).c_user,
                            (String) rent_object.get("type"),
                            (null != games && !games.isEmpty()) ? games.get(0).c_name_zh : "-",
                            (String) rent_object.get("begin"),
                            rent_object.containsKey("end") ? (String) rent_object.get("end") : "-",
                            formatTimeLong(time),
                            df.format(rent_object.get("uprice")) + "元/天",
                            df.format(rent_object.get("money")) + "元"});
                });
        rent.add(String.format("租赁小计：%s元", df.format(ucd.get("rent.total"))));
        ucr.append(createReportTable("租赁", rent, 8));
        
        List<Object> coupon = new LinkedList<Object>();
        coupon.add(new String[] {"获得时间", "金额", "备注"});
        for (Map<String, Object> coupon_object : (Set<Map<String, Object>>)ucd.get("coupon.object")) {
            coupon.add(new String[] {(String) coupon_object.get("time"),
                    df.format(coupon_object.get("money")) + "元",
                    0 == ((String) coupon_object.get("remark")).length() ? "-" : (String) coupon_object.get("remark")});
        }
        coupon.add(String.format("优惠券小计：%s元", df.format(ucd.get("coupon.total"))));
        ucr.append(createReportTable("优惠券", coupon, 3));
        
        List<Object> total = new LinkedList<Object>();
        total.add(String.format("账户余额：%s元", df.format(ucd.get("balance"))));
        total.add(String.format("可退金额：%s元", df.format(ucd.get("refund"))));
        ucr.append(createReportTable("总计", total, 1));
        
        return ucr.toString();
    }
    
    @SuppressWarnings("unchecked")
    public static String createOIR(BeanOrderItem item) {
        BeanChannelAccount user = Service.map_channel_account.get(Service.map_order.get(item.i_oid).i_caid);
        Map<String, Object> ucd = createUCD(user.i_caid);
        
        StringBuilder oir = new StringBuilder(1024);
        DecimalFormat df = new DecimalFormat("###,###,###.##");
        
        switch (item.i_oper_type) {
            case Service.OPER_TYPE_BUY: {
                oir.append(createReportHead(String.format("%s的购买报告", user.c_user)));
                List<Object> rows = new LinkedList<Object>();
                rows.add(new String[] {"购买时间",  item.t_oper_time});
                rows.add(new String[] {"购买金额",  df.format(Float.parseFloat(item.c_oper_arg0)) + "元"});
                rows.add(new String[] {"购买商品",  item.c_oper_arg1});
                rows.add(new String[] {"备注",     0 == item.c_remark.length() ? "-" : item.c_remark});
                rows.add(new String[] {"账户余额",  df.format(ucd.get("balance")) + "元"});
                rows.add(new String[] {"可退金额",  df.format(ucd.get("refund"))});
                oir.append(createReportTable(null, rows, 2));
                break;
            }
            case Service.OPER_TYPE_RECHARGE: {
                oir.append(createReportHead(String.format("%s的充值报告", user.c_user)));
                List<Object> rows = new LinkedList<Object>();
                rows.add(new String[] {"充值时间",  item.t_oper_time});
                rows.add(new String[] {"充值金额",  df.format(Float.parseFloat(item.c_oper_arg0)) + "元"});
                rows.add(new String[] {"备注",     0 == item.c_remark.length() ? "-" : item.c_remark});
                rows.add(new String[] {"账户余额",  df.format(ucd.get("balance")) + "元"});
                rows.add(new String[] {"可退金额",  df.format(ucd.get("refund")) + "元"});
                oir.append(createReportTable(null, rows, 2));
                break;
            }
            case Service.OPER_TYPE_RENT_BEGIN: {
                oir.append(createReportHead(String.format("%s的起租报告", user.c_user)));
                Map<String, Object> rent    = (Map<String, Object>) ((Map<String, Object>) ucd.get("rent.object")).get(item.c_oper_arg1 + item.c_oper_arg2);
                BeanGameAccount account     = (BeanGameAccount) rent.get("account");
                List<BeanGame>  games       = (List<BeanGame>) rent.get("games");
                List<Object> rows = new LinkedList<Object>();
                rows.add(new String[] {"游戏账号",  account.c_user});
                rows.add(new String[] {"当前密码",  account.c_pass_curr});
                rows.add(new String[] {"租赁类型",  item.c_oper_arg2});
                rows.add(new String[] {"包含游戏",  (null == games || games.isEmpty()) ? "-" : games.stream().map(game->game.c_name_zh).collect(Collectors.joining(", "))});
                rows.add(new String[] {"起租时间",  (String) rent.get("begin")});
                rows.add(new String[] {"租赁单价",  df.format(rent.get("uprice")) + "元/天"});
                rows.add(new String[] {"备注",     0 == item.c_remark.length() ? "-" : item.c_remark});
                rows.add(new String[] {"账户余额",  df.format(ucd.get("balance")) + "元"});
                rows.add(new String[] {"可退金额",  df.format(ucd.get("refund")) + "元"});
                oir.append(createReportTable(null, rows, 2));
                break;
            }
            case Service.OPER_TYPE_RENT_END: {
                oir.append(createReportHead(String.format("%s的退租报告", user.c_user)));
                Map<String, Object> rent    = (Map<String, Object>) ((Map<String, Object>) ucd.get("rent.object")).get(item.c_oper_arg1 + item.c_oper_arg2);
                BeanGameAccount account     = (BeanGameAccount) rent.get("account");
                List<BeanGame>  games       = (List<BeanGame>) rent.get("games");
                List<Object> rows = new LinkedList<Object>();
                rows.add(new String[] {"游戏账号",  account.c_user});
                rows.add(new String[] {"当前密码",  account.c_pass_curr});
                rows.add(new String[] {"租赁类型",  item.c_oper_arg2});
                rows.add(new String[] {"包含游戏",  (null == games || games.isEmpty()) ? "-" : games.stream().map(game->game.c_name_zh).collect(Collectors.joining(", "))});
                rows.add(new String[] {"起租时间",  (String) rent.get("begin")});
                rows.add(new String[] {"退租时间",  (String) rent.get("end")});
                rows.add(new String[] {"租赁单价",  df.format(rent.get("uprice")) + "元/天"});
                rows.add(new String[] {"有效时长",  formatTimeLong((long) rent.get("time"))});
                rows.add(new String[] {"费用小计",  df.format(rent.get("money")) + "元"});
                rows.add(new String[] {"备注",     0 == item.c_remark.length() ? "-" : item.c_remark});
                rows.add(new String[] {"账户余额",  df.format(ucd.get("balance")) + "元"});
                rows.add(new String[] {"可退金额",  df.format(ucd.get("refund")) + "元"});
                oir.append(createReportTable(null, rows, 2));
                break;
            }
            case Service.OPER_TYPE_RENT_PAUSE: {
                oir.append(createReportHead(String.format("%s的停租报告", user.c_user)));
                Map<String, Object> rent    = (Map<String, Object>) ((Map<String, Object>) ucd.get("rent.object")).get(item.c_oper_arg1 + item.c_oper_arg2);
                BeanGameAccount account     = (BeanGameAccount) rent.get("account");
                List<BeanGame>  games       = (List<BeanGame>) rent.get("games");
                List<Object> rows = new LinkedList<Object>();
                rows.add(new String[] {"游戏账号",  account.c_user});
                rows.add(new String[] {"当前密码",  account.c_pass_curr});
                rows.add(new String[] {"租赁类型",  item.c_oper_arg2});
                rows.add(new String[] {"包含游戏",  (null == games || games.isEmpty()) ? "-" : games.stream().map(game->game.c_name_zh).collect(Collectors.joining(", "))});
                rows.add(new String[] {"起租时间",  (String) rent.get("begin")});
                rows.add(new String[] {"停租时间",  (String) rent.get("lastpause")});
                rows.add(new String[] {"租赁单价",  df.format(rent.get("uprice")) + "元/天"});
                rows.add(new String[] {"有效时长",  formatTimeLong((long) rent.get("time"))});
                rows.add(new String[] {"费用小计",  df.format(rent.get("money")) + "元"});
                rows.add(new String[] {"备注",     0 == item.c_remark.length() ? "-" : item.c_remark});
                rows.add(new String[] {"账户余额",  df.format(ucd.get("balance")) + "元"});
                rows.add(new String[] {"可退金额",  df.format(ucd.get("refund")) + "元"});
                oir.append(createReportTable(null, rows, 2));
                break;
            }
            case Service.OPER_TYPE_RENT_RESUME: {
                oir.append(createReportHead(String.format("%s的续租报告", user.c_user)));
                Map<String, Object> rent    = (Map<String, Object>) ((Map<String, Object>) ucd.get("rent.object")).get(item.c_oper_arg1 + item.c_oper_arg2);
                BeanGameAccount account     = (BeanGameAccount) rent.get("account");
                List<BeanGame>  games       = (List<BeanGame>) rent.get("games");
                List<Object> rows = new LinkedList<Object>();
                rows.add(new String[] {"游戏账号",  account.c_user});
                rows.add(new String[] {"当前密码",  account.c_pass_curr});
                rows.add(new String[] {"租赁类型",  item.c_oper_arg2});
                rows.add(new String[] {"包含游戏",  (null == games || games.isEmpty()) ? "-" : games.stream().map(game->game.c_name_zh).collect(Collectors.joining(", "))});
                rows.add(new String[] {"起租时间",  (String) rent.get("begin")});
                rows.add(new String[] {"续租时间",  (String) rent.get("lastresume")});
                rows.add(new String[] {"租赁单价",  df.format(rent.get("uprice")) + "元/天"});
                rows.add(new String[] {"有效时长",  formatTimeLong((long) rent.get("time"))});
                rows.add(new String[] {"费用小计",  df.format(rent.get("money")) + "元"});
                rows.add(new String[] {"备注",     0 == item.c_remark.length() ? "-" : item.c_remark});
                rows.add(new String[] {"账户余额",  df.format(ucd.get("balance")) + "元"});
                rows.add(new String[] {"可退金额",  df.format(ucd.get("refund")) + "元"});
                oir.append(createReportTable(null, rows, 2));
                break;
            }
            case Service.OPER_TYPE_RENT_SWAP: {
                oir.append(createReportHead(String.format("%s的换租报告", user.c_user)));
                Map<String, Object> rent_to         = (Map<String, Object>) ((Map<String, Object>) ucd.get("rent.object")).get(item.c_oper_arg1 + item.c_oper_arg2);
                BeanGameAccount     account_to      = (BeanGameAccount) rent_to.get("account");
                List<BeanGame>      games_to        = (List<BeanGame>) rent_to.get("games");
                Map<String, Object> rent_from       = (Map<String, Object>) ((Map<String, Object>) ucd.get("rent.object")).get(item.c_oper_arg3 + item.c_oper_arg4);
                BeanGameAccount     account_from    = (BeanGameAccount) rent_from.get("account");
                List<BeanGame>      games_from      = (List<BeanGame>) rent_from.get("games");
                List<Object> rows = new LinkedList<Object>();
                rows.add(new String[] {"游戏账号",  account_from.c_user});
                rows.add(new String[] {"当前密码",  account_from.c_pass_curr});
                rows.add(new String[] {"租赁类型",  item.c_oper_arg4});
                rows.add(new String[] {"包含游戏",  (null == games_from || games_from.isEmpty()) ? "-" : games_from.stream().map(game->game.c_name_zh).collect(Collectors.joining(", "))});
                rows.add(new String[] {"起租时间",  (String) rent_from.get("begin")});
                rows.add(new String[] {"退租时间",  (String) rent_from.get("end")});
                rows.add(new String[] {"租赁单价",  df.format(rent_from.get("uprice")) + "元/天"});
                rows.add(new String[] {"有效时长",  formatTimeLong((long) rent_from.get("time"))});
                rows.add(new String[] {"费用小计",  df.format(rent_from.get("money")) + "元"});
                oir.append(createReportTable("老帐号", rows, 2));
                rows.clear();
                rows.add(new String[] {"游戏账号",  account_to.c_user});
                rows.add(new String[] {"当前密码",  account_to.c_pass_curr});
                rows.add(new String[] {"租赁类型",  item.c_oper_arg2});
                rows.add(new String[] {"包含游戏",  (null == games_to || games_to.isEmpty()) ? "-" : games_to.stream().map(game->game.c_name_zh).collect(Collectors.joining(", "))});
                rows.add(new String[] {"起租时间",  (String) rent_to.get("begin")});
                rows.add(new String[] {"租赁单价",  df.format(rent_to.get("uprice")) + "元/天"});
                oir.append(createReportTable("新帐号", rows, 2));
                rows.clear();
                rows.add(new String[] {"备注",     0 == item.c_remark.length() ? "-" : item.c_remark});
                rows.add(new String[] {"账户余额",  df.format(ucd.get("balance")) + "元"});
                rows.add(new String[] {"可退金额",  df.format(ucd.get("refund")) + "元"});
                oir.append(createReportTable("总计", rows, 2));
                break;
            }
            case Service.OPER_TYPE_COUPON: {
                oir.append(createReportHead(String.format("%s的优惠券报告", user.c_user)));
                List<Object> rows = new LinkedList<Object>();
                rows.add(new String[] {"获得时间",  item.t_oper_time});
                rows.add(new String[] {"券面金额",  df.format(Float.parseFloat(item.c_oper_arg0)) + "元"});
                rows.add(new String[] {"备注",     0 == item.c_remark.length() ? "-" : item.c_remark});
                rows.add(new String[] {"账户余额",  df.format(ucd.get("balance")) + "元"});
                rows.add(new String[] {"可退金额",  df.format(ucd.get("refund")) + "元"});
                oir.append(createReportTable(null, rows, 2));
                break;
            }
        }
        return oir.toString();
    }
    
    private static String formatTimeLong(long time) {
        return String.format("%d天%d时%d分%d秒", (time / 1000 / 60 / 60 / 24), (time / 1000 / 60 / 60) % 24, (time / 1000 / 60) % 60, (time / 1000) % 60);
    }
    
    private static String createReportHead(String title) {
        return String.format("<table><tr><td><h1>%s</h1></td></tr></table>", title);
    }
    
    private static String createReportTable(String category, List<Object> data, int maxcol) {
        StringBuilder sbtable = new StringBuilder(512);
        sbtable.append("<table>");
        if (null != category && 0 < category.length())
            sbtable.append(String.format("<tr><td colspan='%d' class='category'><h2>%s</h2></td></tr>", maxcol, category));
        for (Object row : data) {
            StringBuilder sbrow = new StringBuilder(128);
            sbrow.append("<tr>");
            if (row instanceof String[]) {
                for (String col : (String[]) row) {
                    sbrow.append(String.format("<td>%s</td>", col));
                }
            } else {
                sbrow.append(String.format("<td colspan='%d' align='right' cellpadding='8px'>%s</td>", maxcol, row.toString()));
            }
            sbrow.append("</tr>");
            sbtable.append(sbrow);
        }
        sbtable.append("</table>");
        return sbtable.toString();
    }

}
