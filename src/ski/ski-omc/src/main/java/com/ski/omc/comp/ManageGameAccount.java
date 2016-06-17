package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjEditLabel.EditListener;
import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.ski.common.SkiCommon;
import com.ski.omc.Service;
import com.ski.omc.UIToolkit;
import com.ski.omc.bean.BeanGame;
import com.ski.omc.bean.BeanGameAccount;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageGameAccount extends JDialog {

    private static final long serialVersionUID = -51034836551447291L;
    
    private JToolBar    toolbar;
    private FjEditLabel i_gaid;
    private FjEditLabel c_user;
    private FjEditLabel c_pass;
    private FjEditLabel t_birth;
    private FjListPane<String> listpane;
    
    public ManageGameAccount(int gaid) {
        BeanGameAccount account = Service.map_game_account.get(gaid);
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("更新到DB"));
        toolbar.add(new JButton("更新到DB和PS"));
        toolbar.add(new JButton("测试账号"));
        toolbar.addSeparator();
        toolbar.add(new JButton("添加游戏"));
        i_gaid = new FjEditLabel(String.format("0x%08X", account.i_gaid), false);
        c_user = new FjEditLabel(account.c_user);
        c_pass = new FjEditLabel(account.c_pass_curr);
        t_birth = new FjEditLabel(account.t_birth);
        
        listpane = new FjListPane<String>();
        listpane.setBorder(BorderFactory.createTitledBorder(listpane.getBorder(), "包含游戏"));
        
        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createTitledBorder("基本信息"));
        panel_basic.setLayout(new GridLayout(4, 1));
        panel_basic.add(UIToolkit.createBasicInfoLabel("GAID", i_gaid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("账号", c_user));
        panel_basic.add(UIToolkit.createBasicInfoLabel("密码", c_pass));
        panel_basic.add(UIToolkit.createBasicInfoLabel("生日", t_birth));
        
        JPanel panel_center = new JPanel();
        panel_center.setLayout(new BorderLayout());
        panel_center.add(panel_basic, BorderLayout.NORTH);
        panel_center.add(listpane, BorderLayout.CENTER);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(panel_center, BorderLayout.CENTER);
        
        setTitle(String.format("管理账号“%s”", account.c_user));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 300));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        registerListener();
        
        updateGameAccountGame();
    }
    
    private JSONObject args = new JSONObject();
    
    private void registerListener() {
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
                if (args.isEmpty()) {
                    JOptionPane.showConfirmDialog(ManageGameAccount.this, "没有可更新的内容", "信息", JOptionPane.DEFAULT_OPTION);
                    return;
                }
                ((JButton) toolbar.getComponent(0)).setEnabled(false);
                FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                JOptionPane.showConfirmDialog(ManageGameAccount.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                if (null != rsp && Service.isResponseSuccess(rsp)) {
                    if (args.has("user"))       c_user.setForeground(Color.darkGray);
                    if (args.has("pass_curr"))  c_pass.setForeground(Color.darkGray);
                    if (args.has("birth"))      t_birth.setForeground(Color.darkGray);
                    args.clear();
                }
                ((JButton) toolbar.getComponent(0)).setEnabled(true);
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
                        if (!args.has("pass")) {
                            JOptionPane.showConfirmDialog(ManageGameAccount.this, "PlayStation网站上只可以更新密码", "错误", JOptionPane.DEFAULT_OPTION);
                            return;
                        }
                        ((JButton) toolbar.getComponent(1)).setEnabled(false);
                        FjDscpMessage rsp_wa = Service.send("wa", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                        JOptionPane.showConfirmDialog(ManageGameAccount.this, null != rsp_wa ? rsp_wa.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                        if (Service.isResponseSuccess(rsp_wa)) {
                            FjDscpMessage rsp_cdb = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                            JOptionPane.showConfirmDialog(ManageGameAccount.this, null != rsp_cdb ? rsp_cdb.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                            
                            if (args.has("user"))   c_user.setForeground(Color.darkGray);
                            if (args.has("pass"))   c_pass.setForeground(Color.darkGray);
                            if (args.has("birth"))  t_birth.setForeground(Color.darkGray);
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
                        ((JButton) toolbar.getComponent(2)).setEnabled(false);
                        args.put("user", c_user.getText());
                        args.put("pass", c_pass.getText());
                        FjDscpMessage rsp = Service.send("wa", SkiCommon.ISIS.INST_ECOM_VERIFY_ACCOUNT, args);
                        JOptionPane.showConfirmDialog(ManageGameAccount.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                        if (null != rsp && Service.isResponseSuccess(rsp)) args.clear();
                        ((JButton) toolbar.getComponent(2)).setEnabled(true);
                    }
                });
            }
        });
        ((JButton) toolbar.getComponent(4)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BeanGame game = UIToolkit.chooseGame();
                if (null == game) return;
                
                if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(ManageGameAccount.this, "即将添加游戏：" + game.c_name_zh, "信息", JOptionPane.OK_CANCEL_OPTION))
                    return;
                
                int gaid = Integer.parseInt(i_gaid.getText().split("x")[1], 16);
                int gid  = game.i_gid;
                JSONObject args = new JSONObject();
                args.put("gaid", gaid);
                args.put("gid", gid);
                FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT_GAME, args);
                JOptionPane.showConfirmDialog(ManageGameAccount.this, rsp.toString(), "服务器响应", JOptionPane.DEFAULT_OPTION);
                
                Service.updateGameAccountGame();
                updateGameAccountGame();
            }
        });
    }
    
    private void updateGameAccountGame() {
        listpane.getList().removeAllCell();
        int gaid = Integer.parseInt(i_gaid.getText().split("x")[1], 16);
        Service.set_game_account_game.forEach(bean->{
            if (gaid == bean.i_gaid) {
                BeanGame game = Service.map_game.get(bean.i_gid);
                listpane.getList().addCell(new FjListCellString(String.format("0x%08X - %s", game.i_gid, game.c_name_zh)));
            }
        });
    }
    
}
