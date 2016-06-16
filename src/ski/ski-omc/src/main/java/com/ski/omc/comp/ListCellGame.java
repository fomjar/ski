package com.ski.omc.comp;

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
import com.ski.omc.MainFrame;
import com.ski.omc.Service;
import com.ski.omc.UIToolkit;
import com.ski.omc.bean.BeanGame;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ListCellGame extends FjListCell<BeanGame> {
    
    private static final long serialVersionUID = 6352907380098820766L;
    
    private FjEditLabel c_name_zh;
    private FjEditLabel i_gid;
    private FjEditLabel i_price_a;
    private FjEditLabel i_price_b;
    private FjEditLabel t_sale;
    private FjEditLabel c_country;
    private JButton     b_update;
    
    public ListCellGame(BeanGame data) {
        super(data);
        
        c_name_zh   = new FjEditLabel(data.c_name_zh);
        c_name_zh.setForeground(color_major);
        i_gid       = new FjEditLabel(String.format("0x%08X", data.i_gid), false);
        i_gid.setForeground(color_minor);
        String keya = Integer.toHexString(data.i_gid) + Service.RENT_TYPE_A;
        String keyb = Integer.toHexString(data.i_gid) + Service.RENT_TYPE_B;
        i_price_a   = new FjEditLabel("A: " + (Service.map_game_rent_price.containsKey(keya) ? Service.map_game_rent_price.get(keya).i_price : 0.0f) + "元/天");
        i_price_a.setForeground(color_major);
        i_price_b   = new FjEditLabel("B: " + (Service.map_game_rent_price.containsKey(keyb) ? Service.map_game_rent_price.get(keyb).i_price : 0.0f) + "元/天");
        i_price_b.setForeground(color_major);
        t_sale      = new FjEditLabel(0 == data.t_sale.length() ? "(没有发售时间)" : data.t_sale);
        t_sale.setForeground(color_minor);
        c_country   = new FjEditLabel(0 == data.c_country.length() ? "(没有国家)" : data.c_country);
        c_country.setForeground(color_minor);
        b_update    = new JButton("更新");
        b_update.setMargin(new Insets(0, 0, 0, 0));
        
        JPanel panel0 = new JPanel();
        panel0.setOpaque(false);
        panel0.setLayout(new BorderLayout());
        panel0.add(c_name_zh, BorderLayout.CENTER);
        panel0.add(i_gid, BorderLayout.EAST);
        
        JPanel panel1 = new JPanel();
        panel1.setOpaque(false);
        panel1.setLayout(new GridLayout(1, 4));
        panel1.add(i_price_a);
        panel1.add(i_price_b);
        panel1.add(t_sale);
        panel1.add(c_country);
        
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        panel.setLayout(new GridLayout(2, 1));
        panel.add(panel0);
        panel.add(panel1);
        
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
        i_price_a.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                if (new_value.contains("A:")) new_value = new_value.replace("A:", "");
                if (new_value.contains("元/天")) new_value = new_value.replace("元/天", "");
                new_value = new_value.trim();
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("type", Service.RENT_TYPE_A);
                args.put("price", Float.parseFloat(new_value));
                FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_RENT_PRICE, args);
                JOptionPane.showConfirmDialog(ListCellGame.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                args.clear();
                
                MainFrame.getInstance().updateAll();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        i_price_b.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                if (new_value.contains("B:")) new_value = new_value.replace("B:", "");
                if (new_value.contains("元/天")) new_value = new_value.replace("元/天", "");
                new_value = new_value.trim();
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("type", Service.RENT_TYPE_B);
                args.put("price", Float.parseFloat(new_value));
                FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_RENT_PRICE, args);
                JOptionPane.showConfirmDialog(ListCellGame.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                args.clear();
                
                MainFrame.getInstance().updateAll();
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
                if (args.isEmpty()) {
                    JOptionPane.showConfirmDialog(ListCellGame.this, "没有可更新的内容", "信息", JOptionPane.DEFAULT_OPTION);
                    return;
                }
                if (3 < args.size() || !args.containsKey("price")) {
                    FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME, args);
                    JOptionPane.showConfirmDialog(ListCellGame.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                    if (null != rsp && Service.isResponseSuccess(rsp)) {
                        if (args.has("name_zh"))    c_name_zh.setForeground(color_major);
                        if (args.has("sale"))       t_sale.setForeground(color_minor);
                        if (args.has("country"))    c_country.setForeground(color_minor);
                        args.clear();
                    }
                }
                if (args.containsKey("price")) {
                    FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_RENT_PRICE, args);
                    JOptionPane.showConfirmDialog(ListCellGame.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                    if (null != rsp && Service.isResponseSuccess(rsp)) {
                        if (args.has("price")) i_price_a.setForeground(color_major);
                        args.clear();
                    }
                }
            }
        });
    }
    
}