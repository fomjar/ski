package com.ski.omc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
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
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.fomjar.widget.FjTextField;
import com.ski.common.CommonService;
import com.ski.common.CommonDefinition;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanCommodity;
import com.ski.common.bean.BeanGame;
import com.ski.common.bean.BeanGameAccount;
import com.ski.common.bean.BeanOrder;
import com.ski.common.bean.BeanPlatformAccount;
import com.ski.omc.comp.ManageGameAccount;
import com.ski.omc.comp.ManageOrder;
import com.ski.omc.comp.StepStepDialog;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;
import sun.awt.image.ToolkitImage;

public class UIToolkit {
    
    public static final Font FONT = new Font("楷体", Font.PLAIN, 14);

    public static final Color COLOR_MODIFYING = Color.blue;
    
    static {
        UIManager.getLookAndFeelDefaults().put("Label.font",        FONT);
        UIManager.getLookAndFeelDefaults().put("Table.font",        FONT);
        UIManager.getLookAndFeelDefaults().put("TableHeader.font",  FONT);
        UIManager.getLookAndFeelDefaults().put("TextField.font",    FONT);
        UIManager.getLookAndFeelDefaults().put("TextArea.font",     FONT);
        UIManager.getLookAndFeelDefaults().put("TitledBorder.font", FONT);
        UIManager.getLookAndFeelDefaults().put("CheckBox.font",     FONT);
        UIManager.getLookAndFeelDefaults().put("RadioButton.font",  FONT);
        UIManager.getLookAndFeelDefaults().put("ComboBox.font",     FONT);
        UIManager.getLookAndFeelDefaults().put("Button.font",       FONT);
        UIManager.getLookAndFeelDefaults().put("Panel.font",        FONT);
        UIManager.getLookAndFeelDefaults().put("FilePane.font",     FONT);
        UIManager.getLookAndFeelDefaults().put("Menu.font",         FONT);
        UIManager.getLookAndFeelDefaults().put("MenuItem.font",     FONT);
        
        // UIManager.getLookAndFeelDefaults().forEach((key, value)->{System.out.println(key + "=" + value);});
    }
    
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    
    public static void doLater(Runnable task) {pool.submit(task);}
    
    public static Image loadImage(String path, double zoom) {
        ToolkitImage  img0 = (ToolkitImage) Toolkit.getDefaultToolkit().getImage(UIToolkit.class.getResource(path));
        BufferedImage img  = new BufferedImage((int) (img0.getWidth() * zoom), (int) (img0.getHeight() * zoom), BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(img0, 0, 0, img.getWidth(), img.getHeight(), null);
        return img;
    }
    
    public static JPanel createBasicInfoLabel(String label, JComponent field) {
        return createBasicInfoLabel(label, field, null, null);
    }
    
    public static JPanel createBasicInfoLabel(String label, JComponent field, String actionName, ActionListener action) {
        JLabel jlabel = new JLabel(label);
        jlabel.setPreferredSize(new Dimension(160, 0));
        jlabel.setFont(field.getFont());
        jlabel.setForeground(field.getForeground());
        
        JPanel jpanel = new JPanel();
        jpanel.setLayout(new BorderLayout());
        jpanel.add(jlabel, BorderLayout.WEST);
        jpanel.add(field, BorderLayout.CENTER);
        
        if (null != actionName) {
            JButton jbutton = new JButton(actionName);
            jbutton.setMargin(new Insets(0, 0, 0, 0));
            jbutton.setPreferredSize(new Dimension(jbutton.getPreferredSize().width, field.getPreferredSize().height));
            jbutton.addActionListener(action);
            jpanel.add(jbutton, BorderLayout.EAST);
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
            UIToolkit.showServerResponse(rsp);
            
            if (CommonService.isResponseSuccess(rsp)) {
                List<BeanGameAccount> accounts = CommonService.getGameAccountByUserName(c_user.getText());
                if (1 == accounts.size()) new ManageGameAccount(accounts.get(0).i_gaid).setVisible(true);
            }
            break;
        }
    }
    
    public static void createChannelAccount() {
        JComboBox<String>   i_channel = new JComboBox<String>(new String[] {"淘  宝", "微  信", "支付宝"}); // ordered
        i_channel.setEditable(false);
        FjTextField c_user  = new FjTextField();
        FjTextField c_phone = new FjTextField();
        FjTextField c_nick  = new FjTextField();
        c_user.setDefaultTips("用户名");
        c_phone.setDefaultTips("电话号码");
        c_nick.setDefaultTips("姓名");
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));
        panel.add(i_channel);
        panel.add(c_user);
        panel.add(c_phone);
        panel.add(c_nick);
        
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建用户", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == c_user.getText().length()) {
                JOptionPane.showMessageDialog(null, "用户名一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            JSONObject args = new JSONObject();
            args.put("channel", i_channel.getSelectedIndex());
            if (0 != c_user.getText().length())     args.put("user",    c_user.getText());
            if (0 != c_phone.getText().length())    args.put("phone",   c_phone.getText());
            if (0 != c_nick.getText().length())     args.put("nick",   c_nick.getText());
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args);
            UIToolkit.showServerResponse(rsp);
            
            if (CommonService.isResponseSuccess(rsp)) {
                if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "现在创建订单？", "提示", JOptionPane.YES_NO_OPTION)) {
                    CommonService.updateChannelAccount();
                    List<BeanChannelAccount> users = CommonService.getChannelAccountByUser(c_user.getText());
                    if (1 == users.size()) UIToolkit.createOrder(users.get(0));
                    else UIToolkit.createOrder();
                }
            }
            break;
        }
    }
    
    public static void createOrder() {
        createOrder(null);
    }
    
    public static void createOrder(BeanChannelAccount user) {
        JComboBox<String>   i_platform = new JComboBox<String>(new String[] {"0 - 淘宝", "1 - 微信"});
        JLabel i_caid_label = new JLabel(null != user ? String.format("0x%08X - %s", user.i_caid, user.c_user) : "");
        i_caid_label.setPreferredSize(new Dimension(240, 0));
        JButton i_caid_button = new JButton("选择用户");
        i_caid_button.setMargin(new Insets(0, 0, 0, 0));
        i_caid_button.addActionListener(e->{
            BeanChannelAccount account = chooseChannelAccount();
            if (null == account) i_caid_label.setText("");
            else i_caid_label.setText(String.format("0x%08X - %s", account.i_caid, account.c_user));
        });
        if (null == user) {
            // auto choose channel account
            i_caid_button.addAncestorListener(new AncestorListener() {
                @Override
                public void ancestorRemoved(AncestorEvent event) {}
                @Override
                public void ancestorMoved(AncestorEvent event) {}
                @Override
                public void ancestorAdded(AncestorEvent event) {
                    for (ActionListener l : i_caid_button.getActionListeners()) l.actionPerformed(null);
                }
            });
        }
        
        JPanel i_caid = new JPanel();
        i_caid.setLayout(new BorderLayout());
        i_caid.add(i_caid_label, BorderLayout.CENTER);
        i_caid.add(i_caid_button, BorderLayout.EAST);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        panel.add(i_platform);
        panel.add(i_caid);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建订单", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == i_caid_label.getText().length()) {
                JOptionPane.showMessageDialog(null, "用户名一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            JSONObject args = new JSONObject();
            args.put("platform", Integer.parseInt(i_platform.getSelectedItem().toString().split(" ")[0]));
            args.put("caid", Integer.parseInt(i_caid_label.getText().split(" ")[0].split("x")[1], 16));
            args.put("open", sdf.format(new Date(System.currentTimeMillis())));
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_ORDER, args);
            UIToolkit.showServerResponse(rsp);
            
            if (CommonService.isResponseSuccess(rsp)) {
                if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "现在创建商品？", "提示", JOptionPane.YES_NO_OPTION)) {
                    CommonService.updateOrder();
                    List<BeanOrder> orders = CommonService.getOrderByCaid(args.getInt("caid")).stream().filter(order->!order.isClose()).collect(Collectors.toList());
                    if (1 == orders.size()) {
                        BeanOrder order = orders.get(0);
                        UIToolkit.openCommodity(order.i_oid);
                        CommonService.updateOrder();
                        new ManageOrder(order.i_oid).setVisible(true);
                    }
                    else JOptionPane.showMessageDialog(null, "不能确认刚才创建的订单，请手工打开再创建商品", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
            break;
        }
    }
    
    public static void openCommodity(int oid) {
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
        JCheckBox           recharge    = new JCheckBox("充值此金额", true);
        recharge.setToolTipText("创建商品的同时，将此单价金额充值到用户账户");
        FjTextField         c_remark    = new FjTextField();
        c_remark.setDefaultTips("(备注)");
        
        c_arg1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ItemEvent.SELECTED != e.getStateChange()) return;
                
                if (c_arg1.getSelectedItem().toString().contains("A"))
                    i_price.setText(CommonService.getRentPriceByGaid(Integer.parseInt(c_arg0.getText().split(" ")[0].split("x")[1], 16), CommonService.RENT_TYPE_A) + "");
                else if (c_arg1.getSelectedItem().toString().contains("B"))
                    i_price.setText(CommonService.getRentPriceByGaid(Integer.parseInt(c_arg0.getText().split(" ")[0].split("x")[1], 16), CommonService.RENT_TYPE_B) + "");
                else
                    i_price.setText("0.00");
            }
        });
        
        choose.addActionListener(e->{
            while (null != (account.obj = chooseGameAccount())) {
                c_arg0.setText(String.format("0x%08X - %s (%s)", account.obj.i_gaid, account.obj.c_user,
                        CommonService.getGameByGaid(account.obj.i_gaid).stream().map(game->game.c_name_zh).collect(Collectors.joining("; "))));
                ((DefaultComboBoxModel<String>) c_arg1.getModel()).removeAllElements();
                if (CommonService.RENT_STATE_IDLE == CommonService.getRentStateByGaid(account.obj.i_gaid, CommonService.RENT_TYPE_A))
                    ((DefaultComboBoxModel<String>) c_arg1.getModel()).addElement("A类");
                if (CommonService.RENT_STATE_IDLE == CommonService.getRentStateByGaid(account.obj.i_gaid, CommonService.RENT_TYPE_B))
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
                i_price.setDefaultTips("0.00");
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
        panel1.add(i_price, BorderLayout.CENTER);
        panel1.add(recharge, BorderLayout.EAST);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));
        panel.add(panel0);
        panel.add(c_arg1);
        panel.add(panel1);
        panel.add(c_remark);
        
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建商品", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == c_arg0.getText().length()) {
                JOptionPane.showMessageDialog(null, "必须要选择游戏账号", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (0 == c_arg1.getModel().getSize()) {
                JOptionPane.showMessageDialog(null, "必须要选择租赁类型", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (c_arg1.getSelectedItem().toString().contains("B")
                    && CommonService.RENT_STATE_IDLE == CommonService.getRentStateByGaid(account.obj.i_gaid, CommonService.RENT_TYPE_A)) {
                JOptionPane.showMessageDialog(null, "B类账号起租要求A类已租，请先将此账号A类出租，然后再出租B类", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!doOpenCommodity(
                    oid,
                    account.obj,
                    c_arg1.getSelectedItem().toString().contains("A") ? "A" : "B",
                    c_remark.getText(),
                    Float.parseFloat(i_price.getText()),
                    recharge.isSelected())) continue;
            
            break;
        }
    }
    
    private static boolean doOpenCommodity(int oid, BeanGameAccount account, String type, String remark, float price, boolean isRecharge) {
        StepStepDialog ssd = new StepStepDialog(new String[] {
                "验证账号",
                "更新数据",
        });
        Wrapper<Boolean> isSuccess = new Wrapper<Boolean>();
        isSuccess.obj = false;
        doLater(()->{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            JSONObject args = new JSONObject();
            // 1
            {
                ssd.appendText("正在登录到PlayStation网站验证账号密码及其绑定状态...");
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
            // 2
            ssd.toNextStep();
            {
                ssd.appendText("正在将租赁数据提交到数据库中...");
                args.clear();
                args.put("oid", oid);
                if (0 < remark.length()) args.put("remark", remark);
                args.put("price", price);
                args.put("count", 1);
                args.put("begin", sdf.format(new Date(System.currentTimeMillis())));
                args.put("arg0", Integer.toHexString(account.i_gaid));
                args.put("arg1", type);
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_COMMODITY, args);
                ssd.appendText(rsp.toString());
                
                if (CommonService.isResponseSuccess(rsp) && isRecharge) {
                    BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByOid(oid));
                    args.clear();
                    args.put("paid",    puser.i_paid);
                    args.put("remark",  "【起租充值】起租账号：" + account.c_user);
                    args.put("type",    CommonService.MONEY_BALANCE);
                    args.put("money",   price);
                    rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, args);
                    ssd.appendText(rsp.toString());
                }
                ssd.appendText("提交完成");
            }
            isSuccess.obj = true;
            JOptionPane.showMessageDialog(null, "起租成功", "信息", JOptionPane.PLAIN_MESSAGE);
            ssd.dispose();
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            JSONObject args = new JSONObject();
            // 1
            {
                ssd.appendText("正在登录到PlayStation网站验证账号密码及其绑定状态");
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
            // 2
            ssd.toNextStep();
            {
                ssd.appendText("正在生成新密码...");
                String pass_new = CommonService.createGameAccountPassword();
                boolean isModify = true;
                switch (commodity.c_arg1) {
                case "A":
                    if (CommonService.RENT_STATE_IDLE != CommonService.getRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_B)) {
                        BeanChannelAccount user_b = CommonService.getChannelAccountByCaid(CommonService.getRentChannelAccountByGaid(account.i_gaid, CommonService.RENT_TYPE_B));
                        JOptionPane.showMessageDialog(null,
                                String.format("退租A类账号时需要修改密码，由于此账号B类正在出租，请现在将新密码(%s)通知给B租用户(%s)，之后点击“确定”继续", pass_new, user_b.c_user),
                                "信息",
                                JOptionPane.PLAIN_MESSAGE);
                    }
                    break;
                case "B":
                    if (CommonService.RENT_STATE_IDLE != CommonService.getRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_A)) {
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
            // 3
            ssd.toNextStep();
            {
                ssd.appendText("正在将退租信息提交到数据库中，并处理结算...");
                args.clear();
                args.put("oid", oid);
                args.put("csn", csn);
                args.put("end", sdf.format(new Date(System.currentTimeMillis())));
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_COMMODITY, args);
                ssd.appendText(rsp.toString());
                ssd.appendText("提交完成");
            }
            JOptionPane.showMessageDialog(null, "退租成功", "信息", JOptionPane.PLAIN_MESSAGE);
            ssd.dispose();
        });
        ssd.setVisible(true);
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
                for (String word : words) if (celldata.contains(word)) count++;
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
                                for (String word : words) if (game.c_name_zh.contains(word)) count++;
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
                                int count1 = 0;
                                for (String word : words) if (user.c_user.contains(word)) count1++;
                                if (count1 == words.length) return true;
                                else return false;
                            }).collect(Collectors.toList());
                    List<BeanGameAccount> accounts = CommonService.getRentGameAccountAll()
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
                    for (String word : words) if (celldata.contains(word)) count++;
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
        dialog.setTitle("选择游戏账号(〇:空闲;●:已租)");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 500);
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((owner.width - dialog.getWidth()) / 2, (owner.height - dialog.getHeight()) / 2);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(types, BorderLayout.NORTH);
        dialog.getContentPane().add(pane, BorderLayout.CENTER);
        
        Wrapper<BeanGameAccount> wrapper = new Wrapper<BeanGameAccount>();
        CommonService.getGameAccountAll().values().forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", account.i_gaid, account.c_user),
                    ( (CommonService.RENT_STATE_IDLE == CommonService.getRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_A) ? "[A:〇]" : "[A:●]")
                    + (CommonService.RENT_STATE_IDLE == CommonService.getRentStateByGaid(account.i_gaid, CommonService.RENT_TYPE_B) ? "[B:〇]" : "[B:●]")));
            JLabel games = new JLabel("包含游戏：" + CommonService.getGameByGaid(account.i_gaid).stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")));
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
                for (String word : words) if (celldata.contains(word)) count++;
                return count == words.length;
            }
        });
        
        // 创建弹框
        JDialog dialog = new JDialog();
        dialog.setTitle("选择用户");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 500);
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((owner.width - dialog.getWidth()) / 2, (owner.height - dialog.getHeight()) / 2);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.add(pane, BorderLayout.CENTER);
        
        Wrapper<BeanChannelAccount> wrapper = new Wrapper<BeanChannelAccount>();
        
        // 添加用户列表
        CommonService.getChannelAccountAll().values().forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - [%s] %s",
                    account.i_caid,
                    CommonService.CHANNEL_TAOBAO == account.i_channel ? "淘宝" : CommonService.CHANNEL_WECHAT == account.i_channel ? "微信" : CommonService.CHANNEL_ALIPAY == account.i_channel ? "支付宝" : "未知",
                    account.c_user));
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
    
    public static void showServerResponse(FjDscpMessage rsp) {
        JOptionPane.showMessageDialog(null, rsp.toString(), "服务器响应", JOptionPane.PLAIN_MESSAGE);
    }
}