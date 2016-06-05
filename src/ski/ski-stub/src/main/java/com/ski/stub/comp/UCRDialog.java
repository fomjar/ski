package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.ski.stub.Service;
import com.ski.stub.bean.BeanChannelAccount;
import com.ski.stub.bean.BeanOrder;

public class UCRDialog extends JDialog {

    private static final long serialVersionUID = 5478304934566261533L;
    
    private float total_buy         = 0.0f;
    private float total_recharge    = 0.0f;
    private float total_rent        = 0.0f;
    private float total_coupon      = 0.0f;

    public UCRDialog(Window owner, BeanChannelAccount user) {
        super(owner, String.format("用户消费报告(UCR) - %s", user.c_user));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(900, 900));
        setLocation(owner.getX() - (getWidth() - owner.getWidth()) / 2, owner.getY() - (getHeight() - owner.getHeight()) / 2);
        
        List<BeanOrder> orders = Service.map_order.values()
                .stream()
                .filter(order->{return order.i_caid == user.i_caid;})
                .collect(Collectors.toList());
        
        String text_buy         = generateBuy(orders);
        String text_recharge    = generateRecharge(orders);
        String text_rent        = generateRent(orders);
        String text_coupon      = generateCoupon(orders);
        String text_total       = generateTotal(orders);
        
        String text_main = String.format(
                  "<style type=\"text/css\">"
                + "table{border:0; margin:0; border-collapse:collapse; border-spacing:0}"
                + "td{border: 1px solid black}"
                + "h1, td{text-align: center}"
                + "</style>"
                + "<h1>%s 的消费报告</h1>"
                + "<h2>一、购买</h2>"
                + "%s<br/>"
                + "<h2>二、充值</h2>"
                + "%s<br/>"
                + "<h2>三、租赁</h2>"
                + "%s<br/>"
                + "<h2>四、优惠券</h2>"
                + "%s<br/>"
                + "<h2>五、总计</h2>"
                + "%s<br/>",
                user.c_user,
                text_buy,
                text_recharge,
                text_rent,
                text_coupon,
                text_total);
        
        JEditorPane jep = new JEditorPane("text/html", text_main);
        jep.setEditable(false);
        JScrollPane jsp = new JScrollPane(jep);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jsp, BorderLayout.CENTER);
    }
    
    private String generateBuy(List<BeanOrder> orders) {
        StringBuilder buffer = new StringBuilder();
        StringBuilder totals = new StringBuilder();
        orders.forEach(order->{
            order.order_items.values().forEach(item->{
                if (0 != item.i_oper_type) return;
                
                if (0 == buffer.length()) buffer.append("<table><tr><td>购买时间</td><td>购买金额</td><td>购买商品</td><td>备注</td></tr>");
                buffer.append(String.format("<tr><td>%s</td><td>%s元</td><td>%s</td><td>%s</td></tr>", item.t_oper_time, item.c_oper_arg0, item.c_oper_arg1, item.c_remark));
                totals.append(" " + item.c_oper_arg0);
            });
        });
        if (0 == buffer.length()) return "(没有购买)<br/>";
        else buffer.append("</table>");
        for (String s : totals.toString().split(" ")) total_buy += Float.parseFloat(s);
        return "购买总计：" + total_buy + "元<br/>购买清单：<br/>" + buffer.toString();
    }
    
    private String generateRecharge(List<BeanOrder> orders) {
        StringBuilder buffer = new StringBuilder();
        StringBuilder totals = new StringBuilder();
        orders.forEach(order->{
            order.order_items.values().forEach(item->{
                if (1 != item.i_oper_type) return;
                
                if (0 == buffer.length()) buffer.append("<table><tr><td>充值时间</td><td>充值金额</td><td>备注</td></tr>");
                buffer.append(String.format("<tr><td>%s</td><td>%s元</td><td>%s</td></tr>", item.t_oper_time, item.c_oper_arg0, item.c_remark));
                if (0 != totals.length()) totals.append(" ");
                totals.append(item.c_oper_arg0);
            });
        });
        
        if (0 == buffer.length()) return "(没有充值)<br/>";
        
        else buffer.append("</table>");
        for (String s : totals.toString().split(" ")) total_recharge += Float.parseFloat(s);
        return "充值总计：" + total_recharge + "元<br/>充值清单：<br/>" + buffer.toString();
    }
    
    private String generateRent(List<BeanOrder> orders) {
        Map<String, Map<String, Object>> total = new HashMap<String, Map<String, Object>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        orders.forEach(order->{
            order.order_items.values()
                    .stream()
                    .sorted((item1, item2)->{
                        return item1.i_oisn - item2.i_oisn;
                    })
                    .forEach(item->{
                        if (2 != item.i_oper_type && 3 != item.i_oper_type && 4 != item.i_oper_type && 5 != item.i_oper_type && 6 != item.i_oper_type) return;
                        
                        try {
                            switch (item.i_oper_type) {
                                case 2: { // 2 - 起租
                                    Map<String, Object> account = new HashMap<String, Object>();
                                    account.put("user",   Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16)).c_user);
                                    account.put("type",   item.c_oper_arg2);
                                    account.put("begin",  sdf.parse(item.t_oper_time).getTime());
                                    account.put("end",    0l);
                                    account.put("last",   account.get("begin")); // 上一次计时开始时间
                                    account.put("time",   0l);
                                    account.put("uprice", Float.parseFloat(item.c_oper_arg0));
                                    account.put("cost",   0.0f);
                                    total.put(item.c_oper_arg1 + item.c_oper_arg2, account);
                                    break;
                                }
                                case 3: { // 3 - 退租
                                    Map<String, Object> account = total.get(item.c_oper_arg1 + item.c_oper_arg2);
                                    long time = sdf.parse(item.t_oper_time).getTime() - (long)account.get("last");
                                    account.put("time", (long) account.get("time") + time);
                                    account.put("cost", (float) account.get("cost") + (time / 1000) * ((float) account.get("uprice") / 24 / 60 / 60));
                                    account.put("end", sdf.parse(item.t_oper_time).getTime());
                                    break;
                                }
                                case 4: { // 4 - 停租
                                    Map<String, Object> account = total.get(item.c_oper_arg1 + item.c_oper_arg2);
                                    long time = sdf.parse(item.t_oper_time).getTime() - (long)account.get("last");
                                    account.put("time", (long) account.get("time") + time);
                                    account.put("cost", (float) account.get("cost") + (time / 1000) * ((float) account.get("uprice") / 24 / 60 / 60));
                                    break;
                                }
                                case 5: { // 5 - 续租
                                    Map<String, Object> account = total.get(item.c_oper_arg1 + item.c_oper_arg2);
                                    account.put("last", sdf.parse(item.t_oper_time).getTime()); // 上一次计时开始时间
                                    break;
                                }
                                case 6: { // 6 - 换租
                                    Map<String, Object> account = total.get(item.c_oper_arg3 + item.c_oper_arg4);
                                    long time = sdf.parse(item.t_oper_time).getTime() - (long)account.get("last");
                                    account.put("time", (long) account.get("time") + time);
                                    account.put("cost", (float) account.get("cost") + (time / 1000) * ((float) account.get("uprice") / 24 / 60 / 60));
                                    account.put("end", sdf.parse(item.t_oper_time).getTime());
                                    
                                    account = new HashMap<String, Object>();
                                    account.put("user",   Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16)).c_user);
                                    account.put("type",   item.c_oper_arg2);
                                    account.put("begin",  sdf.parse(item.t_oper_time).getTime());
                                    account.put("end",    0l);
                                    account.put("last",   account.get("begin")); // 上一次计时开始时间
                                    account.put("time",   0l);
                                    account.put("uprice", Float.parseFloat(item.c_oper_arg0));
                                    account.put("cost",   0.0f);
                                    total.put(item.c_oper_arg1 + item.c_oper_arg2, account);
                                    break;
                                }
                            }
                        } catch (ParseException e) {e.printStackTrace();}
                    });
        });
        
        if (total.isEmpty()) return "(没有租用)<br/>";
        
        StringBuilder buffer = new StringBuilder();
        buffer.append("<table><tr><td>游戏账号</td><td>租赁类型</td><td>起租时间</td><td>退租时间</td><td>使用时长(除去停租)</td><td>单价</td><td>小计</td></tr>");
        for (Map<String, Object> account : total.values()) {
            long time = 0.0f == (float) account.get("cost") ? (System.currentTimeMillis() - (long) account.get("last")) : (long) account.get("time");
            float rent = (float) account.get("uprice") / 1000 * (time / 24 / 60 / 60);
            total_rent += rent;
            String time_desc = String.format("%d天%d时%d分%d秒", (time / 1000 / 60 / 60 / 24), (time / 1000 / 60 / 60) % 24, (time / 1000 / 60) % 60, (time / 1000) % 60);
            buffer.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s元/天</td><td>%s元</td></tr>",
                    account.get("user"),
                    account.get("type"),
                    sdf.format(new Date((long) account.get("begin"))),
                    0 == (long) account.get("end") ? "-" : sdf.format(new Date((long) account.get("end"))),
                    time_desc,
                    account.get("uprice"),
                    rent));
        };
        buffer.append("</table>");
        return "租金总计：" + total_rent + "元<br/>租赁清单：<br/>" + buffer.toString();
    }
    
    private String generateCoupon(List<BeanOrder> orders) {
        StringBuilder buffer = new StringBuilder();
        StringBuilder totals = new StringBuilder();
        orders.forEach(order->{
            order.order_items.values().forEach(item->{
                if (7 != item.i_oper_type) return;
                
                if (0 == buffer.length()) buffer.append("<table><tr><td>获得时间</td><td>面值</td><td>备注</td></tr>");
                buffer.append(String.format("<tr><td>%s</td><td>%s元</td><td>%s</td></tr>", item.t_oper_time, item.c_oper_arg0, item.c_remark));
                if (0 != totals.length()) totals.append(" ");
                totals.append(item.c_oper_arg0);
            });
        });
        
        if (0 == buffer.length()) return "(没有充值)<br/>";
        
        else buffer.append("</table>");
        float totalf = 0.0f;
        for (String s : totals.toString().split(" ")) total_coupon += Float.parseFloat(s);
        return "优惠券总计：" + totalf + "元<br/>优惠券清单：<br/>" + buffer.toString();
    }
    
    private String generateTotal(List<BeanOrder> orders) {
        DecimalFormat df = new DecimalFormat("###,###,###.##");
        return String.format(
                  "账户余额：%s元<br/>"
                + "可退金额：%s元",
                df.format(total_recharge + total_coupon - total_buy - total_rent),
                df.format(total_coupon > total_buy + total_rent ? total_recharge : total_recharge + total_coupon - total_buy - total_rent));
    }
}
