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
import com.ski.common.SkiCommon;
import com.ski.omc.bean.BeanChannelAccount;
import com.ski.omc.bean.BeanGame;
import com.ski.omc.bean.BeanGameAccount;
import com.ski.omc.bean.BeanOrder;
import com.ski.omc.comp.ManageOrder;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;
import sun.awt.image.ToolkitImage;

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
        UIManager.getLookAndFeelDefaults().put("Menu.font",         FONT);
        UIManager.getLookAndFeelDefaults().put("MenuItem.font",     FONT);
        
        // UIManager.getLookAndFeelDefaults().forEach((key, value)->{System.out.println(key + "=" + value);});
    }
    
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
        jlabel.setPreferredSize(new Dimension(100, 0));
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
                JOptionPane.showConfirmDialog(null, "游戏名称一定要填", "错误", JOptionPane.DEFAULT_OPTION);
                continue;
            }
            JSONObject args = new JSONObject();
            if (0 != c_country.getText().length())  args.put("country", c_country.getText());
            if (0 != t_sale.getText().length())     args.put("sale",    t_sale.getText());
            if (0 != c_name_zh.getText().length())  args.put("name_zh", c_name_zh.getText());
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME, args);
            JOptionPane.showConfirmDialog(null, rsp, "服务器响应", JOptionPane.DEFAULT_OPTION);
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
                JOptionPane.showConfirmDialog(null, "用户名一定要填", "错误", JOptionPane.DEFAULT_OPTION);
                continue;
            }
            if (0 == c_pass.getText().length()) {
                JOptionPane.showConfirmDialog(null, "密码一定要填", "错误", JOptionPane.DEFAULT_OPTION);
                continue;
            }
            JSONObject args = new JSONObject();
            if (0 != c_user.getText().length())     args.put("user",        c_user.getText());
            if (0 != c_pass.getText().length())     args.put("pass_curr",   c_pass.getText());
            if (0 != t_birth.getText().length())    args.put("birth",       t_birth.getText());
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
            JOptionPane.showConfirmDialog(null, rsp, "服务器响应", JOptionPane.DEFAULT_OPTION);
            break;
        }
    }
    
    public static void createChannelAccount() {
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
        
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建用户", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == c_user.getText().length()) {
                JOptionPane.showConfirmDialog(null, "用户名一定要填", "错误", JOptionPane.DEFAULT_OPTION);
                continue;
            }
            JSONObject args = new JSONObject();
            args.put("gender", 1);
            args.put("channel", Integer.parseInt(i_channel.getSelectedItem().toString().split(" ")[0]));
            if (0 != c_user.getText().length())     args.put("user",    c_user.getText());
            if (0 != c_phone.getText().length())    args.put("phone",   c_phone.getText());
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args);
            JOptionPane.showConfirmDialog(null, rsp, "服务器响应", JOptionPane.DEFAULT_OPTION);
            
            if (Service.isResponseSuccess(rsp)) {
                if(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "现在创建订单？", "提示", JOptionPane.OK_CANCEL_OPTION)) {
                    Service.updateChannelAccount();
                    List<BeanChannelAccount> users = Service.getChannelAccountByUserName(c_user.getText());
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
                JOptionPane.showConfirmDialog(null, "用户名一定要填", "错误", JOptionPane.DEFAULT_OPTION);
                continue;
            }
            JSONObject args = new JSONObject();
            args.put("platform", Integer.parseInt(i_platform.getSelectedItem().toString().split(" ")[0]));
            args.put("caid", Integer.parseInt(i_caid_label.getText().split(" ")[0].split("x")[1], 16));
            args.put("open", sdf.format(new Date(System.currentTimeMillis())));
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_ORDER, args);
            JOptionPane.showConfirmDialog(null, rsp, "服务器响应", JOptionPane.DEFAULT_OPTION);
            
            if (Service.isResponseSuccess(rsp)) {
                if(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "现在创建商品？", "提示", JOptionPane.OK_CANCEL_OPTION)) {
                    Service.updateOrder();
                    List<BeanOrder> orders = Service.getOrderByChannelAccount(args.getInt("caid")).stream().filter(order->!order.isClose()).collect(Collectors.toList());
                    if (1 == orders.size()) {
                        BeanOrder order = orders.get(0);
                        UIToolkit.createCommodity(order.i_oid);
                        Service.updateOrder();
                        new ManageOrder(order.i_oid).setVisible(true);
                    }
                    else JOptionPane.showConfirmDialog(null, "不能确认刚才创建的订单", "错误", JOptionPane.DEFAULT_OPTION);
                }
            }
            break;
        }
    }
    
    public static void createCommodity(int oid) {
        JLabel              c_arg0      = new JLabel();
        c_arg0.setPreferredSize(new Dimension(300, 0));
        JButton             button      = new JButton("选择游戏账号");
        button.setMargin(new Insets(0, 0, 0, 0));
        JComboBox<String>   c_arg1      = new JComboBox<String>();
        c_arg1.setEnabled(false);
        c_arg1.setPreferredSize(new Dimension(c_arg1.getPreferredSize().width, button.getPreferredSize().height));
        FjTextField         i_price     = new FjTextField();
        i_price.setDefaultTips("(单价)");
        FjTextField         c_remark    = new FjTextField();
        c_remark.setDefaultTips("(备注)");
        
        c_arg1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ItemEvent.SELECTED != e.getStateChange()) return;
                
                if (c_arg1.getSelectedItem().toString().contains("A"))
                    i_price.setText(Service.getRentPriceByGameAccount(Integer.parseInt(c_arg0.getText().split(" ")[0].split("x")[1], 16), Service.RENT_TYPE_A) + "");
                else if (c_arg1.getSelectedItem().toString().contains("B"))
                    i_price.setText(Service.getRentPriceByGameAccount(Integer.parseInt(c_arg0.getText().split(" ")[0].split("x")[1], 16), Service.RENT_TYPE_B) + "");
                else
                    i_price.setText("0.00");
            }
        });
        
        button.addActionListener(e->{
            BeanGameAccount account = null;
            while (null != (account = chooseGameAccount())) {
                c_arg0.setText(String.format("0x%08X - %s (%s)", account.i_gaid, account.c_user,
                        Service.getGameAccountGames(account.i_gaid).stream().map(game->game.c_name_zh).collect(Collectors.joining("; "))));
                ((DefaultComboBoxModel<String>) c_arg1.getModel()).removeAllElements();
                if (Service.RENT_STATE_IDLE == Service.getRentStateByGameAccount(account.i_gaid, Service.RENT_TYPE_A))
                    ((DefaultComboBoxModel<String>) c_arg1.getModel()).addElement("A类");
                if (Service.RENT_STATE_IDLE == Service.getRentStateByGameAccount(account.i_gaid, Service.RENT_TYPE_B))
                    ((DefaultComboBoxModel<String>) c_arg1.getModel()).addElement("B类");
                if (0 == c_arg1.getModel().getSize()) {
                    c_arg0.setText("");
                    c_arg1.setEnabled(false);
                    JOptionPane.showConfirmDialog(null, "选定账号已没有可租类型，请重新选择", "错误", JOptionPane.DEFAULT_OPTION);
                    continue;
                }
                c_arg1.setEnabled(true);
                break;
            }
            if (null == account) {
                c_arg0.setText("");
                ((DefaultComboBoxModel<String>) c_arg1.getModel()).removeAllElements();
                c_arg1.setEnabled(false);
                i_price.setDefaultTips("0.00");
            }
        });
        button.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorRemoved(AncestorEvent event) {}
            @Override
            public void ancestorMoved(AncestorEvent event) {}
            @Override
            public void ancestorAdded(AncestorEvent event) {
                for (ActionListener l : button.getActionListeners()) l.actionPerformed(null);
            }
        });
        
        JPanel panel0 = new JPanel();
        panel0.setLayout(new BorderLayout());
        panel0.add(c_arg0, BorderLayout.CENTER);
        panel0.add(button, BorderLayout.EAST);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));
        panel.add(panel0);
        panel.add(c_arg1);
        panel.add(i_price);
        panel.add(c_remark);
        
        while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "创建商品", JOptionPane.OK_CANCEL_OPTION)) {
            if (0 == c_arg0.getText().length()) {
                JOptionPane.showConfirmDialog(null, "必须要选择游戏账号", "服务器响应", JOptionPane.DEFAULT_OPTION);
                continue;
            }
            if (0 == c_arg1.getModel().getSize()) {
                JOptionPane.showConfirmDialog(null, "必须要选择租赁类型", "服务器响应", JOptionPane.DEFAULT_OPTION);
                continue;
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            JSONObject args = new JSONObject();
            args.put("oid", oid);
            if (0 < c_remark.getText().length()) args.put("remark", c_remark.getText());
            args.put("price", Float.parseFloat(i_price.getText()));
            args.put("count", 1);
            args.put("begin", sdf.format(new Date(System.currentTimeMillis())));
            args.put("arg0", c_arg0.getText().split(" ")[0].split("x")[1]);
            args.put("arg1", c_arg1.getSelectedItem().toString().contains("A") ? "A" : "B");
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_COMMODITY, args);
            JOptionPane.showConfirmDialog(null, rsp, "服务器响应", JOptionPane.DEFAULT_OPTION);
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
                    List<BeanGame> games = Service.map_game.values()
                            .stream()
                            .filter(game->{
                                int count = 0;
                                for (String word : words) if (game.c_name_zh.contains(word)) count++;
                                if (count == words.length) return true;
                                else return false;
                            }).collect(Collectors.toList());
                    List<BeanGameAccount> accounts = Service.set_game_account_game
                            .stream()
                            .filter(gag->{
                                for (BeanGame game : games) {
                                    if (game.i_gid == gag.i_gid) return true;
                                }
                                return false;
                            })
                            .map(gag->Service.map_game_account.get(gag.i_gaid))
                            .collect(Collectors.toList());
                    for (BeanGameAccount account : accounts) {
                        if (null == account) continue;
                        if (!isMatchCheckBox(type_a, type_b, celldata)) continue;
                        if (account.i_gaid == Integer.parseInt(celldata.split(" ")[0].split("x")[1], 16)) return true;
                    }
                    return false;
                }
                case "按用户名": {
                    List<BeanChannelAccount> users = Service.map_channel_account.values()
                            .stream()
                            .filter(user->{
                                int count1 = 0;
                                for (String word : words) if (user.c_user.contains(word)) count1++;
                                if (count1 == words.length) return true;
                                else return false;
                            }).collect(Collectors.toList());
                    List<BeanGameAccount> accounts = Service.set_game_account_rent
                            .stream()
                            .filter(rent->{
                                if (Service.RENT_STATE_RENT != rent.i_state) return false;
                                for (BeanChannelAccount user : users) {
                                    if (user.i_caid == rent.i_caid) return true;
                                }
                                return false;
                            })
                            .map(rent->Service.map_game_account.get(rent.i_gaid))
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
        Service.map_game_account.values().forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", account.i_gaid, account.c_user),
                    ( (Service.RENT_STATE_IDLE == Service.getRentStateByGameAccount(account.i_gaid, Service.RENT_TYPE_A) ? "[A:〇]" : "[A:●]")
                    + (Service.RENT_STATE_IDLE == Service.getRentStateByGameAccount(account.i_gaid, Service.RENT_TYPE_B) ? "[B:〇]" : "[B:●]")));
            cell.add(new JLabel(Service.getGameAccountGames(account.i_gaid).stream().map(game->game.c_name_zh).collect(Collectors.joining("; "))), BorderLayout.SOUTH);
            cell.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wrapper.obj = Service.map_game_account.get(account.i_gaid);
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