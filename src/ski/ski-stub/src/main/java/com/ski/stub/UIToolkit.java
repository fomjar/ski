package com.ski.stub;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.fomjar.widget.FjTextField;
import com.ski.common.SkiCommon;
import com.ski.stub.bean.BeanChannelAccount;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class UIToolkit {
    
    public static final Font FONT = new Font("仿宋", Font.PLAIN, 14);

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
        
        // UIManager.getLookAndFeelDefaults().forEach((key, value)->{System.out.println(key + "=" + value);});
    }
    
    public static JPanel createBasicInfoLabel(String label, FjEditLabel field) {
        JLabel jlabel = new JLabel(label);
        jlabel.setPreferredSize(new Dimension(80, 0));
        
        JPanel jpanel = new JPanel();
        jpanel.setLayout(new BorderLayout());
        jpanel.add(jlabel, BorderLayout.WEST);
        jpanel.add(field, BorderLayout.CENTER);
        
        return jpanel;
    }
    
    public static FjDscpMessage createGame() {
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
        
        int option = JOptionPane.showConfirmDialog(null, panel, "创建游戏", JOptionPane.OK_CANCEL_OPTION);
        if (JOptionPane.OK_OPTION == option) {
            JSONObject args = new JSONObject();
            if (0 != c_country.getText().length())  args.put("country", c_country.getText());
            if (0 != t_sale.getText().length())     args.put("sale",    t_sale.getText());
            if (0 != c_name_zh.getText().length())  args.put("name_zh", c_name_zh.getText());
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME, args);
            JOptionPane.showConfirmDialog(null, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
            return rsp;
        }
        
        return null;
    }
    
    public static FjDscpMessage createGameAccount() {
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
        
        int option = JOptionPane.showConfirmDialog(null, panel, "创建游戏账号", JOptionPane.OK_CANCEL_OPTION);
        if (JOptionPane.OK_OPTION == option) {
            JSONObject args = new JSONObject();
            if (0 != c_user.getText().length())     args.put("user",        c_user.getText());
            if (0 != c_pass.getText().length())     args.put("pass_curr",   c_pass.getText());
            if (0 != t_birth.getText().length())    args.put("birth",       t_birth.getText());
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
            JOptionPane.showConfirmDialog(null, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
            return rsp;
        }
        return null;
    }
    
    public static FjDscpMessage createChannelAccount() {
        JComboBox<String>   i_channel = new JComboBox<String>(new String[] {"0 - 淘宝", "1 - 微信", "2 - 支付宝"});
        i_channel.setEditable(false);
        FjTextField         c_user  = new FjTextField();
        FjTextField         c_phone  = new FjTextField();
        c_user.setDefaultTips("用户名");
        c_phone.setDefaultTips("电话号码");
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));
        panel.add(i_channel);
        panel.add(c_user);
        panel.add(c_phone);
        
        int option = JOptionPane.showConfirmDialog(null, panel, "创建用户", JOptionPane.OK_CANCEL_OPTION);
        if (JOptionPane.OK_OPTION == option) {
            JSONObject args = new JSONObject();
            args.put("gender", 1);
            args.put("channel", Integer.parseInt(i_channel.getSelectedItem().toString().split(" ")[0]));
            if (0 != c_user.getText().length())     args.put("user",    c_user.getText());
            if (0 != c_phone.getText().length())    args.put("phone",   c_phone.getText());
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args);
            JOptionPane.showConfirmDialog(null, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
            return rsp;
        }
        return null;
    }
    
    public static FjDscpMessage createOrder() {
        JComboBox<String>   i_platform = new JComboBox<String>(new String[] {"0 - 淘宝", "1 - 微信"});
        JLabel i_caid_label = new JLabel();
        i_caid_label.setPreferredSize(new Dimension(240, 0));
        JButton i_caid_button = new JButton("选择用户");
        i_caid_button.setMargin(new Insets(0, 0, 0, 0));
        i_caid_button.addActionListener(e->{
            BeanChannelAccount account = chooseChannelAccount();
            if (null == account) return;
            i_caid_label.setText(String.format("0x%08X - %s", account.i_caid, account.c_user));
        });
        
        JPanel i_caid = new JPanel();
        i_caid.setLayout(new BorderLayout());
        i_caid.add(i_caid_label, BorderLayout.CENTER);
        i_caid.add(i_caid_button, BorderLayout.EAST);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        panel.add(i_platform);
        panel.add(i_caid);
        
        int option = JOptionPane.showConfirmDialog(null, panel, "创建订单", JOptionPane.OK_CANCEL_OPTION);
        if (JOptionPane.OK_OPTION == option) {
            JSONObject args = new JSONObject();
            args.put("platform", Integer.parseInt(i_platform.getSelectedItem().toString().split(" ")[0]));
            args.put("caid", Integer.parseInt(i_caid_label.getText().split(" ")[0].split("x")[1], 16));
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_ORDER, args);
            JOptionPane.showConfirmDialog(null, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
            return rsp;
        }
        return null;
    }
    
    public static FjDscpMessage createOrderItem(int oid) {
        JLabel              i_oid = new JLabel(String.format("订单编号：0x%08X", oid));
        i_oid.setPreferredSize(new Dimension(360, 0));
        JComboBox<String>   i_oper_type = new JComboBox<String>(new String[] {"0 - 购买", "1 - 充值", "2 - 起租", "3 - 退租", "4 - 停租", "5 - 续租", "6 - 换租", "7 - 赠券"});
        FjTextField         c_remark = new FjTextField();
        c_remark.setDefaultTips("备注");
        FjTextField         c_cost = new FjTextField();
        
        JComboBox<String> rent_type = new JComboBox<String>();
        JLabel game_account_label= new JLabel();
        JButton game_account_button = new JButton();
        game_account_button.setMargin(new Insets(0, 0, 0, 0));
        game_account_button.addActionListener(e->{
            BeanGameAccount account = null;
            while (null != (account = chooseGameAccount())) {
                ((DefaultComboBoxModel<String>) rent_type.getModel()).removeAllElements();
                switch (Integer.parseInt(i_oper_type.getSelectedItem().toString().split(" ")[0])) {
                case 2: // 2 - 起租
                case 6: // 6 - 换租
                    if (Service.RENT_STATE_IDLE == Service.getGameAccountCurrentRentState(account.i_gaid, Service.RENT_TYPE_A))
                        ((DefaultComboBoxModel<String>) rent_type.getModel()).addElement("租赁类型：A");
                    if (Service.RENT_STATE_IDLE == Service.getGameAccountCurrentRentState(account.i_gaid, Service.RENT_TYPE_B))
                        ((DefaultComboBoxModel<String>) rent_type.getModel()).addElement("租赁类型：B");
                    game_account_label.setText(String.format("0x%08X - %s", account.i_gaid, account.c_user));
                    if (0 == rent_type.getModel().getSize()) {
                        JOptionPane.showConfirmDialog(null, "此账号A/B类均已在租赁当中，请重新选择", "错误", JOptionPane.DEFAULT_OPTION);
                        continue;
                    }
                    break;
                case 3: // 3 - 退租
                case 4: // 4 - 停租
                case 5: // 5 - 续租
                    if (Service.map_order.get(oid).i_caid == Service.getGameAccountCurrentRentUser(account.i_gaid, Service.RENT_TYPE_A))
                        ((DefaultComboBoxModel<String>) rent_type.getModel()).addElement("租赁类型：A");
                    if (Service.map_order.get(oid).i_caid == Service.getGameAccountCurrentRentUser(account.i_gaid, Service.RENT_TYPE_B))
                        ((DefaultComboBoxModel<String>) rent_type.getModel()).addElement("租赁类型：B");
                    game_account_label.setText(String.format("0x%08X - %s", account.i_gaid, account.c_user));
                    if (0 == rent_type.getModel().getSize()) {
                        JOptionPane.showConfirmDialog(null, "此用户并没有租用选定的游戏账号，请重新选择", "错误", JOptionPane.DEFAULT_OPTION);
                        continue;
                    }
                    break;
                }
                break;
            }
            if (null == account) game_account_label.setText("");
        });
        JPanel game_account0 = new JPanel();
        game_account0.setLayout(new BorderLayout());
        game_account0.add(game_account_label, BorderLayout.CENTER);
        game_account0.add(game_account_button, BorderLayout.EAST);
        JPanel game_account = new JPanel();
        game_account.setLayout(new BorderLayout());
        game_account.add(game_account0, BorderLayout.CENTER);
        game_account.add(rent_type, BorderLayout.EAST);
        
        JComboBox<String> rent_type_old = new JComboBox<String>();
        JLabel game_account_label_old= new JLabel();
        JButton game_account_button_old = new JButton();
        game_account_button_old.setMargin(new Insets(0, 0, 0, 0));
        game_account_button_old.addActionListener(e->{
            BeanGameAccount account = null;
            while (null != (account = chooseGameAccount())) {
                ((DefaultComboBoxModel<String>) rent_type_old.getModel()).removeAllElements();
                if (Service.map_order.get(oid).i_caid == Service.getGameAccountCurrentRentUser(account.i_gaid, Service.RENT_TYPE_A))
                    ((DefaultComboBoxModel<String>) rent_type_old.getModel()).addElement("租赁类型：A");
                if (Service.map_order.get(oid).i_caid == Service.getGameAccountCurrentRentUser(account.i_gaid, Service.RENT_TYPE_B))
                    ((DefaultComboBoxModel<String>) rent_type_old.getModel()).addElement("租赁类型：B");
                game_account_label_old.setText(String.format("0x%08X - %s", account.i_gaid, account.c_user));
                if (0 == rent_type_old.getModel().getSize()) {
                    JOptionPane.showConfirmDialog(null, "此用户并没有租用选定的游戏账号，请重新选择", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                break;
            }
            if (null == account) game_account_label_old.setText("");
        });
        JPanel game_account_old0 = new JPanel();
        game_account_old0.setLayout(new BorderLayout());
        game_account_old0.add(game_account_label_old, BorderLayout.CENTER);
        game_account_old0.add(game_account_button_old, BorderLayout.EAST);
        JPanel game_account_old = new JPanel();
        game_account_old.setLayout(new BorderLayout());
        game_account_old.add(game_account_old0, BorderLayout.CENTER);
        game_account_old.add(rent_type_old, BorderLayout.EAST);
        
        JComponent[] components = new JComponent[] {c_cost, game_account, rent_type, game_account_old, rent_type_old};
        for (JComponent c : components) c.setVisible(false);
        i_oper_type.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                for (JComponent c : components) c.setVisible(false);
                
                switch(Integer.parseInt(i_oper_type.getSelectedItem().toString().split(" ")[0])) {
                case 0: // 0 - 购买
                    JOptionPane.showConfirmDialog(null, "没有可购买产品，暂不开放", "信息", JOptionPane.DEFAULT_OPTION);
                    i_oper_type.setSelectedIndex(1);
                    break;
                case 1: // 1 - 充值
                    c_cost.setVisible(true);
                    c_cost.setDefaultTips("输入充值金额");
                    break;
                case 2: // 2 - 起租
                    c_cost.setVisible(true);
                    c_cost.setDefaultTips("输入账号日租金额");
                    game_account.setVisible(true);
                    game_account_button.setText("选择游戏账号");
                    rent_type.setVisible(true);
                    break;
                case 3: // 3 - 退租
                case 4: // 4 - 停租
                case 5: // 5 - 续租
                    game_account.setVisible(true);
                    game_account_button.setText("选择游戏账号");
                    rent_type.setVisible(true);
                    break;
                case 6: // 6 - 换租
                    c_cost.setVisible(true);
                    c_cost.setDefaultTips("输入新账号日租金额");
                    game_account.setVisible(true);
                    game_account_button.setText("选择新的游戏账号");
                    rent_type.setVisible(true);
                    game_account_old.setVisible(true);
                    game_account_button_old.setText("选择将被替换掉的游戏账号");
                    rent_type_old.setVisible(true);
                    break;
                case 7: // 7 - 赠券
                    c_cost.setVisible(true);
                    c_cost.setDefaultTips("输入优惠券金额");
                    break;
                }
                Window window = SwingUtilities.getWindowAncestor(i_oper_type);
                if (null != window) window.pack();
            }
        });
        i_oper_type.setSelectedIndex(1);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(i_oid);
        panel.add(i_oper_type);
        panel.add(c_remark);
        panel.add(Box.createVerticalStrut(8));
        panel.add(c_cost);
        panel.add(Box.createVerticalStrut(8));
        panel.add(game_account);
        panel.add(Box.createVerticalStrut(8));
        panel.add(game_account_old);
        
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建订单项", JOptionPane.OK_CANCEL_OPTION)) {
            JSONObject args = new JSONObject();
            args.put("oid", oid);
            int ot = Integer.parseInt(i_oper_type.getSelectedItem().toString().split(" ")[0]);
            args.put("oper_type", ot);
            args.put("remark", c_remark.getText());
            
            switch(ot) {
            case 0: // 0 - 购买
                break;
            case 1: // 1 - 充值
                if (0 == c_cost.getText().length()) {
                    JOptionPane.showConfirmDialog(null, "请输入充值金额", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                args.put("oper_arg0", c_cost.getText());
                break;
            case 2: // 2 - 起租
                if (0 == c_cost.getText().length()) {
                    JOptionPane.showConfirmDialog(null, "请输入日租金额", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                if (0 == game_account_label.getText().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择租赁账号", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                if (0 == rent_type.getSelectedItem().toString().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择租赁类型", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                args.put("oper_arg0", c_cost.getText());
                args.put("oper_arg1", game_account_label.getText().split(" ")[0].split("x")[1]);
                args.put("oper_arg2", rent_type.getSelectedItem().toString().substring(rent_type.getSelectedItem().toString().length() - 1));
                break;
            case 3: // 3 - 退租
                if (0 == game_account_label.getText().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择退租账号", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                if (0 == rent_type.getSelectedItem().toString().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择退租类型", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                args.put("oper_arg1", game_account_label.getText().split(" ")[0].split("x")[1]);
                args.put("oper_arg2", rent_type.getSelectedItem().toString().substring(rent_type.getSelectedItem().toString().length() - 1));
                break;
            case 4: // 4 - 停租
                if (0 == game_account_label.getText().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择停租账号", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                if (0 == rent_type.getSelectedItem().toString().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择停租类型", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                args.put("oper_arg1", game_account_label.getText().split(" ")[0].split("x")[1]);
                args.put("oper_arg2", rent_type.getSelectedItem().toString().substring(rent_type.getSelectedItem().toString().length() - 1));
                break;
            case 5: // 5 - 续租
                if (0 == game_account_label.getText().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择续租账号", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                if (0 == rent_type.getSelectedItem().toString().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择续租类型", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                args.put("oper_arg1", game_account_label.getText().split(" ")[0].split("x")[1]);
                args.put("oper_arg2", rent_type.getSelectedItem().toString().substring(rent_type.getSelectedItem().toString().length() - 1));
                break;
            case 6: // 6 - 换租
                if (0 == c_cost.getText().length()) {
                    JOptionPane.showConfirmDialog(null, "请输入新账号日租金额", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                if (0 == game_account_label.getText().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择新游戏账号", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                if (0 == rent_type.getSelectedItem().toString().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择新续租类型", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                if (0 == game_account_label_old.getText().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择被替换掉的游戏账号", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                if (0 == rent_type_old.getSelectedItem().toString().length()) {
                    JOptionPane.showConfirmDialog(null, "请选择被替换掉的账号类型", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                args.put("oper_arg0", c_cost.getText());
                args.put("oper_arg1", game_account_label.getText().split(" ")[0].split("x")[1]);
                args.put("oper_arg2", rent_type.getSelectedItem().toString().substring(rent_type.getSelectedItem().toString().length() - 1));
                args.put("oper_arg3", game_account_label_old.getText().split(" ")[0].split("x")[1]);
                args.put("oper_arg4", rent_type_old.getSelectedItem().toString().substring(rent_type_old.getSelectedItem().toString().length() - 1));
                break;
            case 7: // 7 - 赠券
                if (0 == c_cost.getText().length()) {
                    JOptionPane.showConfirmDialog(null, "请输入券额", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                args.put("oper_arg0", c_cost.getText());
                break;
            }
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_ORDER_ITEM, args);
            JOptionPane.showConfirmDialog(null, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
            return rsp;
        }
        return null;
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
        Service.map_game.values().forEach(game->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", game.i_gid, game.c_name_zh));
            cell.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wrapper.obj = Service.map_game.get(game.i_gid);
                    dialog.dispose();
                }
            });
            pane.getList().addCell(cell);
        });
        
        dialog.setVisible(true);
        
        return wrapper.obj;
    }
    
    public static BeanGameAccount chooseGameAccount() {
        JCheckBox type_all = new JCheckBox("全部", true);
        JCheckBox type_a   = new JCheckBox("可A租", true);
        JCheckBox type_b   = new JCheckBox("可B租", true);
        
        FjListPane<String> pane = new FjListPane<String>();
        // 启用搜索框
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTypes(new String[] {"按账号名", "按游戏名"});
        pane.getSearchBar().setSearchTips("键入关键词搜索");
        pane.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<String>(pane.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, String celldata) {
                if (null == type) return true;
                
                switch(type) {
                case "按账号名":
                    int count = 0;
                    for (String word : words) if (celldata.contains(word)) count++;
                    return count == words.length && ((celldata.contains("可A租") && type_a.isSelected()) || (celldata.contains("可B租") && type_b.isSelected()));
                case "按游戏名":
                    List<BeanGame> games = Service.map_game.values().stream().filter(game->{
                        int count1 = 0;
                        for (String word : words) if (game.c_name_zh.contains(word)) count1++;
                        if (count1 == words.length) return true;
                        else return false;
                    }).collect(Collectors.toList());
                    Map<Integer, BeanGameAccount> accounts = new LinkedHashMap<Integer, BeanGameAccount>();
                    games.forEach(game->{
                        Service.set_game_account_game.forEach(bean->{
                            if (game.i_gid == bean.i_gid) {
                                if (!accounts.containsKey(bean.i_gaid)) {
                                    accounts.put(bean.i_gaid, Service.map_game_account.get(bean.i_gaid));
                                }
                            }
                        });
                    });
                    for (BeanGameAccount account : accounts.values())
                        if (account.i_gaid == Integer.parseInt(celldata.split(" ")[0].split("x")[1], 16)) return true && ((celldata.contains("可A租") && type_a.isSelected()) || (celldata.contains("可B租") && type_b.isSelected()));
                    return false;
                default:
                    return true;
                }
            }
        });
        
        type_all.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                type_a.setSelected(type_all.isSelected());
                type_b.setSelected(type_all.isSelected());
                pane.getSearchBar().doSearch();
            }
        });
        type_a.addActionListener(e->pane.getSearchBar().doSearch());
        type_b.addActionListener(e->pane.getSearchBar().doSearch());
        
        JPanel types = new JPanel();
        types.setBorder(BorderFactory.createTitledBorder("过滤条件"));
        types.setLayout(new GridLayout(1, 3));
        types.add(type_all);
        types.add(type_a);
        types.add(type_b);
        
        // 创建弹框
        JDialog dialog = new JDialog();
        dialog.setTitle("选择游戏账号");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 500);
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((owner.width - dialog.getWidth()) / 2, (owner.height - dialog.getHeight()) / 2);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(types, BorderLayout.NORTH);
        dialog.getContentPane().add(pane, BorderLayout.CENTER);
        
        Wrapper<BeanGameAccount> wrapper = new Wrapper<BeanGameAccount>();
        Service.map_game_account.values().forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s %s%s",
                    account.i_gaid,
                    account.c_user,
                    Service.RENT_STATE_IDLE == Service.getGameAccountCurrentRentState(account.i_gaid, Service.RENT_TYPE_A) ? "[可A租]" : "",
                    Service.RENT_STATE_IDLE == Service.getGameAccountCurrentRentState(account.i_gaid, Service.RENT_TYPE_B) ? "[可B租]" : ""));
            cell.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wrapper.obj = Service.map_game_account.get(account.i_gaid);
                    dialog.dispose();
                }
            });
            pane.getList().addCell(cell);
        });
        
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
        Service.map_channel_account.values().forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - [%s] %s",
                    account.i_caid,
                    0 == account.i_channel ? "淘宝" : 1 == account.i_channel ? "微信" : 2 == account.i_channel ? "支付宝" : "未知",
                    account.c_user));
            cell.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wrapper.obj = Service.map_channel_account.get(account.i_caid);
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
}