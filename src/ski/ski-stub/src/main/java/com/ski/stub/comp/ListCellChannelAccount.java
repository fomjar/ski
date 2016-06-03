package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjListCell;
import com.fomjar.widget.FjEditLabel.EditListener;
import com.ski.common.SkiCommon;
import com.ski.stub.Service;
import com.ski.stub.UIToolkit;
import com.ski.stub.bean.BeanChannelAccount;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ListCellChannelAccount extends FjListCell<BeanChannelAccount> {

    private static final long serialVersionUID = 6691136971313150684L;
    
    private FjEditLabel i_channel;
    private FjEditLabel c_user;
    private FjEditLabel c_phone;
    private FjEditLabel i_caid;
    private JButton     b_update;

    public ListCellChannelAccount(BeanChannelAccount data) {
        super(data);
        
        i_channel = new FjEditLabel(0 == data.i_channel ? "[淘宝]" : 1 == data.i_channel ? "[微信]" : 2 == data.i_channel ? "[支付宝]" : "[未知]");
        i_channel.setForeground(color_major);
        i_channel.setFont(i_channel.getFont().deriveFont(Font.ITALIC));
        c_user    = new FjEditLabel(data.c_user);
        c_user.setForeground(color_major);
        c_user.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        c_phone   = new FjEditLabel(data.c_phone);
        c_phone.setForeground(color_major);
        i_caid    = new FjEditLabel(String.format("0x%08X", data.i_caid), false);
        i_caid.setForeground(color_minor);
        b_update  = new JButton("更新");
        b_update.setMargin(new Insets(0, 0, 0, 0));
        
        JPanel panel_center1 = new JPanel();
        panel_center1.setOpaque(false);
        panel_center1.setLayout(new BorderLayout());
        panel_center1.add(i_channel, BorderLayout.WEST);
        panel_center1.add(c_user, BorderLayout.CENTER);
        
        JPanel panel_center0 = new JPanel();
        panel_center0.setOpaque(false);
        panel_center0.setLayout(new GridLayout(2, 1));
        panel_center0.add(panel_center1);
        panel_center0.add(c_phone);
        
        JPanel panel_east0 = new JPanel();
        panel_east0.setOpaque(false);
        panel_east0.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        panel_east0.setLayout(new GridLayout(2, 1));
        panel_east0.add(i_caid);
        
        JPanel panel_center = new JPanel();
        panel_center.setOpaque(false);
        panel_center.setLayout(new BorderLayout());
        panel_center.add(panel_center0, BorderLayout.CENTER);
        panel_center.add(panel_east0, BorderLayout.EAST);
        
        setLayout(new BorderLayout());
        add(panel_center, BorderLayout.CENTER);
        add(b_update, BorderLayout.EAST);
        
        passthroughMouseEvent(panel_center);
        
        registerListener();
    }
    
        
    private JSONObject args = new JSONObject();
        
    private void registerListener() {
        i_channel.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", Integer.parseInt(i_caid.getText().split("x")[1], 16));
                if (i_channel.getText().contains("淘宝")) args.put("channel", 0);
                if (i_channel.getText().contains("微信")) args.put("channel", 1);
                i_channel.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_user.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", Integer.parseInt(i_caid.getText().split("x")[1], 16));
                args.put("user", c_user.getText());
                c_user.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_phone.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", Integer.parseInt(i_caid.getText().split("x")[1], 16));
                args.put("phone", c_phone.getText());
                c_phone.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        b_update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        if (args.isEmpty()) {
                            JOptionPane.showConfirmDialog(ListCellChannelAccount.this, "没有可更新的内容", "信息", JOptionPane.DEFAULT_OPTION);
                            return;
                        }
                        b_update.setEnabled(false);
                        FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args);
                        JOptionPane.showConfirmDialog(ListCellChannelAccount.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                        if (null != rsp && Service.isResponseSuccess(rsp)) {
                            if (args.has("channel"))    i_channel.setForeground(color_major);
                            if (args.has("user"))       c_user.setForeground(color_major);
                            if (args.has("phone"))      c_phone.setForeground(color_major);
                            args.clear();
                        }
                        b_update.setEnabled(true);
                    }
                }.start();
            }
        });
    }
}
