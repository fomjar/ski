package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjTextField;
import com.ski.common.SkiCommon;
import com.ski.omc.MainFrame;
import com.ski.omc.Service;
import com.ski.omc.UIToolkit;
import com.ski.omc.bean.BeanChannelAccount;
import com.ski.omc.bean.BeanPlatformAccount;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageChannelAccount extends JDialog {

    private static final long serialVersionUID = -1539252669784510561L;
    private static final String SWITCH_TITLE_OPEN = "当前显示：未关闭订单 (点击切换)";
    private static final String SWITCH_TITLE_ALL  = "当前显示：全部订单 (点击切换)";
    
    private BeanChannelAccount  user;
    private BeanPlatformAccount puser;
    private JToolBar            toolbar;
    private FjEditLabel         i_caid;
    private FjEditLabel         c_user;
    private FjEditLabel         i_channel;
    private FjEditLabel         c_nick;
    private FjEditLabel         i_gender;
    private FjEditLabel         c_phone;
    private FjEditLabel         c_address;
    private FjEditLabel         c_zipcode;
    private FjEditLabel         t_birth;
    private FjEditLabel         i_balance;
    private FjEditLabel         i_coupon;
    private FjEditLabel         ri_balance;
    private FjEditLabel         ri_coupon;
    private FjListPane<String>  pane_user;
    private FjListPane<String>  pane_account;
    private FjListPane<String>  pane_order;
    private JButton             order_switch;
    
    public ManageChannelAccount(int caid) {
        super(MainFrame.getInstance());
        
        user = Service.map_channel_account.get(caid);
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        toolbar.add(new JButton("更新"));
        toolbar.addSeparator();
        toolbar.add(new JButton("充值"));
        toolbar.add(new JButton("充券"));
        toolbar.add(new JButton("创建订单"));
        toolbar.addSeparator();
        toolbar.add(new JButton("关联用户"));
        
        i_caid      = new FjEditLabel(false);
        i_caid.setForeground(Color.gray);
        c_user      = new FjEditLabel(false);
        i_channel   = new FjEditLabel();
        c_nick      = new FjEditLabel();
        i_gender    = new FjEditLabel();
        c_phone     = new FjEditLabel();
        c_address   = new FjEditLabel();
        c_zipcode   = new FjEditLabel();
        t_birth     = new FjEditLabel();
        i_balance   = new FjEditLabel(false);
        i_balance.setFont(i_balance.getFont().deriveFont(Font.BOLD));
        i_coupon    = new FjEditLabel(false);
        i_coupon.setFont(i_coupon.getFont().deriveFont(Font.BOLD));
        ri_balance  = new FjEditLabel(false);
        ri_balance.setFont(ri_balance.getFont().deriveFont(Font.BOLD));
        ri_coupon   = new FjEditLabel(false);
        ri_coupon.setFont(ri_coupon.getFont().deriveFont(Font.BOLD));
        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "基本信息"));
        panel_basic.setLayout(new GridLayout(13, 1));
        panel_basic.add(UIToolkit.createBasicInfoLabel("用户编号", i_caid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("账    号", c_user));
        panel_basic.add(UIToolkit.createBasicInfoLabel("来源平台", i_channel));
        panel_basic.add(UIToolkit.createBasicInfoLabel("昵    称", c_nick));
        panel_basic.add(UIToolkit.createBasicInfoLabel("性    别", i_gender));
        panel_basic.add(UIToolkit.createBasicInfoLabel("电    话", c_phone));
        panel_basic.add(UIToolkit.createBasicInfoLabel("地    址", c_address));
        panel_basic.add(UIToolkit.createBasicInfoLabel("邮    编", c_zipcode));
        panel_basic.add(UIToolkit.createBasicInfoLabel("出生日期", t_birth));
        panel_basic.add(UIToolkit.createBasicInfoLabel("账户余额",  i_balance));
        panel_basic.add(UIToolkit.createBasicInfoLabel("优惠券金额", i_coupon));
        panel_basic.add(UIToolkit.createBasicInfoLabel("账户余额  (RT)", ri_balance));
        panel_basic.add(UIToolkit.createBasicInfoLabel("优惠券金额(RT)", ri_coupon));
        
        pane_user = new FjListPane<String>();
        pane_user.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "关联用户"));
        pane_account = new FjListPane<String>();
        pane_account.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "在租账号"));
        pane_order = new FjListPane<String>();
        pane_order.setBorder(null);
        order_switch = new JButton(SWITCH_TITLE_OPEN);
        order_switch.setMargin(new Insets(0, 0, 0, 0));
        
        JPanel panel_north = new JPanel();
        panel_north.setLayout(new BoxLayout(panel_north, BoxLayout.Y_AXIS));
        panel_north.add(toolbar);
        panel_north.add(panel_basic);
        panel_north.add(pane_user);
        panel_north.add(pane_account);
        
        JPanel panel_center = new JPanel();
        panel_center.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "订单列表"));
        panel_center.setLayout(new BorderLayout());
        panel_center.add(order_switch, BorderLayout.NORTH);
        panel_center.add(pane_order, BorderLayout.CENTER);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel_north, BorderLayout.NORTH);
        getContentPane().add(panel_center, BorderLayout.CENTER);
        
        setTitle(String.format("管理用户“%s”", user.c_user));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(480, 600));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        registerListener();
        
        updateBasicPane();
        updateListPane();

    }
    
    private JSONObject args = new JSONObject();
    
    private void registerListener() {
        i_channel.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("channel", getChannel2Int(new_value));
                i_channel.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_nick.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("nick", new_value);
                c_nick.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        i_gender.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("gender", "女".equals(new_value) ? 0 : "男".equals(new_value) ? 1 : 2);
                i_gender.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_phone.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("phone", new_value);
                c_phone.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_address.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("address", new_value);
                c_address.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_zipcode.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("zipcode", new_value);
                c_zipcode.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        t_birth.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("birth", new_value);
                t_birth.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            if (args.isEmpty()) {
                JOptionPane.showMessageDialog(ManageChannelAccount.this, "没有可更新的内容", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args);
            UIToolkit.showServerResponse(rsp);
            if (Service.isResponseSuccess(rsp)) {
                if (args.has("user"))       c_user.setForeground(Color.darkGray);
                if (args.has("channel"))    i_channel.setForeground(Color.darkGray);
                if (args.has("nick"))       c_nick.setForeground(Color.darkGray);
                if (args.has("gender"))     i_gender.setForeground(Color.darkGray);
                if (args.has("phone"))      c_phone.setForeground(Color.darkGray);
                if (args.has("address"))    c_address.setForeground(Color.darkGray);
                if (args.has("zipcode"))    c_zipcode.setForeground(Color.darkGray);
                if (args.has("birth"))      t_birth.setForeground(Color.darkGray);
                args.clear();
            }
        });
        ((JButton) toolbar.getComponent(2)).addActionListener(e->{
            FjTextField money = new FjTextField();
            money.setDefaultTips("(请输入充值金额)");
            while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ManageChannelAccount.this, money, "充值", JOptionPane.OK_CANCEL_OPTION)) {
                if (0 == money.getText().length()) {
                    JOptionPane.showMessageDialog(ManageChannelAccount.this, "充值金额一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                float balance = puser.i_balance + Float.parseFloat(money.getText());
                JSONObject args = new JSONObject();
                args.put("paid", puser.i_paid);
                args.put("balance", balance);
                FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_PLATFORM_ACCOUNT, args);
                UIToolkit.showServerResponse(rsp);
                if (Service.isResponseSuccess(rsp)) {
                    Service.updatePlatformAccount();
                    updateBasicPane();
                }
                break;
            }
        });
        ((JButton) toolbar.getComponent(3)).addActionListener(e->{
            FjTextField money = new FjTextField();
            money.setDefaultTips("(请输入充券金额)");
            while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ManageChannelAccount.this, money, "充券", JOptionPane.OK_CANCEL_OPTION)) {
                if (0 == money.getText().length()) {
                    JOptionPane.showMessageDialog(ManageChannelAccount.this, "充券金额一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                float coupon = puser.i_coupon + Float.parseFloat(money.getText());
                JSONObject args = new JSONObject();
                args.put("paid", puser.i_paid);
                args.put("coupon", coupon);
                FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_PLATFORM_ACCOUNT, args);
                UIToolkit.showServerResponse(rsp);
                if (Service.isResponseSuccess(rsp)) {
                    Service.updatePlatformAccount();
                    updateBasicPane();
                }
                break;
            }
        });
        ((JButton) toolbar.getComponent(4)).addActionListener(e->{
            UIToolkit.createOrder(user);
            
            Service.updateOrder();
            Service.updateGameAccountRent();
            updateListPane();
        });
        ((JButton) toolbar.getComponent(6)).addActionListener(e->{
            BeanChannelAccount user2 = UIToolkit.chooseChannelAccount();
            if (null == user2) return;
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(ManageChannelAccount.this, String.format("即将关联用户%s和%s，关联之后将无法回退，继续？", user.c_user, user2.c_user), "提示", JOptionPane.OK_CANCEL_OPTION))
                return;
            
            int paid_to = Service.getPlatformAccountByChannelAccount(user.i_caid);
            int paid_from = Service.getPlatformAccountByChannelAccount(user2.i_caid);
            if (paid_to == paid_from) {
                JOptionPane.showMessageDialog(ManageChannelAccount.this, "即将关联的两个账户已经属于同一个平台账户了", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JSONObject args = new JSONObject();
            args.put("paid_to", paid_to);
            args.put("paid_from", paid_from);
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, args);
            UIToolkit.showServerResponse(rsp);
            
            Service.updatePlatformAccount();
            Service.updatePlatformAccountMap();
            updateBasicPane();
            updateListPane();
        });
        order_switch.addActionListener(e->{
            switch (order_switch.getText()) {
            case SWITCH_TITLE_OPEN:
                order_switch.setText(SWITCH_TITLE_ALL);
                break;
            case SWITCH_TITLE_ALL:
                order_switch.setText(SWITCH_TITLE_OPEN);
                break;
            }
            updatePaneOrder();
        });
    }
    
    private void updateListPane() {
        pane_user.getList().removeAllCell();
        Service.getChannelAccountRelated(user.i_caid).forEach(user->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", user.i_caid, user.c_user), "[" + getChannel2String(user.i_channel) + "]");
            cell.addActionListener(e->new ManageChannelAccount(user.i_caid).setVisible(true));
            pane_user.getList().addCell(cell);
        });
        
        pane_account.getList().removeAllCell();
        Service.getRentGameAccountByChannelAccount(user.i_caid, Service.RENT_TYPE_A).forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", account.i_gaid, account.c_user), "[A类]");
            cell.addActionListener(e->new ManageGameAccount(account.i_gaid).setVisible(true));
            pane_account.getList().addCell(cell);
        });
        Service.getRentGameAccountByChannelAccount(user.i_caid, Service.RENT_TYPE_B).forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", account.i_gaid, account.c_user), "[B类]");
            cell.addActionListener(e->new ManageGameAccount(account.i_gaid).setVisible(true));
            pane_account.getList().addCell(cell);
        });
        
        updatePaneOrder();
        
        getContentPane().revalidate();
    }
    
    private void updatePaneOrder() {
        pane_order.getList().removeAllCell();
        Service.map_order.values()
                .stream()
                .filter(order->order.i_caid == user.i_caid)
                .filter(order->{
                    if (SWITCH_TITLE_OPEN.equals(order_switch.getText())) return !order.isClose();
                    else return true;})
                .forEach(order->{
                    FjListCellString cell = new FjListCellString(String.format("%s ~ %s", order.t_open, order.t_close), String.format("0x%08X", order.i_oid));
                    if (order.isClose()) cell.setForeground(Color.lightGray);
                    cell.addActionListener(e->{
                        ManageOrder dialog = new ManageOrder(order.i_oid);
                        dialog.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosed(WindowEvent e) {
                                Service.updateOrder();
                                Service.updateGameAccountRent();
                                updateListPane();
                            }
                        });
                        dialog.setVisible(true);
                    });
                    pane_order.getList().addCell(cell);
                });
        pane_order.getList().repaint();
    }
    
    private void updateBasicPane() {
        user = Service.map_channel_account.get(user.i_caid);
        puser = Service.map_platform_account.get(Service.getPlatformAccountByChannelAccount(user.i_caid));
        
        i_caid.setText(String.format("0x%08X", user.i_caid));
        c_user.setText(user.c_user);
        i_channel.setText(getChannel2String(user.i_channel));
        c_nick.setText(0 == user.c_nick.length() ? "(没有昵称)" : user.c_nick);
        i_gender.setText(0 == user.i_gender ? "女" : 1 == user.i_gender ? "男" : "人妖");
        c_phone.setText(0 == user.c_phone.length() ? "(没有电话)" : user.c_phone);
        c_address.setText(0 == user.c_address.length() ? "(没有地址)" : user.c_address);
        c_zipcode.setText(0 == user.c_zipcode.length() ? "(没有邮编)" : user.c_zipcode);
        t_birth.setText(0 == user.t_birth.length() ? "(没有生日)" : user.t_birth);
        i_balance.setText(puser.i_balance + "元");
        i_coupon.setText(puser.i_coupon + "元");
        float[] ps = prestatement(user.i_caid);
        ri_balance.setText(ps[0] + "元");
        ri_coupon.setText(ps[1] + "元");
    }
    
    private static String getChannel2String(int channel) {
        switch (channel) {
        case Service.USER_TYPE_TAOBAO: return "淘宝";
        case Service.USER_TYPE_WECHAT: return "微信";
        case Service.USER_TYPE_ALIPAY: return "支付宝";
        default: return "未知";
        }
    }
    
    private static int getChannel2Int(String channel) {
        switch (channel) {
        case "淘宝": return Service.USER_TYPE_TAOBAO;
        case "微信": return Service.USER_TYPE_WECHAT;
        case "支付宝": return Service.USER_TYPE_ALIPAY;
        default: return Integer.parseInt(channel);
        }
    }
    
    private static float[] prestatement(int caid) {
        BeanPlatformAccount puser = Service.map_platform_account.get(Service.getPlatformAccountByChannelAccount(caid));
        float balance   = puser.i_balance;
        float coupon    = puser.i_coupon;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        float cost = 0.00f;
        try {
            cost = Service.getOrderByChannelAccount(caid)
                .stream()
                .filter(order->!order.isClose())
                .map(order->{
                    try {
                        return order.commodities.values()
                                .stream()
                                .filter(commodity->!commodity.isClose())
                                .map(commodity->{
                                    try {
                                        long begin  = sdf.parse(commodity.t_begin).getTime();
                                        long end    = System.currentTimeMillis();
                                        int  times  = (int) ((end - begin) / 1000 / 60 / 60 / 12);
                                        if (times < 2) times = 2;
                                        else times = times + 1;
                                        
                                        return (commodity.i_price / 2) * times;
                                    } catch (Exception e) {e.printStackTrace();}
                                    return 0.00f;
                                })
                                .reduce((cost1, cost2)->(cost1 + cost2))
                                .get();
                    } catch (Exception e) {}
                    return 0.00f;
                })
                .reduce((cost1, cost2)->(cost1 + cost2))
                .get();
        } catch (Exception e) {}
        
        if (cost <= coupon) {
            coupon -= cost;
        } else {
            balance -= (cost - coupon);
            coupon = 0.00f;
        }
        
        return new float[] {balance, coupon};
    }
}
