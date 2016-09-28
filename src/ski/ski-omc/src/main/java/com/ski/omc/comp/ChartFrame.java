package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.omc.UIToolkit;

public class ChartFrame extends JFrame {

    private static final long serialVersionUID = 7856940672729570702L;
    private JTabbedPane charts;
    private JPanel      charts_user;
    private JPanel      charts_order;
    private JPanel        charts_sale;
//    private JPanel        charts_access;
    private JPanel        charts_game;
    
    public ChartFrame() {
        setTitle("数据统计");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(1280, 800));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
//        CommonService.updateAccessRecord();
        initChartTheme();
        
        charts_user = new JPanel();
        charts_user.setLayout(new BoxLayout(charts_user, BoxLayout.Y_AXIS));
        charts_user.add(createChartUserDistribution());
        charts_user.add(createChartUserEncreaseLastMonth());
        charts_user.add(createChartUserTotalLastMonth());
        
        charts_order = new JPanel();
        charts_order.setLayout(new BoxLayout(charts_order, BoxLayout.Y_AXIS));
        charts_order.add(createChartCommodityTypeDistributionByCommodityType());
        charts_order.add(createChartCommodityTypeDistributionByUserType());
        charts_order.add(createChartCommodityBeginEncreaseLastMonthByCommodityType());
        charts_order.add(createChartCommodityEndEncreaseLastMonthByCommodityType());
        charts_order.add(createChartCommodityBeginEncreaseLastMonthByUserType());
        charts_order.add(createChartCommodityEndEncreaseLastMonthByUserType());
        charts_order.add(createChartCommodityBeginTotalLastMonthByCommodityType());
        charts_order.add(createChartCommodityEndTotalLastMonthByCommodityType());
        charts_order.add(createChartCommodityBeginTotalLastMonthByUserType());
        charts_order.add(createChartCommodityEndTotalLastMonthByUserType());
        
        charts_sale = new JPanel();
        charts_sale.setLayout(new BoxLayout(charts_sale, BoxLayout.Y_AXIS));
        charts_sale.add(createChartMoneyDistribution());
        charts_sale.add(createChartMoneyConsumeEncreaseLastMonth());
        charts_sale.add(createChartMoneyConsumeTotalLastMonth());
        
//        charts_access = new JPanel();
//        charts_access.setLayout(new BoxLayout(charts_access, BoxLayout.Y_AXIS));
//        charts_access.add(createChartAccessDistribution());
//        charts_access.add(createChartAccessGameDistribution());
        
        charts_game = new JPanel();
        charts_game.setLayout(new BoxLayout(charts_game, BoxLayout.Y_AXIS));
        
        charts = new JTabbedPane();
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(charts_user, BorderLayout.NORTH);
            JScrollPane jsp = new JScrollPane(panel);
            jsp.getVerticalScrollBar().setUnitIncrement(16);
            charts.add("用户统计", jsp);
        }
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(charts_order, BorderLayout.NORTH);
            JScrollPane jsp = new JScrollPane(panel);
            jsp.getVerticalScrollBar().setUnitIncrement(16);
            charts.add("订单统计", jsp);
        }
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(charts_sale, BorderLayout.NORTH);
            JScrollPane jsp = new JScrollPane(panel);
            jsp.getVerticalScrollBar().setUnitIncrement(16);
            charts.add("销售统计", jsp);
        }
//        {
//            JPanel panel = new JPanel();
//            panel.setLayout(new BorderLayout());
//            panel.add(charts_access, BorderLayout.NORTH);
//            JScrollPane jsp = new JScrollPane(panel);
//            jsp.getVerticalScrollBar().setUnitIncrement(16);
//            charts.add("访问统计", jsp);
//        }
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(charts_game, BorderLayout.NORTH);
            JScrollPane jsp = new JScrollPane(panel);
            jsp.getVerticalScrollBar().setUnitIncrement(16);
            charts.add("游戏分析", jsp);
        }
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(charts, BorderLayout.CENTER);
    }
    
    private static void initChartTheme() {
        //创建主题样式  
        StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
        //设置标题字体  
        standardChartTheme.setExtraLargeFont(UIToolkit.FONT.deriveFont(20.0f));
        //设置图例的字体  
        standardChartTheme.setRegularFont(UIToolkit.FONT);
        //设置轴向的字体  
        standardChartTheme.setLargeFont(UIToolkit.FONT);
        //应用主题样式  
        ChartFactory.setChartTheme(standardChartTheme);
    }
    
    private static JPanel createChartUserDistribution() {
        long count_taobao = CommonService.getChannelAccountAll().values().stream().filter(u->u.i_channel == CommonService.CHANNEL_TAOBAO).count();
        long count_wechat = CommonService.getChannelAccountAll().values().stream().filter(u->u.i_channel == CommonService.CHANNEL_WECHAT).count();
        long count_alipay = CommonService.getChannelAccountAll().values().stream().filter(u->u.i_channel == CommonService.CHANNEL_ALIPAY).count();
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("淘宝", count_taobao);
        dataset.setValue("微信", count_wechat);
        dataset.setValue("支付宝", count_alipay);
        JFreeChart chart = ChartFactory.createPieChart3D("用户分布", dataset);
        ((PiePlot) chart.getPlot()).setLabelGenerator(new StandardPieSectionLabelGenerator("{0}({1},{2})")); 
        JPanel panel = new ChartPanel(chart);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        return panel;
    }
    
    private static JPanel createChartUserEncreaseLastMonth() {
        int     total = 28;
        int[]   count = new int[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getChannelAccountAll().values().forEach(user->{
            try {
                Date    date = parser.parse(user.t_create);
                double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                if (delta >= total) return;

                int day = (int) delta;
                count[day]++;
            } catch (Exception e) {e.printStackTrace();}
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(count[i], "最近一个月", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("用户增量曲线", "时间", "数量", dataset));
    }
    
    private static JPanel createChartUserTotalLastMonth() {
        int     total = 28;
        int[]   base  = new int[] {0};
        int[]   count = new int[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getChannelAccountAll().values().forEach(user->{
            try {
                Date    date = parser.parse(user.t_create);
                double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                if (delta >= total) {   // 过去的作为基数
                    base[0]++;
                    return;
                }

                int day = (int) delta;
                for (int i = 0; i <= day; i++) count[i]++;
            } catch (Exception e) {e.printStackTrace();}
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(base[0] + count[i], "最近一个月", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("用户总量曲线", "时间", "数量", dataset));
    }
    
    private static JPanel createChartMoneyDistribution() {
        float count_cash    = CommonService.getPlatformAccountAll().values().stream().map(p->p.i_cash).reduce(0.00f, (c1, c2)->c1 + c2).floatValue();
        float count_coupon  = CommonService.getPlatformAccountAll().values().stream().map(p->p.i_coupon).reduce(0.00f, (c1, c2)->c1 + c2).floatValue();
        float count_consume = CommonService.getOrderAll().values()
                .stream()
                .map(o->{
                    return o.commodities.values()
                            .stream()
                            .filter(c->c.isClose())
                            .map(c->c.i_expense)
                            .reduce(0.00f, (c1, c2)->c1 + c2)
                            .floatValue();
                })
                .reduce(0.00f, (m1, m2)->m1 + m2)
                .floatValue();
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("现金",  count_cash);
        dataset.setValue("优惠券", count_coupon);
        dataset.setValue("消费",  count_consume);
        JFreeChart chart = ChartFactory.createPieChart3D("金额分布", dataset);
        ((PiePlot) chart.getPlot()).setLabelGenerator(new StandardPieSectionLabelGenerator("{0}({1}元,{2})")); 
        JPanel panel = new ChartPanel(chart);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        return panel;
    }
    
    private static JPanel createChartMoneyConsumeEncreaseLastMonth() {
        int     total = 28;
        float[] count = new float[total];
        float[] count_a = new float[total];
        float[] count_b = new float[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getOrderAll().values().forEach(o->{
            o.commodities.values().forEach(c->{
                try {
                    if (!c.isClose()) return;
                    
                    Date    date = parser.parse(c.t_end);
                    double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                    if (delta >= total) return;

                    int day = (int) delta;
                    count[day] += c.i_expense;
                    switch (c.c_arg1) {
                    case "A": count_a[day] += c.i_expense; break;
                    case "B": count_b[day] += c.i_expense; break;
                    }
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(count[i], "所有订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(count_a[i], "A类订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(count_b[i], "B类订单", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("营业额增量曲线", "时间", "数量", dataset));
    }
    
    private static JPanel createChartMoneyConsumeTotalLastMonth() {
        int     total = 28;
        float[] base  = new float[] {0.00f};
        float[] base_a  = new float[] {0.00f};
        float[] base_b  = new float[] {0.00f};
        float[] count = new float[total];
        float[] count_a = new float[total];
        float[] count_b = new float[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getOrderAll().values().forEach(o->{
            o.commodities.values().forEach(c->{
                try {
                    if (!c.isClose()) return;
                    
                    Date    date = parser.parse(c.t_end);
                    double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                    if (delta >= total) {   // 过去的作为基数
                        base[0] += c.i_expense;
                        switch (c.c_arg1) {
                        case "A": base_a[0] += c.i_expense; break;
                        case "B": base_b[0] += c.i_expense; break;
                        }
                        return;
                    }

                    int day = (int) delta;
                    for (int i = 0; i <= day; i++) {
                        count[i] += c.i_expense;
                        switch (c.c_arg1) {
                        case "A": count_a[i] += c.i_expense; break;
                        case "B": count_b[i] += c.i_expense; break;
                        }
                    }
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(base[0] + count[i], "所有订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(base_a[0] + count_a[i], "A类订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(base_b[0] + count_b[i], "B类订单", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("营业额总量曲线", "时间", "数量", dataset));
    }
    
    private static JPanel createChartCommodityBeginEncreaseLastMonthByCommodityType() {
        int     total = 28;
        int[]   count = new int[total];
        int[]    count_a = new int[total];
        int[]    count_b = new int[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getOrderAll().values().forEach(o->{
            o.commodities.values().forEach(c->{
                try {
                    Date    date = parser.parse(c.t_begin);
                    double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                    if (delta >= total) return;

                    int day = (int) delta;
                    count[day]++;
                    switch (c.c_arg1) {
                    case "A": count_a[day]++; break;
                    case "B": count_b[day]++; break;
                    }
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(count[i], "所有订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(count_a[i], "A类订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(count_b[i], "B类订单", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单开启增量曲线(按订单类型)", "时间", "数量", dataset));
    }
    private static JPanel createChartCommodityBeginEncreaseLastMonthByUserType() {
        int     total = 28;
        int[]   count = new int[total];
        int[]    count_tb = new int[total];
        int[]    count_wc = new int[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getOrderAll().values().forEach(o->{
            o.commodities.values().forEach(c->{
                try {
                    Date    date = parser.parse(c.t_begin);
                    double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                    if (delta >= total) return;

                    int day = (int) delta;
                    count[day]++;
                    switch (CommonService.getChannelAccountByCaid(o.i_caid).i_channel) {
                    case CommonService.CHANNEL_TAOBAO: count_tb[day]++; break;
                    case CommonService.CHANNEL_WECHAT: count_wc[day]++; break;
                    }
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(count[i], "所有订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(count_tb[i], "淘宝订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(count_wc[i], "微信订单", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单开启增量曲线(按用户类型)", "时间", "数量", dataset));
    }
    
    private static JPanel createChartCommodityBeginTotalLastMonthByCommodityType() {
        int     total = 28;
        int[]   base  = new int[] {0};
        int[]   base_a  = new int[] {0};
        int[]   base_b  = new int[] {0};
        int[]   count = new int[total];
        int[]   count_a = new int[total];
        int[]   count_b = new int[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getOrderAll().values().forEach(o->{
            o.commodities.values().forEach(c->{
                try {
                    Date    date = parser.parse(c.t_begin);
                    double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                    if (delta >= total) {   // 过去的作为基数
                        base[0]++;
                        switch (c.c_arg1) {
                        case "A": base_a[0]++; break;
                        case "B": base_b[0]++; break;
                        }
                        return;
                    }

                    int day = (int) delta;
                    for (int i = 0; i <= day; i++) {
                        count[i]++;
                        switch (c.c_arg1) {
                        case "A": count_a[i]++; break;
                        case "B": count_b[i]++; break;
                        }
                    }
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(base[0] + count[i], "所有订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(base_a[0] + count_a[i], "A类订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(base_b[0] + count_b[i], "B类订单", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单开启总量曲线(按订单类型)", "时间", "数量", dataset));
    }
    private static JPanel createChartCommodityBeginTotalLastMonthByUserType() {
        int     total = 28;
        int[]   base  = new int[] {0};
        int[]   base_tb  = new int[] {0};
        int[]   base_wc  = new int[] {0};
        int[]   count = new int[total];
        int[]   count_tb = new int[total];
        int[]   count_wc = new int[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getOrderAll().values().forEach(o->{
            o.commodities.values().forEach(c->{
                try {
                    Date    date = parser.parse(c.t_begin);
                    double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                    if (delta >= total) {   // 过去的作为基数
                        base[0]++;
                        switch (CommonService.getChannelAccountByCaid(o.i_caid).i_channel) {
                        case CommonService.CHANNEL_TAOBAO: base_tb[0]++; break;
                        case CommonService.CHANNEL_WECHAT: base_wc[0]++; break;
                        }
                        return;
                    }

                    int day = (int) delta;
                    for (int i = 0; i <= day; i++) {
                        count[i]++;
                        switch (CommonService.getChannelAccountByCaid(o.i_caid).i_channel) {
                        case CommonService.CHANNEL_TAOBAO: count_tb[i]++; break;
                        case CommonService.CHANNEL_WECHAT: count_wc[i]++; break;
                        }
                    }
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(base[0] + count[i], "所有订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(base_tb[0] + count_tb[i], "淘宝订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(base_wc[0] + count_wc[i], "微信订单", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单开启总量曲线(按用户类型)", "时间", "数量", dataset));
    }
    
    private static JPanel createChartCommodityEndEncreaseLastMonthByCommodityType() {
        int     total = 28;
        int[]   count = new int[total];
        int[]   count_a = new int[total];
        int[]   count_b = new int[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getOrderAll().values().forEach(o->{
            o.commodities.values().forEach(c->{
                try {
                    if (!c.isClose()) return;
                    
                    Date    date = parser.parse(c.t_end);
                    double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                    if (delta >= total) return;

                    int day = (int) delta;
                    count[day]++;
                    switch (c.c_arg1) {
                    case "A": count_a[day]++; break;
                    case "B": count_b[day]++; break;
                    }
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(count[i], "所有订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(count_a[i], "A类订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(count_b[i], "B类订单", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单关闭增量曲线(按订单类型)", "时间", "数量", dataset));
    }
    
    private static JPanel createChartCommodityEndEncreaseLastMonthByUserType() {
        int     total = 28;
        int[]   count = new int[total];
        int[]   count_tb = new int[total];
        int[]   count_wc = new int[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getOrderAll().values().forEach(o->{
            o.commodities.values().forEach(c->{
                try {
                    if (!c.isClose()) return;
                    
                    Date    date = parser.parse(c.t_end);
                    double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                    if (delta >= total) return;

                    int day = (int) delta;
                    count[day]++;
                    switch (CommonService.getChannelAccountByCaid(o.i_caid).i_channel) {
                    case CommonService.CHANNEL_TAOBAO: count_tb[day]++; break;
                    case CommonService.CHANNEL_WECHAT: count_wc[day]++; break;
                    }
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(count[i], "所有订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(count_tb[i], "淘宝订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(count_wc[i], "微信订单", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单关闭增量曲线(按用户类型)", "时间", "数量", dataset));
    }
    
    
    private static JPanel createChartCommodityEndTotalLastMonthByCommodityType() {
        int     total = 28;
        int[]   base  = new int[] {0};
        int[]   base_a  = new int[] {0};
        int[]   base_b  = new int[] {0};
        int[]   count = new int[total];
        int[]   count_a = new int[total];
        int[]   count_b = new int[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getOrderAll().values().forEach(o->{
            o.commodities.values().forEach(c->{
                try {
                    if (!c.isClose()) return;
                    
                    Date    date = parser.parse(c.t_end);
                    double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                    if (delta >= total) {   // 过去的作为基数
                        base[0]++;
                        switch (c.c_arg1) {
                        case "A": base_a[0]++; break;
                        case "B": base_b[0]++; break;
                        }
                        return;
                    }

                    int day = (int) delta;
                    for (int i = 0; i <= day; i++) {
                        count[i]++;
                        switch (c.c_arg1) {
                        case "A": count_a[i]++; break;
                        case "B": count_b[i]++; break;
                        }
                    }
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(base[0] + count[i], "所有订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(base_a[0] + count_a[i], "A类订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(base_b[0] + count_b[i], "B类订单", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单关闭总量曲线(按订单类型)", "时间", "数量", dataset));
    }
    
    private static JPanel createChartCommodityEndTotalLastMonthByUserType() {
        int     total = 28;
        int[]   base  = new int[] {0};
        int[]   base_tb  = new int[] {0};
        int[]   base_wc  = new int[] {0};
        int[]   count = new int[total];
        int[]   count_tb = new int[total];
        int[]   count_wc = new int[total];
        
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date    today = new Date();
        long    oneday = 1000L * 60 * 60 * 24;
        CommonService.getOrderAll().values().forEach(o->{
            o.commodities.values().forEach(c->{
                try {
                    if (!c.isClose()) return;
                    
                    Date    date = parser.parse(c.t_end);
                    double  delta = Math.ceil((double) today.getTime() / oneday) - Math.ceil((double) date.getTime() / oneday);
                    if (delta >= total) {   // 过去的作为基数
                        base[0]++;
                        switch (CommonService.getChannelAccountByCaid(o.i_caid).i_channel) {
                        case CommonService.CHANNEL_TAOBAO: base_tb[0]++; break;
                        case CommonService.CHANNEL_WECHAT: base_wc[0]++; break;
                        }
                        return;
                    }

                    int day = (int) delta;
                    for (int i = 0; i <= day; i++) {
                        count[i]++;
                        switch (CommonService.getChannelAccountByCaid(o.i_caid).i_channel) {
                        case CommonService.CHANNEL_TAOBAO: count_tb[i]++; break;
                        case CommonService.CHANNEL_WECHAT: count_wc[i]++; break;
                        }
                    }
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(base[0] + count[i], "所有订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(base_tb[0] + count_tb[i], "淘宝订单", formater.format(new Date(today.getTime() - i * oneday)));
            dataset.addValue(base_wc[0] + count_wc[i], "微信订单", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单关闭总量曲线(按用户类型)", "时间", "数量", dataset));
    }
    
    
    private static JPanel createChartCommodityTypeDistributionByCommodityType() {
        int count_a = CommonService.getOrderAll().values().stream().map(o->o.commodities.values().stream().filter(c->"A".equals(c.c_arg1)).count()).reduce(0L, (c1, c2)->c1 + c2).intValue();
        int count_b = CommonService.getOrderAll().values().stream().map(o->o.commodities.values().stream().filter(c->"B".equals(c.c_arg1)).count()).reduce(0L, (c1, c2)->c1 + c2).intValue();

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A租用户", count_a);
        dataset.setValue("B租用户", count_b);
        JFreeChart chart = ChartFactory.createPieChart3D("订单类型分布(按订单类型)", dataset);
        ((PiePlot) chart.getPlot()).setLabelGenerator(new StandardPieSectionLabelGenerator("{0}({1},{2})")); 
        JPanel panel = new ChartPanel(chart);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        return panel;
    }
    
    private static JPanel createChartCommodityTypeDistributionByUserType() {
        int count_tb = CommonService.getOrderAll().values().stream().filter(o->CommonService.getChannelAccountByCaid(o.i_caid).i_channel == CommonService.CHANNEL_TAOBAO).map(o->o.commodities.size()).reduce(0, (c1, c2)->c1 + c2).intValue();
        int count_wc = CommonService.getOrderAll().values().stream().filter(o->CommonService.getChannelAccountByCaid(o.i_caid).i_channel == CommonService.CHANNEL_WECHAT).map(o->o.commodities.size()).reduce(0, (c1, c2)->c1 + c2).intValue();

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("淘宝用户", count_tb);
        dataset.setValue("微信用户", count_wc);
        JFreeChart chart = ChartFactory.createPieChart3D("订单类型分布(按用户类型)", dataset);
        ((PiePlot) chart.getPlot()).setLabelGenerator(new StandardPieSectionLabelGenerator("{0}({1},{2})")); 
        JPanel panel = new ChartPanel(chart);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        return panel;
    }

    
    @SuppressWarnings("unused")
    private static JPanel createChartAccessDistribution() {
        int[] inst_apply_platform_account_money = new int[] {0};
        int[] inst_apply_rent_begin = new int[] {0};
        int[] inst_apply_rent_end = new int[] {0};
        int[] inst_query_game = new int[] {0};
        int[] inst_query_order = new int[] {0};
        int[] inst_query_platform_account_map = new int[] {0};
        int[] inst_query_platform_account_money = new int[] {0};
        int[] inst_update_platform_account_money = new int[] {0};
        
        CommonService.getAccessRecordAll().forEach(access->{
            // 排除后续步骤
            if (null != getAccessArgument(access.c_local, "step")) return;
        
            int inst = getAccessInstruction(access.c_local);
            switch (inst) {
            case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY:
                inst_apply_platform_account_money[0]++;
                break;
            case CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_BEGIN:
                inst_apply_rent_begin[0]++;
                break;
            case CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_END:
                inst_apply_rent_end[0]++;
                break;
            case CommonDefinition.ISIS.INST_ECOM_QUERY_GAME:
                inst_query_game[0]++;
                break;
            case CommonDefinition.ISIS.INST_ECOM_QUERY_ORDER:
                inst_query_order[0]++;
                break;
            case CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT_MAP:
                inst_query_platform_account_map[0]++;
                break;
            case CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT_MONEY:
                inst_query_platform_account_money[0]++;
                break;
            case CommonDefinition.ISIS.INST_ECOM_UPDATE_PLATFORM_ACCOUNT_MAP:
                inst_update_platform_account_money[0]++;
                break;
            }
        });
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("充值/退款",     inst_apply_platform_account_money[0]);
        dataset.setValue("起租",     inst_apply_rent_begin[0]);
        dataset.setValue("退租",     inst_apply_rent_end[0]);
        dataset.setValue("全搜索",     inst_query_game[0]);
        dataset.setValue("正在玩",     inst_query_order[0]);
        dataset.setValue("淘宝＋",     inst_query_platform_account_map[0]);
        dataset.setValue("小金库",     inst_query_platform_account_money[0]);
        dataset.setValue("绑定",     inst_update_platform_account_money[0]);
        JFreeChart chart = ChartFactory.createPieChart3D("用户访问分布", dataset);
        ((PiePlot) chart.getPlot()).setLabelGenerator(new StandardPieSectionLabelGenerator("{0}({1},{2})")); 
        JPanel panel = new ChartPanel(chart);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        return panel;
    }
    
    @SuppressWarnings("unused")
    private static JPanel createChartAccessGameDistribution() {
        Map<Integer, Integer> count = new HashMap<Integer, Integer>();
        CommonService.getAccessRecordAll().forEach(access->{
            if (CommonDefinition.ISIS.INST_ECOM_QUERY_GAME != getAccessInstruction(access.c_local)) return;
            if (null == getAccessArgument(access.c_local, "gid")) return;
            
            int gid = Integer.parseInt(getAccessArgument(access.c_local, "gid"), 16);
            if (!count.containsKey(gid)) count.put(gid, 1);
            else count.put(gid, count.get(gid) + 1);
        });
        
        List<Integer> keys = new ArrayList<Integer>(count.keySet());
        Collections.sort(keys, (k1, k2)->count.get(k2) - count.get(k1));
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 19; i >= 0; i--) {
            int gid = keys.get(i);
            int c   = count.get(gid);
            dataset.addValue(c, "游戏访问量", CommonService.getGameByGid(gid).c_name_zh_cn);
        }
        
        return createBarChartPanel(ChartFactory.createBarChart3D("游戏访问TOP20", "游戏", "访问量", dataset));
    }
    
    private static JPanel createLineChartPanel(JFreeChart chart) {
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setUpperMargin(0.2);
        
        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        ((LineAndShapeRenderer) renderer).setBaseShapesVisible(true);
        
        JPanel panel = new ChartPanel(chart);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        return panel;
    }
    
    private static JPanel createBarChartPanel(JFreeChart chart) {
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setUpperMargin(0.2);
        
        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        
        JPanel panel = new ChartPanel(chart);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        return panel;
    }
    
    private static int getAccessInstruction(String local) {
        if (!local.contains("inst=")) return -1;
        
        return Integer.parseInt(getAccessArgument(local, "inst"), 16);
    }
    
    private static String getAccessArgument(String local, String key) {
        if (!local.contains(key + "=")) return null;
        
        int begin     = local.indexOf(key + "=") + key.length() + 1;
        int end        = local.indexOf("&", begin);
        if (-1 == end) end = local.length();
        
        return local.substring(begin, end);
    }
    
    @SuppressWarnings("unused")
    private static String getAccessPath(String local) {
        if (local.contains("?")) return local.substring(local.lastIndexOf("|") + 1, local.indexOf("?"));
        
        return local.substring(local.lastIndexOf("|") + 1);
    }
}
