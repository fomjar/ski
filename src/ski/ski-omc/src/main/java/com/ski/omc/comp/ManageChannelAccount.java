package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjTextField;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanPlatformAccount;
import com.ski.common.bean.BeanPlatformAccountMoney;
import com.ski.omc.UIToolkit;

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
    private FjEditLabel         c_name;
    private FjEditLabel         i_gender;
    private FjEditLabel         c_phone;
    private FjEditLabel         c_address;
    private FjEditLabel         c_zipcode;
    private FjEditLabel         t_birth;
    private FjEditLabel         i_cash;
    private FjEditLabel         i_coupon;
    private FjEditLabel         ri_cash;
    private FjEditLabel         ri_coupon;
    private FjListPane<String>  pane_user;
    private FjListPane<String>  pane_account;
    private FjListPane<String>  pane_order;
    private JButton             order_switch;
    
    public ManageChannelAccount(int caid) {
        super(MainFrame.getInstance());
        
        user = CommonService.getChannelAccountByCaid(caid);
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        toolbar.add(new JButton("更新"));
        toolbar.addSeparator();
        toolbar.add(new JButton("充值"));
        toolbar.add(new JButton("创建订单"));
        toolbar.addSeparator();
        toolbar.add(new JButton("操作记录"));
        toolbar.add(new JButton("关联用户"));
        
        i_caid      = new FjEditLabel(false);
        i_caid.setForeground(Color.gray);
        c_user      = new FjEditLabel(false);
        i_channel   = new FjEditLabel(false);
        c_name      = new FjEditLabel();
        i_gender    = new FjEditLabel();
        c_phone     = new FjEditLabel();
        c_address   = new FjEditLabel();
        c_zipcode   = new FjEditLabel();
        t_birth     = new FjEditLabel();
        i_cash   = new FjEditLabel(false);
        i_cash.setFont(i_cash.getFont().deriveFont(Font.BOLD));
        i_coupon    = new FjEditLabel(false);
        i_coupon.setFont(i_coupon.getFont().deriveFont(Font.BOLD));
        ri_cash  = new FjEditLabel(false);
        ri_cash.setFont(ri_cash.getFont().deriveFont(Font.BOLD));
        ri_coupon   = new FjEditLabel(false);
        ri_coupon.setFont(ri_coupon.getFont().deriveFont(Font.BOLD));
        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "基本信息"));
        panel_basic.setLayout(new GridLayout(13, 1));
        panel_basic.add(UIToolkit.createBasicInfoLabel("用户编号", i_caid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("账    号", c_user, "复制", e->{
            StringSelection ss = new StringSelection(user.c_user);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
        }));
        panel_basic.add(UIToolkit.createBasicInfoLabel("来源平台", i_channel));
        panel_basic.add(UIToolkit.createBasicInfoLabel("姓    名", c_name));
        panel_basic.add(UIToolkit.createBasicInfoLabel("性    别", i_gender));
        panel_basic.add(UIToolkit.createBasicInfoLabel("电    话", c_phone));
        panel_basic.add(UIToolkit.createBasicInfoLabel("地    址", c_address));
        panel_basic.add(UIToolkit.createBasicInfoLabel("邮    编", c_zipcode));
        panel_basic.add(UIToolkit.createBasicInfoLabel("出生日期", t_birth));
        panel_basic.add(UIToolkit.createBasicInfoLabel("账户余额",  i_cash));
        panel_basic.add(UIToolkit.createBasicInfoLabel("优惠券金额", i_coupon));
        panel_basic.add(UIToolkit.createBasicInfoLabel("账户余额  (RT)", ri_cash));
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
        
        setTitle(String.format("管理用户 - %s", user.getDisplayName()));
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
        c_name.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("name", new_value);
                c_name.setForeground(UIToolkit.COLOR_MODIFYING);
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
            
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args);
            UIToolkit.showServerResponse(rsp);
            if (CommonService.isResponseSuccess(rsp)) {
                if (args.has("user"))       c_user.setForeground(Color.darkGray);
                if (args.has("name"))       c_name.setForeground(Color.darkGray);
                if (args.has("gender"))     i_gender.setForeground(Color.darkGray);
                if (args.has("phone"))      c_phone.setForeground(Color.darkGray);
                if (args.has("address"))    c_address.setForeground(Color.darkGray);
                if (args.has("zipcode"))    c_zipcode.setForeground(Color.darkGray);
                if (args.has("birth"))      t_birth.setForeground(Color.darkGray);
                args.clear();
            }
        });
        ((JButton) toolbar.getComponent(2)).addActionListener(e->{
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
                        remark.setText("【客服充值】");
                        money.setText("150.00");
                        break;
                    case 1:
                        remark.setText("【客服充券】");
                        money.setText("10.00");
                        break;
                    case 2:
                        remark.setText("【客服退款】");
                        money.setText("-" + puser.i_cash);
                        break;
                    }
                }
            });
            type.setSelectedIndex(1);
            type.setSelectedIndex(0);
            while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ManageChannelAccount.this, panel, "充值", JOptionPane.OK_CANCEL_OPTION)) {
                if (0 == money.getText().length()) {
                    JOptionPane.showMessageDialog(ManageChannelAccount.this, "金额一定要填", "错误", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                JSONObject args = new JSONObject();
                args.put("paid",    puser.i_paid);
                if (0 < remark.getText().length()) args.put("remark",  remark.getText());
                args.put("type",    0 == type.getSelectedIndex() ? CommonService.MONEY_CASH : 1 == type.getSelectedIndex() ? CommonService.MONEY_COUPON : 2 == type.getSelectedIndex() ? CommonService.MONEY_CASH : -1);
                args.put("money",   Float.parseFloat(money.getText()));
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, args);
                UIToolkit.showServerResponse(rsp);
                CommonService.updatePlatformAccount();
                updateBasicPane();
                break;
            }
        });
        ((JButton) toolbar.getComponent(3)).addActionListener(e->{
            UIToolkit.createOrder(user);
            updateListPane();
        });
        ((JButton) toolbar.getComponent(5)).addActionListener(e->{
            CommonService.updatePlatformAccountMoney();
            List<BeanPlatformAccountMoney> money = CommonService.getPlatformAccountMoneyByPaid(puser.i_paid);
            JTable table = new JTable();
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setHorizontalAlignment(SwingConstants.CENTER);
            table.setDefaultRenderer(Object.class, renderer);
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setColumnIdentifiers(new String[] {"平台用户", "备注", "时间", "操作类型", "基准值", "变化值"});
            money.forEach(m->{
                model.addRow(new String[] {
                        String.format("0x%08X", m.i_paid),
                        m.c_remark,
                        m.t_time,
                        CommonService.MONEY_CONSUME == m.i_type ? "消费" : CommonService.MONEY_CASH == m.i_type ? "充值" : CommonService.MONEY_COUPON == m.i_type ? "充券" : "未知",
                        m.i_base + "元",
                        m.i_money + "元"});
            });
            JDialog dialog = new JDialog(MainFrame.getInstance(), "操作记录");
            dialog.getContentPane().setLayout(new BorderLayout());
            dialog.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
            
            dialog.setModal(false);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setSize(new Dimension(800, 200));
            Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setLocation((owner.width - dialog.getWidth()) / 2, (owner.height - dialog.getHeight()) / 2);
            dialog.setVisible(true);
            
            table.getColumn("备注").setPreferredWidth(280);
            table.getColumn("备注").setCellRenderer(new DefaultTableCellRenderer()); // alignment to left
            table.getColumn("时间").setPreferredWidth(160);
        });
        ((JButton) toolbar.getComponent(6)).addActionListener(e->{
            BeanChannelAccount user2 = null;
            int option = JOptionPane.CLOSED_OPTION;
            while (JOptionPane.CLOSED_OPTION != (option = JOptionPane.showOptionDialog(null, "请选择关联途径", "提示", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"创建新用户", "选择现有用户"}, "选择现有用户"))) {
                switch (option) {
                case JOptionPane.YES_OPTION:
                    String suser = UIToolkit.createChannelAccount();
                    if (null == suser) continue; // no choose
                    user2 = CommonService.getChannelAccountByUser(suser).get(0);
                    break;
                case JOptionPane.NO_OPTION:
                    user2 = UIToolkit.chooseChannelAccount();
                    break;
                }
                if (null == user2) continue;
                
                if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(ManageChannelAccount.this, String.format("即将关联用户“%s”和“%s”，关联之后将无法回退，继续？", user.getDisplayName(), user2.getDisplayName()), "提示", JOptionPane.OK_CANCEL_OPTION))
                    return;
                
                int paid_to = CommonService.getPlatformAccountByCaid(user.i_caid);
                int paid_from = CommonService.getPlatformAccountByCaid(user2.i_caid);
                if (paid_to == paid_from) {
                    JOptionPane.showMessageDialog(ManageChannelAccount.this, "即将关联的两个账户已经属于同一个平台账户了", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                JSONObject args = new JSONObject();
                args.put("paid_to", paid_to);
                args.put("paid_from", paid_from);
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, args);
                UIToolkit.showServerResponse(rsp);
                
                CommonService.updatePlatformAccount();
                CommonService.updatePlatformAccountMap();
                updateBasicPane();
                updateListPane();
                
                break;
            }
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
        CommonService.getChannelAccountRelated(user.i_caid).forEach(user->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", user.i_caid, user.getDisplayName()), "[" + getChannel2String(user.i_channel) + "]");
            cell.addActionListener(e->new ManageChannelAccount(user.i_caid).setVisible(true));
            pane_user.getList().addCell(cell);
        });
        
        pane_account.getList().removeAllCell();
        CommonService.getRentGameAccountByCaid(user.i_caid, CommonService.RENT_TYPE_A).forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", account.i_gaid, account.c_user), "[A类]");
            cell.addActionListener(e->new ManageGameAccount(account.i_gaid).setVisible(true));
            pane_account.getList().addCell(cell);
        });
        CommonService.getRentGameAccountByCaid(user.i_caid, CommonService.RENT_TYPE_B).forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", account.i_gaid, account.c_user), "[B类]");
            cell.addActionListener(e->new ManageGameAccount(account.i_gaid).setVisible(true));
            pane_account.getList().addCell(cell);
        });
        
        updatePaneOrder();
        
        getContentPane().revalidate();
    }
    
    private void updatePaneOrder() {
        pane_order.getList().removeAllCell();
        CommonService.getOrderAll().values()
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
                                updateBasicPane();
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
        user = CommonService.getChannelAccountByCaid(user.i_caid);
        puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(user.i_caid));
        
        i_caid.setText(String.format("0x%08X", user.i_caid));
        c_user.setText(user.getDisplayName());
        i_channel.setText(getChannel2String(user.i_channel));
        c_name.setText(0 == user.c_name.length() ? "(没有姓名)" : user.c_name);
        i_gender.setText(CommonService.GENDER_FEMALE == user.i_gender ? "女" : CommonService.GENDER_MALE == user.i_gender ? "男" : "人妖");
        c_phone.setText(0 == user.c_phone.length() ? "(没有电话)" : user.c_phone);
        c_address.setText(0 == user.c_address.length() ? "(没有地址)" : user.c_address);
        c_zipcode.setText(0 == user.c_zipcode.length() ? "(没有邮编)" : user.c_zipcode);
        t_birth.setText(0 == user.t_birth.length() ? "(没有生日)" : user.t_birth);
        i_cash.setText(puser.i_cash + "元");
        i_coupon.setText(puser.i_coupon + "元");
        float[] ps = CommonService.prestatement(user.i_caid);
        ri_cash.setText(ps[0] + "元");
        ri_coupon.setText(ps[1] + "元");
    }
    
    private static String getChannel2String(int channel) {
        switch (channel) {
        case CommonService.CHANNEL_TAOBAO: return "淘  宝";
        case CommonService.CHANNEL_WECHAT: return "微  信";
        case CommonService.CHANNEL_ALIPAY: return "支付宝";
        default: return "未  知";
        }
    }
    
}
