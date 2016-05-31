package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjEditLabel.EditListener;
import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.ski.common.SkiCommon;
import com.ski.stub.Service;
import com.ski.stub.UIToolkit;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageGameAccount extends JDialog {

    private static final long serialVersionUID = -51034836551447291L;
    
    private final BeanGameAccount account;
    private JToolBar    toolbar;
    private FjEditLabel i_gaid;
    private FjEditLabel c_user;
    private FjEditLabel c_pass;
    private FjEditLabel t_birth;
    private FjListPane<String> list;
    
    public ManageGameAccount(Window window, BeanGameAccount account) {
        super(window, "管理账号“" + account.c_user + "”");
        
        this.account = account;
        toolbar = new JToolBar();
        toolbar.add(new JButton("添加游戏"));
        toolbar.add(new JButton("更新到DB"));
        toolbar.add(new JButton("更新到DB和PS"));
        toolbar.add(new JButton("校验此账户"));
        toolbar.setFloatable(false);
        i_gaid = new FjEditLabel(String.format("0x%08X", account.i_gaid), false);
        c_user = new FjEditLabel(account.c_user);
        c_pass = new FjEditLabel(account.c_pass_curr);
        t_birth = new FjEditLabel(account.t_birth);
        
        JPanel labels = new JPanel();
        labels.setBorder(BorderFactory.createTitledBorder("基本信息"));
        labels.setLayout(new GridLayout(4, 1));
        labels.add(createBasicInfoLabel("I  D", i_gaid));
        labels.add(createBasicInfoLabel("账号", c_user));
        labels.add(createBasicInfoLabel("密码", c_pass));
        labels.add(createBasicInfoLabel("生日", t_birth));
        
        list = new FjListPane<String>();
        list.setBorder(BorderFactory.createTitledBorder("拥有的游戏"));
        
        JPanel panel_center = new JPanel();
        panel_center.setLayout(new BoxLayout(panel_center, BoxLayout.Y_AXIS));
        panel_center.add(labels);
        panel_center.add(list);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(panel_center, BorderLayout.CENTER);
        
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 300));
        setLocation(window.getX() - (getWidth() - window.getWidth()) / 2, window.getY() - (getHeight() - window.getHeight()) / 2);
        
        registerListener();
        
        updateAll();
    }
    
    private JSONObject args = new JSONObject();
    
    private void registerListener() {
        ActionMap am = getRootPane().getActionMap();
        am.put("dispose", new AbstractAction() {
            private static final long serialVersionUID = 4074354978669029364L;
            @Override
            public void actionPerformed(ActionEvent e) {ManageGameAccount.this.dispose();}
        });
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "dispose");
        
        c_user.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gaid", Integer.parseInt(i_gaid.getText().split("x")[1], 16));
                args.put("user", new_value);
                c_user.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_pass.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gaid", Integer.parseInt(i_gaid.getText().split("x")[1], 16));
                args.put("pass_curr", new_value);   // for cdb
                args.put("user", c_user.getText()); // for wa
                args.put("pass", old_value);        // for wa
                args.put("pass_new", new_value);    // for wa
                c_pass.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        t_birth.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gaid", Integer.parseInt(i_gaid.getText().split("x")[1], 16));
                args.put("birth", new_value);
                t_birth.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        ((JButton) toolbar.getComponent(0)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BeanGame game = UIToolkit.chooseGame(ManageGameAccount.this);
                if (null == game) return;
                
                int gaid = account.i_gaid;
                int gid  = game.i_gid;
                JSONObject args = new JSONObject();
                args.put("gaid", gaid);
                args.put("gid", gid);
                FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT_GAME, args);
                JOptionPane.showConfirmDialog(ManageGameAccount.this, rsp.toString(), "服务器响应", JOptionPane.DEFAULT_OPTION);
                
                Service.updateGameAccountGame();
                updateAll();
            }
        });
        ((JButton) toolbar.getComponent(1)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Service.doLater(new Runnable() {
                    @Override
                    public void run() {
                        if (args.isEmpty()) {
                            JOptionPane.showConfirmDialog(ManageGameAccount.this, "没有可更新的内容", "信息", JOptionPane.DEFAULT_OPTION);
                            return;
                        }
                        ((JButton) toolbar.getComponent(1)).setEnabled(false);
                        FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                        JOptionPane.showConfirmDialog(ManageGameAccount.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                        if (null != rsp && Service.isResponseSuccess(rsp)) {
                            if (args.has("user"))       c_user.setForeground(Color.darkGray);
                            if (args.has("pass_curr"))  c_pass.setForeground(Color.darkGray);
                            if (args.has("birth"))      t_birth.setForeground(Color.darkGray);
                            args.clear();
                        }
                        ((JButton) toolbar.getComponent(1)).setEnabled(true);
                    }
                });
            }
        });
        ((JButton) toolbar.getComponent(2)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Service.doLater(new Runnable() {
                    @Override
                    public void run() {
                        if (args.isEmpty()) {
                            JOptionPane.showConfirmDialog(ManageGameAccount.this, "没有可更新的内容", "信息", JOptionPane.DEFAULT_OPTION);
                            return;
                        }
                        if (!args.has("pass")) {
                            JOptionPane.showConfirmDialog(ManageGameAccount.this, "PlayStation网站上只可以更新密码", "错误", JOptionPane.DEFAULT_OPTION);
                            return;
                        }
                        ((JButton) toolbar.getComponent(2)).setEnabled(false);
                        FjDscpMessage rsp_wa = Service.send("wa", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                        if (!Service.isResponseSuccess(rsp_wa)) {
                            JOptionPane.showConfirmDialog(ManageGameAccount.this, null != rsp_wa ? rsp_wa.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                            ((JButton) toolbar.getComponent(2)).setEnabled(true);
                            return;
                        }
                        FjDscpMessage rsp_cdb = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                        if (!Service.isResponseSuccess(rsp_cdb)) {
                            JOptionPane.showConfirmDialog(ManageGameAccount.this, null != rsp_cdb ? rsp_cdb.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                            ((JButton) toolbar.getComponent(2)).setEnabled(true);
                            return;
                        }
                        
                        if (args.has("user"))   c_user.setForeground(Color.darkGray);
                        if (args.has("pass"))   c_pass.setForeground(Color.darkGray);
                        if (args.has("birth"))  t_birth.setForeground(Color.darkGray);
                        args.clear();
                        ((JButton) toolbar.getComponent(2)).setEnabled(true);
                    }
                });
            }
        });
        ((JButton) toolbar.getComponent(3)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Service.doLater(new Runnable() {
                    @Override
                    public void run() {
                        ((JButton) toolbar.getComponent(3)).setEnabled(false);
                        args.put("user", c_user.getText());
                        args.put("pass", c_pass.getText());
                        FjDscpMessage rsp = Service.send("wa", SkiCommon.ISIS.INST_ECOM_VERIFY_ACCOUNT, args);
                        JOptionPane.showConfirmDialog(ManageGameAccount.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                        if (null != rsp && Service.isResponseSuccess(rsp)) args.clear();
                        ((JButton) toolbar.getComponent(3)).setEnabled(true);
                    }
                });
            }
        });
    }
    
    private void updateAll() {
        list.getList().removeAllCell();
        Service.set_game_account_game.forEach(pair->{
            if (account.i_gaid == pair.i_gaid) {
                BeanGame game = Service.map_game.get(pair.i_gid);
                list.getList().addCell(new FjListCellString(String.format("0x%08X - %s", game.i_gid, game.c_name_zh)));
            }
        });
    }
    
    private static JPanel createBasicInfoLabel(String label, FjEditLabel field) {
        JLabel jlabel = new JLabel(label);
        jlabel.setPreferredSize(new Dimension(80, 0));
        
        JPanel jpanel = new JPanel();
        jpanel.setLayout(new BorderLayout());
        jpanel.add(jlabel, BorderLayout.WEST);
        jpanel.add(field, BorderLayout.CENTER);
        
        return jpanel;
    }
    
}
