package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjListCell;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanGame;

public class ListCellGame extends FjListCell<BeanGame> {
    
    private static final long serialVersionUID = 6352907380098820766L;
    
    private JLabel  c_name_zh;
    private JLabel  i_gid;
    private JLabel  i_price_a;
    private JLabel  i_price_b;
    private JLabel  t_sale;
    private JLabel  c_country;
    private JPanel  tags;
    
    public ListCellGame(BeanGame data) {
        super(data);
        
        c_name_zh   = new JLabel(data.c_name_zh);
        c_name_zh.setForeground(color_major);
        i_gid       = new JLabel(String.format("0x%08X", data.i_gid));
        i_gid.setForeground(color_minor);
        i_price_a   = new JLabel("A: " + (null != CommonService.getRentPriceByGid(data.i_gid, CommonService.RENT_TYPE_A) ? CommonService.getRentPriceByGid(data.i_gid, CommonService.RENT_TYPE_A).i_price : 0.0f) + "元/天");
        i_price_a.setForeground(color_major);
        i_price_b   = new JLabel("B: " + (null != CommonService.getRentPriceByGid(data.i_gid, CommonService.RENT_TYPE_B) ? CommonService.getRentPriceByGid(data.i_gid, CommonService.RENT_TYPE_B).i_price : 0.0f) + "元/天");
        i_price_b.setForeground(color_major);
        t_sale      = new JLabel(0 == data.t_sale.length() ? "(没有发售时间)" : data.t_sale);
        t_sale.setForeground(color_minor);
        c_country   = new JLabel(0 == data.c_country.length() ? "(没有国家)" : data.c_country);
        c_country.setForeground(color_minor);
        tags        = new JPanel();
        tags.setOpaque(false);
        
        FlowLayout layout = new FlowLayout();
        layout.setVgap(0);
        tags.setLayout(layout);
        CommonService.getTagByInstance(CommonService.TAG_GAME, data.i_gid)
                .stream()
                .forEach(tag->{
                    JButton btn = new JButton(tag.c_tag);
                    btn.setFont(btn.getFont().deriveFont(8.0f));
                    btn.setMargin(new Insets(0, 0, 0, 0));
                    btn.setContentAreaFilled(false);
                    tags.add(btn);
                });
        JPanel panelt = new JPanel();
        panelt.setOpaque(false);
        panelt.setLayout(new BorderLayout());
        panelt.add(c_name_zh, BorderLayout.CENTER);
        panelt.add(tags, BorderLayout.EAST);
        
        JPanel panel0 = new JPanel();
        panel0.setOpaque(false);
        panel0.setLayout(new BorderLayout());
        panel0.add(panelt, BorderLayout.CENTER);
        panel0.add(i_gid, BorderLayout.EAST);
        
        JPanel panel1 = new JPanel();
        panel1.setOpaque(false);
        panel1.setLayout(new GridLayout(1, 4));
        panel1.add(i_price_a);
        panel1.add(i_price_b);
        panel1.add(t_sale);
        panel1.add(c_country);
        
        setLayout(new GridLayout(2, 1));
        add(panel0);
        add(panel1);
        
        addActionListener(e->new ManageGame(data.i_gid).setVisible(true));
    }
}