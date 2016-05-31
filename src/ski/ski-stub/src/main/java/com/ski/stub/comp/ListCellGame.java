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
import com.ski.stub.UIToolkit;
import com.ski.stub.Service;
import com.ski.stub.bean.BeanGame;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ListCellGame extends FjListCell<BeanGame> {
    
    private static final long serialVersionUID = 6352907380098820766L;
    
    private FjEditLabel c_name_zh;
    private FjEditLabel t_sale;
    private FjEditLabel i_gid;
    private FjEditLabel c_country;
    private JButton     b_update;
    
    public ListCellGame(BeanGame data) {
        super(data);
        
        c_name_zh   = new FjEditLabel(data.c_name_zh);
        c_name_zh.setForeground(color_major);
        t_sale      = new FjEditLabel(data.t_sale);
        t_sale.setForeground(color_major);
        i_gid       = new FjEditLabel(String.format("0x%08X", data.i_gid), false);
        i_gid.setForeground(color_minor);
        c_country   = new FjEditLabel(data.c_country);
        c_country.setForeground(color_minor);
        b_update    = new JButton("更新");
        b_update.setMargin(new Insets(0, 0, 0, 0));
        
        JPanel panel_center = new JPanel();
        panel_center.setOpaque(false);
        panel_center.setLayout(new GridLayout(2, 1));
        panel_center.add(c_name_zh);
        panel_center.add(t_sale);
        
        JPanel panel_east = new JPanel();
        panel_east.setOpaque(false);
        panel_east.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        panel_east.setLayout(new GridLayout(2, 1));
        panel_east.add(i_gid);
        panel_east.add(c_country);
        
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        panel.add(panel_center, BorderLayout.CENTER);
        panel.add(panel_east, BorderLayout.EAST);
        
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(b_update, BorderLayout.EAST);
        
        passthroughMouseEvent(panel);
        
        registerListener();
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
                c_name_zh.setForeground(UIToolkit.COLOR_MODIFYING);
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
                t_sale.setForeground(UIToolkit.COLOR_MODIFYING);
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
                c_country.setForeground(UIToolkit.COLOR_MODIFYING);
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
                            JOptionPane.showConfirmDialog(ListCellGame.this, "没有可更新的内容", "信息", JOptionPane.DEFAULT_OPTION);
                            return;
                        }
                        b_update.setEnabled(false);
                        FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME, args);
                        JOptionPane.showConfirmDialog(ListCellGame.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
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