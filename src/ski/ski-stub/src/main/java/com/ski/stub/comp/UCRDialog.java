package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;

import com.ski.stub.Service;
import com.ski.stub.bean.BeanChannelAccount;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;
import com.ski.stub.bean.BeanOrder;

public class UCRDialog extends JDialog {

    private static final long serialVersionUID = 5478304934566261533L;
    
    private float total_buy         = 0.0f;
    private float total_recharge    = 0.0f;
    private float total_rent        = 0.0f;
    private float total_coupon      = 0.0f;

    public UCRDialog(BeanChannelAccount user) {
        setModal(false);
        setTitle(String.format("用户消费报告(UCR) - %s", user.c_user));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(600, 800));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
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
                + "table {width:100%%; border: 0px; margin: 0px; border-collapse: collapse; border-spacing: 0px; font-family: '微软雅黑', 'Hiragino Sans GB'}"
                + "tr, td {border: 1px solid black; text-align: center; background-color: #DDDDDD}"
                + "h1 {color: #884444}"
                + "h2 {text-align: left; padding-left: 8px}"
                + ".category {background-color: #444488; color: #EEEEEE}"
                + "</style>"
                + "<table><tr><td><h1>%s的消费报告</h1></td></tr></table>"
                + "<table><tr><td colspan='4' class='category'><h2>购买</h2></th></tr>"
                + "%s</table>"
                + "<table><tr><td colspan='3' class='category'><h2>充值</h2></th></tr>"
                + "%s</table>"
                + "<table><tr><td colspan='8' class='category'><h2>租赁</h2></th></tr>"
                + "%s</table>"
                + "<table><tr><td colspan='3' class='category'><h2>优惠券</h2></th></tr>"
                + "%s</table>"
                + "<table><tr><td class='category'><h2>总计</h2></td></tr>"
                + "%s</table>",
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
        jep.setSelectionStart(0);
        jep.setSelectionEnd(0);
        
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("另存为图片"));
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            BufferedImage buffer = new BufferedImage(jep.getWidth(), jep.getHeight(), BufferedImage.TYPE_INT_RGB);
            jep.paint(buffer.getGraphics());
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(String.format("%s的消费报告%s.png", user.c_user, new SimpleDateFormat("-yyyyMMddHHmmss").format(new Date(System.currentTimeMillis())))));
            if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(UCRDialog.this)) {
                File file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".png")) file = new File(file.getAbsolutePath() + ".png");
                try {
                    ImageIO.write(buffer, "png", file);
                    JOptionPane.showConfirmDialog(null, "保存成功: " + file.getAbsolutePath(), "信息", JOptionPane.DEFAULT_OPTION);
                } catch (Exception e1) {
                    JOptionPane.showConfirmDialog(null, "保存失败: " + e1.getMessage(), "错误", JOptionPane.DEFAULT_OPTION);
                    e1.printStackTrace();
                }
            }
        });
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(jsp, BorderLayout.CENTER);
    }
    
    private String generateBuy(List<BeanOrder> orders) {
        StringBuilder buffer = new StringBuilder();
        StringBuilder totals = new StringBuilder();
        orders.forEach(order->{
            order.order_items.values().forEach(item->{
                if (0 != item.i_oper_type) return;
                
                if (0 == buffer.length()) buffer.append(generateTableRow("购买时间", "购买金额", "购买商品", "备注"));
                buffer.append(generateTableRow(item.t_oper_time, item.c_oper_arg0 + "元", item.c_oper_arg1, item.c_remark));
                totals.append(" " + item.c_oper_arg0);
            });
        });
        if (0 == buffer.length()) return generateTableRow("(没有购买)", 4);
        for (String s : totals.toString().split(" ")) total_buy += Float.parseFloat(s);
        buffer.append(generateTableRow(String.format("购买总计：%f元", total_buy), 4));
        return buffer.toString();
    }
    
    private String generateRecharge(List<BeanOrder> orders) {
        StringBuilder buffer = new StringBuilder();
        StringBuilder totals = new StringBuilder();
        orders.forEach(order->{
            order.order_items.values().forEach(item->{
                if (1 != item.i_oper_type) return;
                
                if (0 == buffer.length()) buffer.append(generateTableRow("充值时间", "充值金额", "备注"));
                buffer.append(generateTableRow(item.t_oper_time, item.c_oper_arg0 + "元", item.c_remark));
                if (0 != totals.length()) totals.append(" ");
                totals.append(item.c_oper_arg0);
            });
        });
        
        if (0 == buffer.length()) return generateTableRow("(没有充值)", 3);
        
        for (String s : totals.toString().split(" ")) total_recharge += Float.parseFloat(s);
        buffer.append(generateTableRow(String.format("充值总计：%f元", total_recharge), 3));
        return buffer.toString();
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
                                    BeanGameAccount bga = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
                                    BeanGame        bg  = null != bga ? Service.getOneGameOfGameAccount(bga.i_gaid) : null;
                                    account.put("user",   null != bga ? bga.c_user : "-");
                                    account.put("type",   item.c_oper_arg2);
                                    account.put("game",   null != bg ? bg.c_name_zh : "-");
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
                                    BeanGameAccount bga = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
                                    BeanGame        bg  = null != bga ? Service.getOneGameOfGameAccount(bga.i_gaid) : null;
                                    account.put("user",   null != bga ? bga.c_user : "-");
                                    account.put("type",   item.c_oper_arg2);
                                    account.put("game",   null != bg ? bg.c_name_zh : "-");
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
        
        if (total.isEmpty()) return generateTableRow("(没有租用)", 8);
        
        StringBuilder buffer = new StringBuilder();
        buffer.append(generateTableRow("游戏账号", "租赁类型", "游戏名称", "起租时间", "退租时间", "使用时长(除去停租)", "单价", "小计"));
        for (Map<String, Object> account : total.values()) {
            long time = 0.0f == (float) account.get("cost") ? (System.currentTimeMillis() - (long) account.get("last")) : (long) account.get("time");
            float rent = (float) account.get("uprice") / 1000 * (time / 24 / 60 / 60);
            total_rent += rent;
            String time_desc = String.format("%d天%d时%d分%d秒", (time / 1000 / 60 / 60 / 24), (time / 1000 / 60 / 60) % 24, (time / 1000 / 60) % 60, (time / 1000) % 60);
            buffer.append(generateTableRow(
                    account.get("user"),
                    account.get("type"),
                    account.get("game"),
                    sdf.format(new Date((long) account.get("begin"))),
                    0 == (long) account.get("end") ? "-" : sdf.format(new Date((long) account.get("end"))),
                    time_desc,
                    account.get("uprice") + "元/天",
                    rent + "元"));
        };
        buffer.append(generateTableRow(String.format("租金总计：%f元", total_rent), 8));
        return buffer.toString();
    }
    
    private String generateCoupon(List<BeanOrder> orders) {
        StringBuilder buffer = new StringBuilder();
        StringBuilder totals = new StringBuilder();
        orders.forEach(order->{
            order.order_items.values().forEach(item->{
                if (7 != item.i_oper_type) return;
                
                if (0 == buffer.length()) buffer.append(generateTableRow("获得时间", "金额", "备注"));
                buffer.append(generateTableRow(item.t_oper_time, item.c_oper_arg0 + "元", item.c_remark));
                if (0 != totals.length()) totals.append(" ");
                totals.append(item.c_oper_arg0);
            });
        });
        
        if (0 == buffer.length()) return generateTableRow("(没有优惠券)", 3);
        
        for (String s : totals.toString().split(" ")) total_coupon += Float.parseFloat(s);
        buffer.append(generateTableRow(String.format("优惠券总计：%f元", total_coupon), 3));
        return buffer.toString();
    }
    
    private String generateTotal(List<BeanOrder> orders) {
        DecimalFormat df = new DecimalFormat("###,###,###.##");
        return generateTableRow(String.format("账户余额：%s元", df.format(total_recharge + total_coupon - total_buy - total_rent)), 1)
                + generateTableRow(String.format("可退金额：%s元", df.format(total_coupon > total_buy + total_rent ? total_recharge : total_recharge + total_coupon - total_buy - total_rent)), 1);
    }
    
    private static String generateTableRow(Object... cols) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");
        for (Object col : cols) sb.append("<td>" + col.toString() + "</td>");
        sb.append("</tr>");
        return sb.toString();
    }
    
    private static String generateTableRow(Object col, int colnum) {
        return String.format("<tr><td colspan='%d' align='left' cellpadding='8px'>%s</td></tr>", colnum, col.toString());
    }
}
