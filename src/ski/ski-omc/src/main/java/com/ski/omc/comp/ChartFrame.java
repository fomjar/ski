package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Date;

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

import com.ski.common.CommonService;
import com.ski.omc.UIToolkit;

public class ChartFrame extends JFrame {

    private static final long serialVersionUID = 7856940672729570702L;
    private JTabbedPane charts;
    private JPanel      charts_user;
    private JPanel      charts_order;
    private JPanel		charts_sale;
    
    public ChartFrame() {
        setTitle("数据统计");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(1280, 800));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        initChartTheme();
        
        charts_user = new JPanel();
        charts_user.setLayout(new BoxLayout(charts_user, BoxLayout.Y_AXIS));
        charts_user.add(createChartUserDistribution());
        charts_user.add(createChartUserEncreaseLastMonth());
        charts_user.add(createChartUserTotalLastMonth());
        
        charts_order = new JPanel();
        charts_order.setLayout(new BoxLayout(charts_order, BoxLayout.Y_AXIS));
        charts_order.add(createChartCommodityTypeDistribution());
        charts_order.add(createChartCommodityBeginEncreaseLastMonth());
        charts_order.add(createChartCommodityEndEncreaseLastMonth());
        charts_order.add(createChartCommodityBeginTotalLastMonth());
        charts_order.add(createChartCommodityEndTotalLastMonth());
        
        charts_sale = new JPanel();
        charts_sale.setLayout(new BoxLayout(charts_sale, BoxLayout.Y_AXIS));
        charts_sale.add(createChartMoneyDistribution());
        charts_sale.add(createChartMoneyConsumeEncreaseLastMonth());
        charts_sale.add(createChartMoneyConsumeTotalLastMonth());
        
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
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(count[i], "最近一个月", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("营业额增量曲线", "时间", "数量", dataset));
    }
    
    private static JPanel createChartMoneyConsumeTotalLastMonth() {
        int     total = 28;
        float[] base  = new float[] {0.00f};
        float[] count = new float[total];
        
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
                        return;
                    }

                    int day = (int) delta;
                    for (int i = 0; i <= day; i++) count[i] += c.i_expense;
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(base[0] + count[i], "最近一个月", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("营业额总量曲线", "时间", "数量", dataset));
    }
    
    private static JPanel createChartCommodityBeginEncreaseLastMonth() {
        int     total = 28;
        int[]   count = new int[total];
        
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
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(count[i], "最近一个月", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单开启增量曲线", "时间", "数量", dataset));
    }
    
    private static JPanel createChartCommodityBeginTotalLastMonth() {
        int     total = 28;
        int[]   base  = new int[] {0};
        int[]   count = new int[total];
        
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
                        return;
                    }

                    int day = (int) delta;
                    for (int i = 0; i <= day; i++) count[i]++;
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(base[0] + count[i], "最近一个月", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单开启总量曲线", "时间", "数量", dataset));
    }
    
    private static JPanel createChartCommodityEndEncreaseLastMonth() {
        int     total = 28;
        int[]   count = new int[total];
        
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
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(count[i], "最近一个月", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单关闭增量曲线", "时间", "数量", dataset));
    }
    
    private static JPanel createChartCommodityEndTotalLastMonth() {
        int     total = 28;
        int[]   base  = new int[] {0};
        int[]   count = new int[total];
        
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
                        return;
                    }

                    int day = (int) delta;
                    for (int i = 0; i <= day; i++) count[i]++;
                } catch (Exception e) {e.printStackTrace();}
            });
        });
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat formater = new SimpleDateFormat("MM月dd日");
        for (int i = total - 1; i >= 0; i--) {
            dataset.addValue(base[0] + count[i], "最近一个月", formater.format(new Date(today.getTime() - i * oneday)));
        }
        
        return createLineChartPanel(ChartFactory.createLineChart("订单关闭总量曲线", "时间", "数量", dataset));
    }
    
    private static JPanel createChartCommodityTypeDistribution() {
        int count_a = CommonService.getOrderAll().values().stream().map(o->o.commodities.values().stream().filter(c->"A".equals(c.c_arg1)).count()).reduce(0L, (c1, c2)->c1 + c2).intValue();
        int count_b = CommonService.getOrderAll().values().stream().map(o->o.commodities.values().stream().filter(c->"B".equals(c.c_arg1)).count()).reduce(0L, (c1, c2)->c1 + c2).intValue();

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A租用户", count_a);
        dataset.setValue("B租用户", count_b);
        JFreeChart chart = ChartFactory.createPieChart3D("订单类型分布", dataset);
        ((PiePlot) chart.getPlot()).setLabelGenerator(new StandardPieSectionLabelGenerator("{0}({1},{2})")); 
        JPanel panel = new ChartPanel(chart);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        return panel;
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
}
