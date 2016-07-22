package com.ski.omc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.fomjar.widget.FjTextArea;
import com.fomjar.widget.FjTextField;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanCommodity;
import com.ski.common.bean.BeanGame;
import com.ski.common.bean.BeanGameAccount;
import com.ski.common.bean.BeanOrder;
import com.ski.common.bean.BeanPlatformAccount;
import com.ski.common.bean.BeanTag;
import com.ski.omc.comp.ManageGameAccount;
import com.ski.omc.comp.OCRDialog;
import com.ski.omc.comp.StepStepDialog;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class UIToolkit {
    
    public static final Font FONT = new Font("楷体", Font.PLAIN, 14);

    public static final Color COLOR_MODIFYING = Color.blue;
    
    public static void initUI() {
        try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {e.printStackTrace();}
        
//        UIManager.getLookAndFeelDefaults().put("Label.font",        FONT);
//        UIManager.getLookAndFeelDefaults().put("Table.font",        FONT);
//        UIManager.getLookAndFeelDefaults().put("TableHeader.font",  FONT);
//        UIManager.getLookAndFeelDefaults().put("TextField.font",    FONT);
//        UIManager.getLookAndFeelDefaults().put("TextArea.font",     FONT);
//        UIManager.getLookAndFeelDefaults().put("TitledBorder.font", FONT);
//        UIManager.getLookAndFeelDefaults().put("CheckBox.font",     FONT);
//        UIManager.getLookAndFeelDefaults().put("RadioButton.font",  FONT);
//        UIManager.getLookAndFeelDefaults().put("ComboBox.font",     FONT);
//        UIManager.getLookAndFeelDefaults().put("Button.font",       FONT);
//        UIManager.getLookAndFeelDefaults().put("Panel.font",        FONT);
//        UIManager.getLookAndFeelDefaults().put("FilePane.font",     FONT);
//        UIManager.getLookAndFeelDefaults().put("Menu.font",         FONT);
//        UIManager.getLookAndFeelDefaults().put("MenuItem.font",     FONT);
        
//        UIManager.getLookAndFeelDefaults().forEach((key, value)->{System.out.println(key + "=" + value);});
    }
    
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    
    public static void doLater(Runnable task) {pool.submit(task);}
    
    public static JPanel createBasicInfoLabel(String label, JComponent field) {
        return createBasicInfoLabel(label, field, null);
    }
    
    public static JPanel createBasicInfoLabel(String label, JComponent field, JButton button) {
        JLabel jlabel = new JLabel(label);
        jlabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 16));
        jlabel.setFont(field.getFont());
        jlabel.setForeground(field.getForeground());
        
        JPanel jpanel = new JPanel();
        jpanel.setLayout(new BorderLayout());
        jpanel.add(jlabel, BorderLayout.WEST);
        jpanel.add(field, BorderLayout.CENTER);
        
        if (null != button) {
            button.setPreferredSize(new Dimension(button.getPreferredSize().width, field.getPreferredSize().height));
            jpanel.add(button, BorderLayout.EAST);
        }
        
        return jpanel;
    }
    
    public static void createGame() {
        FjTextField c_country   = new FjTextField();
        FjTextField t_sale      = new FjTextField();
        FjTextField c_name_zh   = new FjTextField();
        c_country.setDefaultTips("国家");
        t_sale.setDefaultTips("发售日期");
        c_name_zh.setDefaultTips("简体中文名");
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));
        panel.add(c_country);
        panel.add(t_sale);
        panel.add(c_name_zh);
        
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建游戏", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == c_name_zh.getText().length()) {
                JOptionPane.showMessageDialog(null, "游戏名称一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            JSONObject args = new JSONObject();
            if (0 != c_country.getText().length())  args.put("country", c_country.getText());
            if (0 != t_sale.getText().length())     args.put("sale",    t_sale.getText());
            if (0 != c_name_zh.getText().length())  args.put("name_zh", c_name_zh.getText());
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME, args);
            CommonService.updateGame();
            UIToolkit.showServerResponse(rsp);
            break;
        }
    }
    
    public static void createGameAccount() {
        FjTextField c_user  = new FjTextField();
        FjTextField c_pass  = new FjTextField();
        FjTextField t_birth = new FjTextField();
        c_user.setDefaultTips("用户名");
        c_pass.setDefaultTips("密码");
        t_birth.setDefaultTips("出生日期");
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));
        panel.add(c_user);
        panel.add(c_pass);
        panel.add(t_birth);
        
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建游戏账号", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == c_user.getText().length()) {
                JOptionPane.showMessageDialog(null, "用户名一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (0 == c_pass.getText().length()) {
                JOptionPane.showMessageDialog(null, "密码一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            JSONObject args = new JSONObject();
            if (0 != c_user.getText().length())     args.put("user",        c_user.getText());
            if (0 != c_pass.getText().length())     args.put("pass_curr",   c_pass.getText());
            if (0 != t_birth.getText().length())    args.put("birth",       t_birth.getText());
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
            CommonService.updateGameAccount();
            if (!UIToolkit.showServerResponse(rsp)) break;
        
            List<BeanGameAccount> accounts = CommonService.getGameAccountByUser(c_user.getText());
            if (1 == accounts.size()) new ManageGameAccount(accounts.get(0).i_gaid).setVisible(true);
            break;
        }
    }
    
    /**
     * 
     * @return caid
     */
    public static int createChannelAccount() {
        int caid = -1;
        JComboBox<String>   i_channel = new JComboBox<String>(new String[] {"淘  宝", "微  信", "支付宝"}); // ordered
        i_channel.setEditable(false);
        FjTextField c_user  = new FjTextField();
        FjTextField c_phone = new FjTextField();
        FjTextField c_name  = new FjTextField();
        JCheckBox   is_recharge_cash    = new JCheckBox("充值现金", true);
        FjTextField i_cash              = new FjTextField("150.00");
        JCheckBox   is_recharge_coupon  = new JCheckBox("充值优惠券", true);
        FjTextField i_coupon            = new FjTextField("5.00");
        JCheckBox   is_open_commodity   = new JCheckBox("创建商品", true);
        
        i_channel.addItemListener(e->{
            switch (i_channel.getSelectedIndex()) {
            case 0:
                is_recharge_cash.setSelected(true);
                is_recharge_coupon.setSelected(true);
                is_open_commodity.setSelected(true);
                break;
            case 1:
            case 2:
                is_recharge_cash.setSelected(false);
                is_recharge_coupon.setSelected(false);
                is_open_commodity.setSelected(false);
                break;
            }
        });
        
        is_recharge_cash.addItemListener(e->i_cash.setEnabled(is_recharge_cash.isSelected()));
        is_recharge_cash.setPreferredSize(new Dimension(100, 0));
        is_recharge_coupon.addItemListener(e->i_coupon.setEnabled(is_recharge_coupon.isSelected()));
        is_recharge_coupon.setPreferredSize(new Dimension(100, 0));
        
        JPanel panel_cash = new JPanel();
        panel_cash.setLayout(new BorderLayout());
        panel_cash.add(is_recharge_cash, BorderLayout.WEST);
        panel_cash.add(i_cash, BorderLayout.CENTER);
        
        JPanel panel_coupon = new JPanel();
        panel_coupon.setLayout(new BorderLayout());
        panel_coupon.add(is_recharge_coupon, BorderLayout.WEST);
        panel_coupon.add(i_coupon, BorderLayout.CENTER);
        
        c_user.setDefaultTips("用户名");
        c_phone.setDefaultTips("电话号码");
        c_name.setDefaultTips("姓名");
        i_cash.setDefaultTips("现金");
        i_coupon.setDefaultTips("优惠券");
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 1));
        panel.add(i_channel);
        panel.add(c_user);
        panel.add(c_phone);
        panel.add(c_name);
        panel.add(panel_cash);
        panel.add(panel_coupon);
        panel.add(is_open_commodity);
        
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建用户", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == c_user.getText().length()) {
                JOptionPane.showMessageDialog(null, "用户名一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            JSONObject args = new JSONObject();
            args.put("channel", i_channel.getSelectedIndex());
            if (0 != c_user.getText().length())     args.put("user",    c_user.getText());
            if (0 != c_phone.getText().length())    args.put("phone",   c_phone.getText());
            if (0 != c_name.getText().length())     args.put("name",    c_name.getText());
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args);
            CommonService.updateChannelAccount();
            CommonService.updatePlatformAccount();
            CommonService.updatePlatformAccountMap();
            
            if (!CommonService.isResponseSuccess(rsp)){
                showServerResponse(rsp);
                break;
            }
            
            caid = Integer.parseInt(CommonService.getResponseDesc(rsp), 16);
            BeanChannelAccount user = CommonService.getChannelAccountByCaid(caid);
            
            if (is_recharge_cash.isSelected()) {
                args.clear();
                args.put("paid", CommonService.getPlatformAccountByCaid(user.i_caid));
                args.put("remark", "【客服充值】创建新用户");
                args.put("type", CommonService.MONEY_CASH);
                args.put("money", Float.parseFloat(i_cash.getText()));
                rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, args);
                if (!CommonService.isResponseSuccess(rsp)){
                    showServerResponse(rsp);
                    break;
                }
            }
            
            if (is_recharge_cash.isSelected()) {
                args.clear();
                args.put("paid", CommonService.getPlatformAccountByCaid(user.i_caid));
                args.put("remark", "【客服充券】创建新用户");
                args.put("type", CommonService.MONEY_COUPON);
                args.put("money", Float.parseFloat(i_coupon.getText()));
                rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, args);
                if (!CommonService.isResponseSuccess(rsp)){
                    showServerResponse(rsp);
                    break;
                }
            }
            
            if(is_open_commodity.isSelected()) UIToolkit.openCommodity(user.i_caid);
            
            break;
        }
        return caid;
    }
    
    public static boolean skip_wa = false;
    
    public static void openCommodity(int caid) {
        int oid = -1;
        // 查找可用订单
        for (BeanOrder order : CommonService.getOrderByCaid(caid)) {
            if (!order.isClose()) {
                oid = order.i_oid;
                break;
            }
        }
        if (-1 == oid) { // 没有找到可用订单
            JSONObject args = new JSONObject();
            args.put("caid",        caid);
            args.put("platform",    CommonService.CHANNEL_TAOBAO);
            args.put("open",        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_ORDER, args);
            CommonService.updateOrder();
            if (!CommonService.isResponseSuccess(rsp)){
                showServerResponse(rsp);
                return;
            }
            
            oid = Integer.parseInt(CommonService.getResponseDesc(rsp), 16);
        }
        
        Wrapper<BeanGameAccount> account = new Wrapper<BeanGameAccount>();
        JLabel              c_arg0      = new JLabel();
        c_arg0.setPreferredSize(new Dimension(300, 0));
        JButton             choose      = new JButton("选择游戏账号");
        choose.setMargin(new Insets(0, 0, 0, 0));
        JComboBox<String>   c_arg1      = new JComboBox<String>();
        c_arg1.setEnabled(false);
        c_arg1.setPreferredSize(new Dimension(c_arg1.getPreferredSize().width, choose.getPreferredSize().height));
        FjTextField         i_price     = new FjTextField();
        i_price.setDefaultTips("(单价)");
        FjTextField         c_remark    = new FjTextField();
        c_remark.setDefaultTips("(备注)");
        JCheckBox           recharge    = new JCheckBox("同时充值", true);
        recharge.setToolTipText("创建商品的同时，将指定金额充值到用户账户");
        FjTextField         i_recharge  = new FjTextField();
        i_recharge.setDefaultTips("(充值金额)");
        recharge.addActionListener(e->i_recharge.setEnabled(recharge.isSelected()));
        
        c_arg1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ItemEvent.SELECTED != e.getStateChange()) return;
                
                if (c_arg1.getSelectedItem().toString().contains("A")) {
                    i_price.setText(CommonService.getGameRentPriceByGaid(Integer.parseInt(c_arg0.getText().split(" ")[0].split("x")[1], 16), CommonService.RENT_TYPE_A) + "");
                } else if (c_arg1.getSelectedItem().toString().contains("B")) {
                    i_price.setText(CommonService.getGameRentPriceByGaid(Integer.parseInt(c_arg0.getText().split(" ")[0].split("x")[1], 16), CommonService.RENT_TYPE_B) + "");
                } else {
                    i_price.setText("0.00");
                }
                i_recharge.setText(i_price.getText());
            }
        });
        
        choose.addActionListener(e->{
            while (null != (account.obj = chooseGameAccount())) {
                c_arg0.setText(String.format("0x%08X - %s (%s)", account.obj.i_gaid, account.obj.c_user,
                        CommonService.getGameByGaid(account.obj.i_gaid).stream().map(game->game.c_name_zh).collect(Collectors.joining("; "))));
                ((DefaultComboBoxModel<String>) c_arg1.getModel()).removeAllElements();
                if (CommonService.RENT_STATE_IDLE == CommonService.getGameAccountRentStateByGaid(account.obj.i_gaid, CommonService.RENT_TYPE_A))
                    ((DefaultComboBoxModel<String>) c_arg1.getModel()).addElement("A类");
                if (CommonService.RENT_STATE_IDLE == CommonService.getGameAccountRentStateByGaid(account.obj.i_gaid, CommonService.RENT_TYPE_B))
                    ((DefaultComboBoxModel<String>) c_arg1.getModel()).addElement("B类");
                if (0 == c_arg1.getModel().getSize()) {
                    c_arg0.setText("");
                    c_arg1.setEnabled(false);
                    JOptionPane.showMessageDialog(null, "选定账号已没有可租类型，请重新选择", "错误", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                c_arg1.setEnabled(true);
                break;
            }
            if (null == account.obj) {
                c_arg0.setText("");
                ((DefaultComboBoxModel<String>) c_arg1.getModel()).removeAllElements();
                c_arg1.setEnabled(false);
                i_price.setText("0.00");
                i_recharge.setText(i_price.getText());
            }
        });
        choose.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorRemoved(AncestorEvent event) {}
            @Override
            public void ancestorMoved(AncestorEvent event) {}
            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (0 == c_arg0.getText().length())
                    for (ActionListener l : choose.getActionListeners()) l.actionPerformed(null);
            }
        });
        
        JPanel panel0 = new JPanel();
        panel0.setLayout(new BorderLayout());
        panel0.add(c_arg0, BorderLayout.CENTER);
        panel0.add(choose, BorderLayout.EAST);
        
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.add(recharge, BorderLayout.WEST);
        panel1.add(i_recharge, BorderLayout.CENTER);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));
        panel.add(panel0);
        panel.add(c_arg1);
        panel.add(i_price);
        panel.add(c_remark);
        panel.add(panel1);
        
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建商品", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == c_arg0.getText().length()) {
                JOptionPane.showMessageDialog(null, "必须要选择游戏账号", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (0 == c_arg1.getModel().getSize()) {
                JOptionPane.showMessageDialog(null, "必须要选择租赁类型", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
//            if (c_arg1.getSelectedItem().toString().contains("B")
//                    && CommonService.RENT_STATE_IDLE == CommonService.getRentStateByGaid(account.obj.i_gaid, CommonService.RENT_TYPE_A)) {
//                JOptionPane.showMessageDialog(null, "B类账号起租要求A类已租，请先将此账号A类出租，然后再出租B类", "错误", JOptionPane.ERROR_MESSAGE);
//                continue;
//            }
            if (!doOpenCommodity(
                    oid,
                    account.obj,
                    c_arg1.getSelectedItem().toString().contains("A") ? "A" : "B",
                    c_remark.getText(),
                    Float.parseFloat(i_price.getText()),
                    recharge.isSelected(),
                    Float.parseFloat(i_recharge.getText()))) continue;
            
            break;
        }
    }
    
    private static boolean doOpenCommodity(int oid, BeanGameAccount account, String type, String remark, float price, boolean isRecharge, float recharge) {
        StepStepDialog ssd = new StepStepDialog(new String[] {
                "验证账号",
                "更新数据",
        });
        Wrapper<Boolean> isSuccess = new Wrapper<Boolean>();
        isSuccess.obj = false;
        doLater(()->{
            JSONObject args = new JSONObject();
            // 1
            {
                ssd.appendText("正在登录到PlayStation网站验证账号密码及其绑定状态...");
                if (skip_wa) ssd.appendText("设定跳过");
                else {
                    args.clear();
                    args.put("user", account.c_user);
                    args.put("pass", account.c_pass_curr);
                    FjDscpMessage rsp = CommonService.send("wa", CommonDefinition.ISIS.INST_ECOM_APPLY_GAME_ACCOUNT_VERIFY, args);
                    ssd.appendText(rsp.toString());
                    if (!CommonService.isResponseSuccess(rsp)) {
                        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null, "账号验证失败，错误原因：\"" + CommonService.getResponseDesc(rsp) + "\"，仍要继续吗？", "错误", JOptionPane.YES_NO_OPTION)) {
                            ssd.dispose();
                            return;
                        }
                    }
                    switch (type) {
                    case "A":
                        if (rsp.toString().contains(" binded")) {
                            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null, "起租A类账号要求未绑定，但此账号当前已被绑定，仍要继续吗？", "错误", JOptionPane.YES_NO_OPTION)) {
                                ssd.dispose();
                                return;
                            }
                        }
                        break;
                    case "B":
                        if (rsp.toString().contains(" unbinded")) {
                            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null, "起租B类账号要求先绑定，但此账号当前尚未绑定，仍要继续吗？", "错误", JOptionPane.YES_NO_OPTION)) {
                                ssd.dispose();
                                return;
                            }
                        }
                        break;
                    }
                    ssd.appendText("验证通过");
                }
            }
            // 2
            ssd.toNextStep();
            int csn = -1;
            {
                ssd.appendText("正在将租赁数据提交到数据库中...");
                args.clear();
                args.put("oid", oid);
                if (0 < remark.length()) args.put("remark", remark);
                args.put("price", price);
                args.put("count", 1);
                args.put("begin", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                args.put("arg0", Integer.toHexString(account.i_gaid));
                args.put("arg1", type);
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_COMMODITY, args);
                ssd.appendText(rsp.toString());
                
                if (CommonService.isResponseSuccess(rsp)) {
                    csn = Integer.parseInt(CommonService.getResponseDesc(rsp), 16);
                    
                    if (isRecharge) {
                        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByOid(oid));
                        args.clear();
                        args.put("paid",    puser.i_paid);
                        args.put("remark",  "【起租充值】起租账号：" + account.c_user);
                        args.put("type",    CommonService.MONEY_CASH);
                        args.put("money",   recharge);
                        rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, args);
                        ssd.appendText(rsp.toString());
                    }
                    isSuccess.obj = true;
                    CommonService.updateOrder();
                    CommonService.updateGameAccountRent();
                    CommonService.updatePlatformAccount();
                    JOptionPane.showMessageDialog(null, "起租成功", "信息", JOptionPane.PLAIN_MESSAGE);
                    ssd.appendText("提交完成");
                }
            }
            ssd.dispose();
            if (-1 != csn) new OCRDialog(CommonService.getOrderByOid(oid).commodities.get(csn)).setVisible(true);
        });
        ssd.setVisible(true);
        return isSuccess.obj;
    }
    
    public static void closeCommodity(int oid, int csn) {
        if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(null, "确认退租此商品？", "提示", JOptionPane.YES_NO_OPTION))
            return;
        
        StepStepDialog ssd = new StepStepDialog(new String[] {
                "验证账号",
                "重设密码",
                "更新数据",
        });
        BeanCommodity commodity = CommonService.getOrderByOid(oid).commodities.get(csn);
        BeanGameAccount account = CommonService.getGameAccountByGaid(Integer.parseInt(commodity.c_arg0, 16));
        doLater(()->{
            JSONObject args = new JSONObject();
            // 1
            {
                ssd.appendText("正在登录到PlayStation网站验证账号密码及其绑定状态");
                if (skip_wa) ssd.appendText("设定跳过");
                else {
                    args.clear();
                    args.put("user", account.c_user);
                    args.put("pass", account.c_pass_curr);
                    FjDscpMessage rsp = CommonService.send("wa", CommonDefinition.ISIS.INST_ECOM_APPLY_GAME_ACCOUNT_VERIFY, args);
                    ssd.appendText(rsp.toString());
                    if (!CommonService.isResponseSuccess(rsp)) {
                        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null, "账号验证失败，错误原因：\"" + CommonService.getResponseDesc(rsp) + "\"，仍要继续吗？", "错误", JOptionPane.YES_NO_OPTION)) {
                            ssd.dispose();
                            return;
                        }
                    }
                    switch (commodity.c_arg1) {
                    case "A":
                        if (rsp.toString().contains(" binded")) {
                            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null, "退租A类账号要求解绑，但此账号当前尚未解绑，仍要继续吗？", "错误", JOptionPane.YES_NO_OPTION)) {
                                ssd.dispose();
                                return;
                            }
                        }
                        break;
                    case "B":
                        ssd.appendText("退租B类账号没有绑定要求");
                        break;
                    }
                    ssd.appendText("验证通过");
                }
            }
            // 2
            ssd.toNextStep();
            {
                ssd.appendText("正在生成新密码...");
                if (skip_wa) ssd.appendText("设定跳过");
                else {
                    String pass_new = CommonService.createGameAccountPassword();
                    boolean isModify = true;
                    switch (commodity.c_arg1) {
                    case "A":
                        if (CommonService.RENT_STATE_IDLE != CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_B)) {
                            BeanChannelAccount user_b = CommonService.getChannelAccountByCaid(CommonService.getChannelAccountByGaid(account.i_gaid, CommonService.RENT_TYPE_B));
                            JOptionPane.showMessageDialog(null,
                                    String.format("退租A类账号时需要修改密码，由于此账号B类正在出租，请现在将新密码(%s)通知给B租用户(%s)，之后点击“确定”继续", pass_new, user_b.getDisplayName()),
                                    "信息",
                                    JOptionPane.PLAIN_MESSAGE);
                        }
                        break;
                    case "B":
                        if (CommonService.RENT_STATE_IDLE != CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_A)) {
                            ssd.appendText("退租B类账号时，由于此账号A类正在出租，将跳过密码修改");
                            isModify = false;
                        }
                        break;
                    }
                    if (isModify) {
                        ssd.appendText("正在登录到PlayStation网站重设密码...");
                        args.clear();
                        args.put("user",     account.c_user);
                        args.put("pass",     account.c_pass_curr);
                        args.put("pass_new", pass_new);
                        FjDscpMessage rsp = CommonService.send("wa", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                        ssd.appendText(rsp.toString());
                        if (!CommonService.isResponseSuccess(rsp)) {
                            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null, "密码重设失败，错误原因：\"" + CommonService.getResponseDesc(rsp) + "\"，仍要继续吗？", "错误", JOptionPane.YES_NO_OPTION)) {
                                ssd.dispose();
                                return;
                            }
                        }
                        ssd.appendText("重设成功");
                        ssd.appendText("正在将新密码提交到数据库中...");
                        args.clear();
                        args.put("gaid", account.i_gaid);
                        args.put("pass_curr", pass_new);
                        rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                        ssd.appendText(rsp.toString());
                        ssd.appendText("提交成功，新密码：\"" + pass_new + "\"");
                    } else {
                        ssd.appendText("密码重设已跳过");
                    }
                }
            }
            // 3
            ssd.toNextStep();
            {
                ssd.appendText("正在将退租信息提交到数据库中，并处理结算...");
                args.clear();
                args.put("oid", oid);
                args.put("csn", csn);
                args.put("end", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_COMMODITY, args);
                ssd.appendText(rsp.toString());
                ssd.appendText("提交完成");
            }
            CommonService.updateOrder();
            CommonService.updateGameAccount();
            CommonService.updateGameAccountRent();
            CommonService.updatePlatformAccount();
            JOptionPane.showMessageDialog(null, "退租成功", "信息", JOptionPane.PLAIN_MESSAGE);
            ssd.dispose();
            new OCRDialog(CommonService.getOrderByOid(oid).commodities.get(commodity.i_csn)).setVisible(true);
        });
        ssd.setVisible(true);
    }
    
    public static void createTicket() {
        createTicket(null);
    }
    
    public static void createTicket(BeanChannelAccount account) {
        JLabel  i_caid  = new JLabel(null != account ? String.format("0x%08X - %s", account.i_caid, account.getDisplayName()) : "");
        i_caid.setPreferredSize(new Dimension(200, i_caid.getPreferredSize().height));
        JButton choose  = new JButton("选择发起用户");
        choose.setMargin(new Insets(0, 0, 0, 0));
        JPanel  caid    = new JPanel();
        caid.setLayout(new BorderLayout());
        caid.add(i_caid, BorderLayout.CENTER);
        caid.add(choose, BorderLayout.EAST);
        choose.addActionListener(e->{
            BeanChannelAccount account1 = chooseChannelAccount();
            if (null == account1) i_caid.setText("");
            else i_caid.setText(String.format("0x%08X - %s", account1.i_caid, account1.getDisplayName()));
        });
        
        JComboBox<String>   i_type      = new JComboBox<String>(new String[] {"退款申请", "意见建议", "备忘事项"}); // ordered
        FjTextField         c_title     = new FjTextField();
        c_title.setDefaultTips("工单标题");
        FjTextArea          c_content   = new FjTextArea();
        c_content.setDefaultTips("工单内容");
        c_content.setColumns(40);
        c_content.setRows(12);
        
        JPanel panel0 = new JPanel();
        panel0.setLayout(new GridLayout(3, 1));
        panel0.add(caid);
        panel0.add(i_type);
        panel0.add(c_title);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(panel0, BorderLayout.NORTH);
        panel.add(new JScrollPane(c_content), BorderLayout.CENTER);
        
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建工单", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == i_caid.getText().length()) {
                JOptionPane.showMessageDialog(null, "用户一定要选择", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (0 == c_title.getText().length()) {
                JOptionPane.showMessageDialog(null, "标题一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            JSONObject args = new JSONObject();
            args.put("caid", Integer.parseInt(i_caid.getText().split(" ")[0].split("x")[1], 16));
            args.put("type", i_type.getSelectedIndex());
            args.put("title", c_title.getText().replace("'", "^").replace("\"", "^").replace("\n", ""));
            args.put("content", c_content.getText().replace("'", "^").replace("\"", "^").replace("\n", ""));
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET, args);
            CommonService.updateTicket();
            UIToolkit.showServerResponse(rsp);
            break;
        }
    }
    
    public static BeanGame chooseGame() {
        FjListPane<String> pane = new FjListPane<String>();
        // 启用搜索框
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTips("键入关键词搜索");
        pane.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<String>(pane.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, String celldata) {
                int count = 0;
                for (String word : words) if (celldata.toLowerCase().contains(word.toLowerCase())) count++;
                return count == words.length;
            }
        });
        
        // 创建弹框
        JDialog dialog = new JDialog();
        dialog.setTitle("选择游戏");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 500);
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((owner.width - dialog.getWidth()) / 2, (owner.height - dialog.getHeight()) / 2);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.add(pane, BorderLayout.CENTER);
        
        Wrapper<BeanGame> wrapper = new Wrapper<BeanGame>();
        
        // 添加游戏列表
        CommonService.getGameAll().values().forEach(game->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", game.i_gid, game.c_name_zh));
            cell.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wrapper.obj = CommonService.getGameByGid(game.i_gid);
                    dialog.dispose();
                }
            });
            pane.getList().addCell(cell);
        });
        
        dialog.setVisible(true);
        
        return wrapper.obj;
    }
    
    public static BeanGameAccount chooseGameAccount() {
        JCheckBox type_enable   = new JCheckBox("启用状态过滤", false);
        JCheckBox type_a   = new JCheckBox("A:〇", true);
        type_a.setEnabled(false);
        JCheckBox type_b   = new JCheckBox("B:〇", true);
        type_b.setEnabled(false);
        
        FjListPane<String> pane = new FjListPane<String>();
        // 启用搜索框
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTypes(new String[] {"按游戏名", "按用户名", "按账号名"});
        pane.getSearchBar().setSearchTips("键入关键词搜索");
        pane.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<String>(pane.getList()) {
            private boolean isMatchCheckBox(JCheckBox type_a, JCheckBox type_b, String celldata) {
                if (!type_enable.isSelected()) return true;
                return celldata.contains(type_a.getText()) && celldata.contains(type_b.getText());
            }
            @Override
            public boolean isMatch(String type, String[] words, String celldata) {
                switch(type) {
                case "按游戏名": {
                    List<BeanGame> games = CommonService.getGameAll().values()
                            .stream()
                            .filter(game->{
                                int count = 0;
                                for (String word : words) if (game.c_name_zh.toLowerCase().contains(word.toLowerCase())) count++;
                                if (count == words.length) return true;
                                else return false;
                            }).collect(Collectors.toList());
                    List<BeanGameAccount> accounts = CommonService.getGameAccountGameAll()
                            .stream()
                            .filter(gag->{
                                for (BeanGame game : games) {
                                    if (game.i_gid == gag.i_gid) return true;
                                }
                                return false;
                            })
                            .map(gag->CommonService.getGameAccountByGaid(gag.i_gaid))
                            .collect(Collectors.toList());
                    for (BeanGameAccount account : accounts) {
                        if (null == account) continue;
                        if (!isMatchCheckBox(type_a, type_b, celldata)) continue;
                        if (account.i_gaid == Integer.parseInt(celldata.split(" ")[0].split("x")[1], 16)) return true;
                    }
                    return false;
                }
                case "按用户名": {
                    List<BeanChannelAccount> users = CommonService.getChannelAccountAll().values()
                            .stream()
                            .filter(user->{
                                int count = 0;
                                for (String word : words) if (user.getDisplayName().toLowerCase().contains(word.toLowerCase())) count++;
                                if (count == words.length) return true;
                                else return false;
                            }).collect(Collectors.toList());
                    List<BeanGameAccount> accounts = CommonService.getGameAccountRentAll()
                            .stream()
                            .filter(rent->{
                                if (CommonService.RENT_STATE_RENT != rent.i_state) return false;
                                for (BeanChannelAccount user : users) {
                                    if (user.i_caid == rent.i_caid) return true;
                                }
                                return false;
                            })
                            .map(rent->CommonService.getGameAccountByGaid(rent.i_gaid))
                            .collect(Collectors.toList());
                    for (BeanGameAccount account : accounts) {
                        if (null == account) continue;
                        if (!isMatchCheckBox(type_a, type_b, celldata)) continue;
                        if (account.i_gaid == Integer.parseInt(celldata.split(" ")[0].split("x")[1], 16)) return true;
                    }
                    return false;
                }
                case "按账号名": {
                    int count = 0;
                    for (String word : words) if (celldata.toLowerCase().contains(word.toLowerCase())) count++;
                    return count == words.length &&  isMatchCheckBox(type_a, type_b, celldata);
                }
                default:
                    return true;
                }
            }
        });

        type_enable.addActionListener(e->{
            type_a.setEnabled(type_enable.isSelected());
            type_b.setEnabled(type_enable.isSelected());
            pane.getSearchBar().doSearch();
        });
        type_a.addActionListener(e->{
            if (type_a.isSelected()) type_a.setText("A:〇");
            else type_a.setText("A:●");
            pane.getSearchBar().doSearch();
        });
        type_b.addActionListener(e->{
            if (type_b.isSelected()) type_b.setText("B:〇");
            else type_b.setText("B:●");
            pane.getSearchBar().doSearch();
        });
        
        JPanel types = new JPanel();
        types.setBorder(BorderFactory.createTitledBorder("过滤条件"));
        types.setLayout(new GridLayout(2, 2));
        types.add(type_enable);
        types.add(new JPanel());
        types.add(type_a);
        types.add(type_b);
        
        // 创建弹框
        JDialog dialog = new JDialog();
        dialog.setTitle("选择游戏账号(〇:空闲;●:占用)");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 500);
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((owner.width - dialog.getWidth()) / 2, (owner.height - dialog.getHeight()) / 2);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(types, BorderLayout.NORTH);
        dialog.getContentPane().add(pane, BorderLayout.CENTER);
        
        Wrapper<BeanGameAccount> wrapper = new Wrapper<BeanGameAccount>();
        CommonService.updateGameAccountRent();
        CommonService.getGameAccountAll().values().forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", account.i_gaid, account.c_user),
                    ( (CommonService.RENT_STATE_IDLE == CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_A) ? "[A:〇]" : "[A:●]")
                    + (CommonService.RENT_STATE_IDLE == CommonService.getGameAccountRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_B) ? "[B:〇]" : "[B:●]")));
            JLabel games = new JLabel(CommonService.getGameByGaid(account.i_gaid).stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")));
            games.setPreferredSize(new Dimension(1, games.getPreferredSize().height));
            games.setForeground(Color.gray);
            cell.add(games, BorderLayout.SOUTH);
            cell.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wrapper.obj = CommonService.getGameAccountByGaid(account.i_gaid);
                    dialog.dispose();
                }
            });
            pane.getList().addCell(cell);
        });
        
        pane.getSearchBar().doSearch();
        
        dialog.setVisible(true);
        
        return wrapper.obj;
    }
    
    public static BeanChannelAccount chooseChannelAccount() {
        FjListPane<String> pane = new FjListPane<String>();
        // 启用搜索框
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTips("键入关键词搜索");
        pane.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<String>(pane.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, String celldata) {
                int count = 0;
                for (String word : words) if (celldata.toLowerCase().contains(word.toLowerCase())) count++;
                return count == words.length;
            }
        });
        
        // 创建弹框
        JDialog dialog = new JDialog();
        dialog.setTitle("选择用户");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 500);
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((owner.width - dialog.getWidth()) / 2, (owner.height - dialog.getHeight()) / 2);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.add(pane, BorderLayout.CENTER);
        
        Wrapper<BeanChannelAccount> wrapper = new Wrapper<BeanChannelAccount>();
        
        // 添加用户列表
        CommonService.updateChannelAccount();
        CommonService.getChannelAccountAll().values().forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - [%s] %s",
                    account.i_caid,
                    CommonService.CHANNEL_TAOBAO == account.i_channel ? "淘  宝" : CommonService.CHANNEL_WECHAT == account.i_channel ? "微  信" : CommonService.CHANNEL_ALIPAY == account.i_channel ? "支付宝" : "未  知",
                    account.getDisplayName()));
            cell.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wrapper.obj = CommonService.getChannelAccountByCaid(account.i_caid);
                    dialog.dispose();
                }
            });
            pane.getList().addCell(cell);
        });
        
        dialog.setVisible(true);
        
        return wrapper.obj;
    }
    
    private static class Wrapper<E> {
        public E obj;
    }
    
    public static void deleteTag(BeanTag tag) {
        if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(null, "确定删除TAG：" + tag.c_tag + "？", "信息", JOptionPane.OK_CANCEL_OPTION))
            return;

        JSONObject args = new JSONObject();
        args.put("type",        tag.i_type);
        args.put("instance",    tag.i_instance);
        args.put("tag",         tag.c_tag);
        FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_TAG_DEL, args);
        showServerResponse(rsp);
    }
    
    public static void userRecharge(int paid) {
        userRecharge(CommonService.MONEY_CASH, paid);
    }
    
    public static void userRecharge(int money_type, int paid) {
        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(paid);
        
        JComboBox<String> type = new JComboBox<String>(new String[] {"充值", "充券", "退款"});
        type.setEditable(false);
        FjTextField remark = new FjTextField();
        remark.setDefaultTips("备注");
        FjTextField money = new FjTextField();
        money.setDefaultTips("金额");
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));
        panel.add(type);
        panel.add(remark);
        panel.add(money);
        type.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                switch (type.getSelectedIndex()) {
                case 0:
                    remark.setText("【人工充值】");
                    money.setText("150.00");
                    break;
                case 1:
                    remark.setText("【人工充券】");
                    money.setText("5.00");
                    break;
                case 2:
                    remark.setText("【人工退款】");
                    money.setText("-" + puser.i_cash);
                    break;
                }
            }
        });
        switch (money_type) {
        case CommonService.MONEY_CASH:
            type.setSelectedIndex(1);
            type.setSelectedIndex(0);
            break;
        case CommonService.MONEY_COUPON:
            type.setSelectedIndex(1);
            break;
        case CommonService.MONEY_CONSUME:
            type.setSelectedIndex(2);
            break;
        }
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "充值", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == money.getText().length()) {
                JOptionPane.showMessageDialog(null, "金额一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            // 退款条件
            if (2 == type.getSelectedIndex()) {
                // 校验订单
                if (0 < CommonService.getChannelAccountByPaid(paid)
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
                    JOptionPane.showMessageDialog(null, "此用户或其关联用户仍有未关闭的订单，不能退款", "错误", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                // 不存在关联的支付宝账户
                if (0 == CommonService.getChannelAccountByPaid(paid)
                        .stream()
                        .filter(user->user.i_channel == CommonService.CHANNEL_ALIPAY)
                        .count()) {
                    JOptionPane.showMessageDialog(null, "没有找到关联的支付宝账户，无法执行退款，点击“确定”后将指定和关联支付宝账户。新创建用户时平台请选择“支付宝”", "信息", JOptionPane.PLAIN_MESSAGE);
                    int caid = -1;
                    while (-1 != (caid = userBind(paid))) {
                        List<BeanChannelAccount> users = CommonService.getChannelAccountRelatedByCaidNChannel(caid, CommonService.CHANNEL_ALIPAY);
                        if (users.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "仍然没有找到关联的支付宝账户，可能刚才关联的用户的平台类型不是“支付宝”，请重新关联", "错误", JOptionPane.ERROR_MESSAGE);
                            continue;
                        }
                        BeanChannelAccount user_alipay = users.get(0);
                        JSONObject args = new JSONObject();
                        args.put("caid",    user_alipay.i_caid);
                        args.put("type",    CommonService.TICKET_TYPE_REFUND);
                        args.put("title",   "【人工】【退款】");
                        args.put("content", String.format("支付宝账号: %s | 真实姓名: %s | 退款金额: %.2f 元 | 退款备注: %s",
                                user_alipay.c_user,
                                user_alipay.c_name,
                                Float.parseFloat(money.getText()),
                                "VC电玩游戏退款"));
                        CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET, args);
                        CommonService.updateTicket();
                        break;
                    }
                }
            }
            JSONObject args = new JSONObject();
            args.put("paid",    puser.i_paid);
            if (0 < remark.getText().length()) args.put("remark",  remark.getText());
            args.put("type",    0 == type.getSelectedIndex() ? CommonService.MONEY_CASH : 1 == type.getSelectedIndex() ? CommonService.MONEY_COUPON : 2 == type.getSelectedIndex() ? CommonService.MONEY_CASH : -1);
            args.put("money",   Float.parseFloat(money.getText()));
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, args);
            CommonService.updatePlatformAccount();
            CommonService.updatePlatformAccountMoney();
            UIToolkit.showServerResponse(rsp);
            break;
        }
    }
    
    /**
     * 
     * @param paid_to
     * @return caid or -1 if canceled
     */
    public static int userBind(int paid_to) {
        BeanChannelAccount user2 = null;
        int option = JOptionPane.CLOSED_OPTION;
        while (JOptionPane.CLOSED_OPTION != (option = JOptionPane.showOptionDialog(null, "请选择关联途径", "提示", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"创建新用户", "选择现有用户"}, "选择现有用户"))) {
            switch (option) {
            case JOptionPane.YES_OPTION:
                int caid = UIToolkit.createChannelAccount();
                if (-1 == caid) continue; // no choose
                user2 = CommonService.getChannelAccountByCaid(caid);
                break;
            case JOptionPane.NO_OPTION:
                user2 = UIToolkit.chooseChannelAccount();
                break;
            }
            if (null == user2) continue;
            
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, "关联之后将无法回退，继续？", "提示", JOptionPane.OK_CANCEL_OPTION))
                continue;
            
            int paid_from = CommonService.getPlatformAccountByCaid(user2.i_caid);
            if (paid_to == paid_from) {
                JOptionPane.showMessageDialog(null, "即将关联的两个账户已经属于同一个平台账户了", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            JSONObject args = new JSONObject();
            args.put("paid_to", paid_to);
            args.put("paid_from", paid_from);
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, args);
            CommonService.updatePlatformAccount();
            CommonService.updatePlatformAccountMap();
            
            UIToolkit.showServerResponse(rsp);
            break;
        }
        if (null != user2) return user2.i_caid;
        else return -1;
    }
    
    public static boolean showServerResponse(FjDscpMessage rsp) {
        if (CommonService.isResponseSuccess(rsp)) {
            JDialog dialog = new JOptionPane("操作成功", JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new String[0]).createDialog(null, "提示");
            doLater(()->{
                try {Thread.sleep(600L);}
                catch (Exception e) {e.printStackTrace();}
                dialog.dispose();
            });
            dialog.setVisible(true);
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "服务器响应：" + CommonService.getResponseDesc(rsp), "错误", JOptionPane.PLAIN_MESSAGE);
            return false;
        }
    }
}