package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjEditLabel.EditListener;
import com.fomjar.widget.FjListCell;
import com.ski.common.SkiCommon;
import com.ski.stub.CommonUI;
import com.ski.stub.Service;
import com.ski.stub.bean.BeanGame;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ListCellGame extends FjListCell<BeanGame> {
    
    private static final long serialVersionUID = 6352907380098820766L;
    
    private FjEditLabel c_name_zh;
    private FjEditLabel i_gid;
    private FjEditLabel t_sale;
    private FjEditLabel c_country;
    private JButton     b_update;
    
    public ListCellGame(BeanGame data) {
        c_name_zh   = new FjEditLabel();
        c_name_zh.setFont(c_name_zh.getFont().deriveFont(18.0f));
        c_name_zh.setForeground(color_major);
        i_gid       = new FjEditLabel(false);
        i_gid.setForeground(color_minor);
        t_sale      = new FjEditLabel();
        t_sale.setForeground(color_minor);
        c_country   = new FjEditLabel();
        c_country.setForeground(color_minor);
        b_update    = new JButton("更新");
        b_update.setMargin(new Insets(0, 0, 0, 0));
        
        setLayout(new BorderLayout());
        add(c_name_zh, BorderLayout.CENTER);
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        panel.add(c_name_zh, BorderLayout.CENTER);
        JPanel panel_south = new JPanel();
        panel_south.setOpaque(false);
        panel_south.setLayout(new GridLayout(1, 3));
        panel_south.add(i_gid);
        panel_south.add(t_sale);
        panel_south.add(c_country);
        panel.add(panel_south, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);
        add(b_update, BorderLayout.EAST);
        
        passthroughMouseEvent(panel, this);
        
        setData(data);
        
        registerListener();
    }

    @Override
    public void setData(BeanGame value) {
        c_name_zh.setText(value.c_name_zh);
        i_gid.setText(String.format("0x%08X", value.i_gid));
        t_sale.setText(value.t_sale);
        c_country.setText(value.c_country);
    }
    
    private JSONObject args = new JSONObject();
    
    private void registerListener() {
        c_name_zh.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("name_zh", new_value);
                c_name_zh.setForeground(CommonUI.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        t_sale.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("sale", new_value);
                t_sale.setForeground(CommonUI.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_country.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("country", new_value);
                c_country.setForeground(CommonUI.COLOR_MODIFYING);
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
                            JOptionPane.showConfirmDialog(ListCellGame.this, "没有可更新的内容", "信息", JOptionPane.CLOSED_OPTION);
                            return;
                        }
                        b_update.setEnabled(false);
                        FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME, args);
                        JOptionPane.showConfirmDialog(ListCellGame.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.CLOSED_OPTION);
                        if (null != rsp && Service.isResponseSuccess(rsp)) {
                            if (args.has("name_zh"))    c_name_zh.setForeground(color_major);
                            if (args.has("sale"))       t_sale.setForeground(color_minor);
                            if (args.has("country"))    c_country.setForeground(color_minor);
                            args.clear();
                        }
                        b_update.setEnabled(true);
                    }
                }.start();
            }
        });
    }
    
}
