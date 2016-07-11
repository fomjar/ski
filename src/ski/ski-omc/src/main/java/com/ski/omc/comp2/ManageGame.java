package com.ski.omc.comp2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjTextField;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanGame;
import com.ski.omc.UIToolkit;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageGame extends JDialog {

    private static final long serialVersionUID = 6823417802672054421L;
    
    private JToolBar    toolbar;
    private FjEditLabel i_gid;
    private FjEditLabel c_platform;
    private FjEditLabel c_country;
    private FjEditLabel c_url_icon;
    private FjEditLabel c_url_poster;
    private FjEditLabel c_url_buy;
    private FjEditLabel t_sale;
    private FjEditLabel c_name_zh;
    private FjEditLabel c_name_en;
    private FjEditLabel i_price_a;
    private FjEditLabel i_price_b;
    private JPanel      pane_tag;
    
    public ManageGame(int gid) {
        super(MainFrame2.getInstance());
        
        BeanGame game = CommonService.getGameByGid(gid);
        
        toolbar         = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("更新"));
        toolbar.add(new JButton("加标签"));
        
        i_gid           = new FjEditLabel(String.format("0x%08X", game.i_gid), false);
        c_platform      = new FjEditLabel(0 == game.c_platform.length() ? "(没有平台)" : game.c_platform);
        c_country       = new FjEditLabel(0 == game.c_country.length() ? "(没有国家)" : game.c_country);
        c_url_icon      = new FjEditLabel(0 == game.c_url_icon.length() ? "(没有链接)" : game.c_url_icon);
        c_url_poster    = new FjEditLabel(0 == game.c_url_poster.length() ? "(没有链接)" : game.c_url_poster);
        c_url_buy       = new FjEditLabel(0 == game.c_url_buy.length() ? "(没有链接)" : game.c_url_buy);
        t_sale          = new FjEditLabel(0 == game.t_sale.length() ? "(没有发售时间)" : game.t_sale);
        c_name_zh       = new FjEditLabel(0 == game.c_name_zh.length() ? "(没有中文名)" : game.c_name_zh);
        c_name_en       = new FjEditLabel(0 == game.c_name_en.length() ? "(没有英文名)" : game.c_name_en);
        
        i_price_a       = new FjEditLabel();
        i_price_b       = new FjEditLabel();
        
        pane_tag        = new JPanel();
        pane_tag.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "TAG"));
        pane_tag.setLayout(new FlowLayout(FlowLayout.LEADING));
        
        JPanel pane_basic = new JPanel();
        pane_basic.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "基本信息"));
        pane_basic.setLayout(new GridLayout(9, 1));
        pane_basic.add(UIToolkit.createBasicInfoLabel("游戏编号", i_gid));
        pane_basic.add(UIToolkit.createBasicInfoLabel("游戏平台", c_platform));
        pane_basic.add(UIToolkit.createBasicInfoLabel("国    家", c_country));
        pane_basic.add(UIToolkit.createBasicInfoLabel("图标链接", c_url_icon, "打开", e->{try{Desktop.getDesktop().browse(new URI(c_url_icon.getText()));}catch(URISyntaxException|IOException e1) {e1.printStackTrace();}}));
        pane_basic.add(UIToolkit.createBasicInfoLabel("海报链接", c_url_poster, "打开", e->{try{Desktop.getDesktop().browse(new URI(c_url_poster.getText()));}catch(URISyntaxException|IOException e1) {e1.printStackTrace();}}));
        pane_basic.add(UIToolkit.createBasicInfoLabel("购买链接", c_url_buy, "打开", e->{try{Desktop.getDesktop().browse(new URI(c_url_buy.getText()));}catch(URISyntaxException|IOException e1) {e1.printStackTrace();}}));
        pane_basic.add(UIToolkit.createBasicInfoLabel("发售日期", t_sale));
        pane_basic.add(UIToolkit.createBasicInfoLabel("中 文 名", c_name_zh));
        pane_basic.add(UIToolkit.createBasicInfoLabel("英 文 名", c_name_en));
        
        JPanel pane_price = new JPanel();
        pane_price.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "价格管理"));
        pane_price.setLayout(new GridLayout(2, 1));
        pane_price.add(UIToolkit.createBasicInfoLabel("A 类租赁", i_price_a));
        pane_price.add(UIToolkit.createBasicInfoLabel("B 类租赁", i_price_b));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(pane_basic, BorderLayout.CENTER);
        panel.add(pane_price, BorderLayout.SOUTH);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(pane_tag, BorderLayout.CENTER);
        
        setTitle(String.format("管理游戏 - %s", game.c_name_zh));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 440));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        registerListener();
        
        updatePrice();
        updateTag();
    }
    
    private JSONObject args = new JSONObject();
    
    private void registerListener() {
        c_platform.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("platform", c_platform.getText());
                c_platform.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_country.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("country", c_country.getText());
                c_country.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_url_icon.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("url_icon", c_url_icon.getText());
                c_url_icon.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_url_poster.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("url_poster", c_url_poster.getText());
                c_url_poster.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_url_buy.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("url_buy", c_url_buy.getText());
                c_url_buy.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        t_sale.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("sale", t_sale.getText());
                t_sale.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_name_zh.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("name_zh", c_name_zh.getText());
                c_name_zh.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_name_en.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("name_en", c_name_en.getText());
                c_name_en.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        
        i_price_a.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                if (new_value.contains("元/天")) new_value = new_value.replace("元/天", "");
                new_value = new_value.trim();
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("type", CommonService.RENT_TYPE_A);
                args.put("price", Float.parseFloat(new_value));
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_RENT_PRICE, args);
                CommonService.updateGameRentPrice();
                UIToolkit.showServerResponse(rsp);
                updatePrice();
                args.clear();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        i_price_b.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                if (new_value.contains("元/天")) new_value = new_value.replace("元/天", "");
                new_value = new_value.trim();
                args.put("gid", Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("type", CommonService.RENT_TYPE_B);
                args.put("price", Float.parseFloat(new_value));
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_RENT_PRICE, args);
                CommonService.updateGameRentPrice();
                UIToolkit.showServerResponse(rsp);
                updatePrice();
                args.clear();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            if (args.isEmpty()) {
                JOptionPane.showMessageDialog(ManageGame.this, "没有可更新的内容", "信息", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME, args);
            UIToolkit.showServerResponse(rsp);
            if (CommonService.isResponseSuccess(rsp)) {
                if (args.has("platform"))   c_platform.setForeground(Color.darkGray);
                if (args.has("country"))    c_country.setForeground(Color.darkGray);
                if (args.has("url_icon"))   c_url_icon.setForeground(Color.darkGray);
                if (args.has("url_poster")) c_url_poster.setForeground(Color.darkGray);
                if (args.has("url_buy"))    c_url_buy.setForeground(Color.darkGray);
                if (args.has("sale"))       t_sale.setForeground(Color.darkGray);
                if (args.has("name_zh"))    c_name_zh.setForeground(Color.darkGray);
                if (args.has("name_en"))    c_name_en.setForeground(Color.darkGray);
                args.clear();
            }
        });
        ((JButton) toolbar.getComponent(1)).addActionListener(e->{
            FjTextField tag = new FjTextField();
            tag.setDefaultTips("(标签)");
            while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ManageGame.this, tag, "加标签", JOptionPane.OK_CANCEL_OPTION)) {
                if (0 == tag.getText().length()) {
                    JOptionPane.showMessageDialog(ManageGame.this, "标签不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                JSONObject args = new JSONObject();
                args.put("type",        CommonService.TAG_GAME);
                args.put("instance",    Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("tag",         tag.getText());
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_TAG, args);
                CommonService.updateTag();
                UIToolkit.showServerResponse(rsp);
                updateTag();
                break;
            }
        });
    }
    
    private void updatePrice() {
        int gid = Integer.parseInt(i_gid.getText().split("x")[1], 16);
        i_price_a.setText((null != CommonService.getRentPriceByGid(gid, CommonService.RENT_TYPE_A) ? CommonService.getRentPriceByGid(gid, CommonService.RENT_TYPE_A).i_price : 0.0f) + "元/天");
        i_price_b.setText((null != CommonService.getRentPriceByGid(gid, CommonService.RENT_TYPE_B) ? CommonService.getRentPriceByGid(gid, CommonService.RENT_TYPE_B).i_price : 0.0f) + "元/天");
    }
    
    private void updateTag() {
        int gid = Integer.parseInt(i_gid.getText().split("x")[1], 16);
        pane_tag.removeAll();
        CommonService.getTagByInstance(CommonService.TAG_GAME, gid)
                .stream()
                .forEach(tag->{
                    JButton btn = new JButton(tag.c_tag);
                    btn.setContentAreaFilled(false);
                    btn.addActionListener(e->{
                        UIToolkit.deleteTag(tag);
                        CommonService.updateTag();
                        updateTag();
                    });
                    pane_tag.add(btn);
                });
        pane_tag.revalidate();
        pane_tag.repaint();
    }
}
