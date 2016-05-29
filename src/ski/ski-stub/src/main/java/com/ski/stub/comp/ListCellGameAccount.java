package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjEditLabel.EditListener;
import com.fomjar.widget.FjListCell;
import com.ski.common.SkiCommon;
import com.ski.stub.CommonUI;
import com.ski.stub.Service;
import com.ski.stub.bean.BeanGameAccount;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ListCellGameAccount extends FjListCell<BeanGameAccount> {
    
    private static final long serialVersionUID = 6352907380098820766L;
    
    private FjEditLabel c_user;
    private FjEditLabel c_pass;
    private FjEditLabel i_gaid;
    private FjEditLabel t_birth;
    private JButton     b_update_to_db;
    private JButton     b_update_to_all;
    private JButton     b_verify;
    private JButton     b_create;
    
    public ListCellGameAccount(BeanGameAccount data) {
        c_user  = new FjEditLabel();
        c_user.setForeground(color_major);
        c_pass  = new FjEditLabel();
        c_pass.setForeground(color_major);
        i_gaid  = new FjEditLabel(false);
        i_gaid.setForeground(color_minor);
        t_birth = new FjEditLabel();
        t_birth.setForeground(color_minor);
        b_update_to_db  = new JButton("更新到DB");
        b_update_to_db.setMargin(new Insets(0, 0, 0, 0));
        b_update_to_all  = new JButton("更新PS和DB");
        b_update_to_all.setMargin(new Insets(0, 0, 0, 0));
        b_verify        = new JButton("校验此账户");
        b_verify.setMargin(new Insets(0, 0, 0, 0));
        b_create        = new JButton("创建此账户");
        b_create.setMargin(new Insets(0, 0, 0, 0));
        
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        JPanel panel_center = new JPanel();
        panel_center.setOpaque(false);
        panel_center.setLayout(new GridLayout(2, 1));
        panel_center.add(c_user);
        panel_center.add(c_pass);
        JPanel panel_east = new JPanel();
        panel_east.setOpaque(false);
        panel_east.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        panel_east.setLayout(new GridLayout(2, 1));
        panel_east.add(i_gaid);
        panel_east.add(t_birth);
        panel.add(panel_center, BorderLayout.CENTER);
        panel.add(panel_east, BorderLayout.EAST);
        
        JPanel panel_button = new JPanel();
        panel_button.setLayout(new GridLayout(2, 2));
        panel_button.add(b_update_to_db);
        panel_button.add(b_update_to_all);
        panel_button.add(b_verify);
        panel_button.add(b_create);
        
        add(panel, BorderLayout.CENTER);
        add(panel_button, BorderLayout.EAST);
        
        passthroughMouseEvent(panel, this);
        
        setData(data);
        
        registerListener();
    }

    @Override
    public void setData(BeanGameAccount data) {
        super.setData(data);
        
        c_user.setText(data.c_user);
        c_pass.setText(data.c_pass_curr);
        i_gaid.setText(String.format("0x%08X", data.i_gaid));
        t_birth.setText(data.t_birth);
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
                c_user.setForeground(CommonUI.COLOR_MODIFYING);
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
                c_pass.setForeground(CommonUI.COLOR_MODIFYING);
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
                t_birth.setForeground(CommonUI.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        b_update_to_db.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        if (args.isEmpty()) {
                            JOptionPane.showConfirmDialog(ListCellGameAccount.this, "没有可更新的内容", "信息", JOptionPane.CLOSED_OPTION);
                            return;
                        }
                        b_update_to_db.setEnabled(false);
                        b_update_to_all.setEnabled(false);
                        b_verify.setEnabled(false);
                        b_create.setEnabled(false);
                        FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                        JOptionPane.showConfirmDialog(ListCellGameAccount.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.CLOSED_OPTION);
                        if (null != rsp && Service.isResponseSuccess(rsp)) {
                            if (args.has("user"))       c_user.setForeground(color_major);
                            if (args.has("pass_curr"))  c_pass.setForeground(color_major);
                            if (args.has("birth"))      t_birth.setForeground(color_minor);
                            args.clear();
                        }
                        b_update_to_db.setEnabled(true);
                        b_update_to_all.setEnabled(true);
                        b_verify.setEnabled(true);
                        b_create.setEnabled(true);
                    }
                }.start();
            }
        });
        b_update_to_all.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        if (args.isEmpty()) {
                            JOptionPane.showConfirmDialog(ListCellGameAccount.this, "没有可更新的内容", "信息", JOptionPane.CLOSED_OPTION);
                            return;
                        }
                        if (!args.has("pass")) {
                            JOptionPane.showConfirmDialog(ListCellGameAccount.this, "PlayStation网站上只可以更新密码", "错误", JOptionPane.CLOSED_OPTION);
                            return;
                        }
                        b_update_to_db.setEnabled(false);
                        b_update_to_all.setEnabled(false);
                        b_verify.setEnabled(false);
                        b_create.setEnabled(false);
                        FjDscpMessage rsp_wa = Service.send("wa", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                        if (!Service.isResponseSuccess(rsp_wa)) {
                            JOptionPane.showConfirmDialog(ListCellGameAccount.this, null != rsp_wa ? rsp_wa.toString() : null, "服务器响应", JOptionPane.CLOSED_OPTION);
                            b_update_to_db.setEnabled(true);
                            b_update_to_all.setEnabled(true);
                            b_verify.setEnabled(true);
                            b_create.setEnabled(true);
                            return;
                        }
                        FjDscpMessage rsp_cdb = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                        if (!Service.isResponseSuccess(rsp_cdb)) {
                            JOptionPane.showConfirmDialog(ListCellGameAccount.this, null != rsp_cdb ? rsp_cdb.toString() : null, "服务器响应", JOptionPane.CLOSED_OPTION);
                            b_update_to_db.setEnabled(true);
                            b_update_to_all.setEnabled(true);
                            b_verify.setEnabled(true);
                            b_create.setEnabled(true);
                            return;
                        }
                        
                        if (args.has("user"))   c_user.setForeground(color_major);
                        if (args.has("pass"))   c_pass.setForeground(color_major);
                        if (args.has("birth"))  t_birth.setForeground(color_minor);
                        args.clear();
                        b_update_to_db.setEnabled(true);
                        b_update_to_all.setEnabled(true);
                        b_verify.setEnabled(true);
                        b_create.setEnabled(true);
                    }
                }.start();
            }
        });
        b_verify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        b_update_to_db.setEnabled(false);
                        b_update_to_all.setEnabled(false);
                        b_verify.setEnabled(false);
                        b_create.setEnabled(false);
                        args.put("user", c_user.getText());
                        args.put("pass", c_pass.getText());
                        FjDscpMessage rsp = Service.send("wa", SkiCommon.ISIS.INST_ECOM_VERIFY_ACCOUNT, args);
                        JOptionPane.showConfirmDialog(ListCellGameAccount.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.CLOSED_OPTION);
                        if (null != rsp && Service.isResponseSuccess(rsp)) args.clear();
                        b_update_to_db.setEnabled(true);
                        b_update_to_all.setEnabled(true);
                        b_verify.setEnabled(true);
                        b_create.setEnabled(true);
                    }
                }.start();
            }
        });
        b_create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showConfirmDialog(ListCellGameAccount.this, "暂未开放", "信息", JOptionPane.CLOSED_OPTION);
            }
        });
        
    }
}
