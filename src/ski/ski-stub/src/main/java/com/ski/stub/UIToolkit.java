package com.ski.stub;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.fomjar.widget.FjTextField;
import com.ski.common.SkiCommon;
import com.ski.stub.bean.BeanGame;

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
    }
    
    public static FjDscpMessage createGame(Window owner) {
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
        
        int option = JOptionPane.showConfirmDialog(owner, panel, "创建游戏", JOptionPane.OK_CANCEL_OPTION);
        if (JOptionPane.OK_OPTION == option) {
            JSONObject args = new JSONObject();
            if (0 != c_country.getText().length())  args.put("country", c_country.getText());
            if (0 != t_sale.getText().length())     args.put("sale",    t_sale.getText());
            if (0 != c_name_zh.getText().length())  args.put("name_zh", c_name_zh.getText());
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME, args);
            JOptionPane.showConfirmDialog(owner, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
            return rsp;
        }
        
        return null;
    }
    
    public static FjDscpMessage createGameAccount(Window owner) {
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
        
        int option = JOptionPane.showConfirmDialog(owner, panel, "创建游戏账号", JOptionPane.OK_CANCEL_OPTION);
        if (JOptionPane.OK_OPTION == option) {
            JSONObject args = new JSONObject();
            if (0 != c_user.getText().length())     args.put("user",        c_user.getText());
            if (0 != c_pass.getText().length())     args.put("pass_curr",   c_pass.getText());
            if (0 != t_birth.getText().length())    args.put("birth",       t_birth.getText());
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
            JOptionPane.showConfirmDialog(owner, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
            return rsp;
        }
        return null;
    }
    
    public static FjDscpMessage createChannelAccount(Window owner) {
        JComboBox<String>   i_channel = new JComboBox<String>(new String[] {"淘宝", "微信"});
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
        
        int option = JOptionPane.showConfirmDialog(owner, panel, "创建用户", JOptionPane.OK_CANCEL_OPTION);
        if (JOptionPane.OK_OPTION == option) {
            JSONObject args = new JSONObject();
            args.put("gender", 1);
            args.put("channel", i_channel.getSelectedIndex());
            if (0 != c_user.getText().length())     args.put("user",    c_user.getText());
            if (0 != c_phone.getText().length())    args.put("phone",   c_phone.getText());
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args);
            JOptionPane.showConfirmDialog(owner, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
            return rsp;
        }
        return null;
    }
    
    public static BeanGame chooseGame(Window owner) {
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
        JDialog dialog = new JDialog(owner, "选择游戏");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 500);
        dialog.setLocation(owner.getX() - (dialog.getWidth() - owner.getWidth()) / 2,
                owner.getY() - (dialog.getHeight() - owner.getHeight()) / 2);
        ActionMap am = dialog.getRootPane().getActionMap();
        am.put("dispose", new AbstractAction() {
            private static final long serialVersionUID = -2394322596485584371L;
            @Override
            public void actionPerformed(ActionEvent e) {dialog.dispose();}
        });
        dialog.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "dispose");
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.add(pane, BorderLayout.CENTER);
        
        Wrapper<BeanGame> wrapper = new Wrapper<BeanGame>();
        
        // 添加游戏列表
        Service.map_game.values().forEach(game->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", game.i_gid, game.c_name_zh));
            cell.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int gid  = Integer.parseInt(cell.getData().split(" ")[0].split("x")[1], 16);
                    wrapper.obj = Service.map_game.get(gid);
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