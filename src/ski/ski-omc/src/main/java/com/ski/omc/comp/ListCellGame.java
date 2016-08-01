package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjListCell;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanGame;
import com.ski.omc.UIToolkit;

public class ListCellGame extends FjListCell<BeanGame> {
    
    private static final long serialVersionUID = 6352907380098820766L;
    
    private JLabel 	c_url_icon;
    private JLabel  c_name;
    private JLabel  i_gid;
    private JLabel  t_sale;
    private JLabel  c_category;
    private JLabel  i_price_a;
    private JLabel  i_price_b;
    private JPanel  tags;
    
    public ListCellGame(BeanGame data) {
        super(data);
        
        c_url_icon 	= new JLabel();
        c_name   	= new JLabel();
        i_gid       = new JLabel();
        t_sale      = new JLabel();
        c_category	= new JLabel();
        i_price_a   = new JLabel();
        i_price_b   = new JLabel();
        tags        = new JPanel();
        
        c_url_icon.setPreferredSize(new Dimension(55, 55));
        c_name.setPreferredSize(new Dimension(1, 0));
        c_name.setFont(c_name.getFont().deriveFont(Font.BOLD));
        c_category.setForeground(color_minor);
        UIToolkit.scaleFont(c_category, 0.8f);
        t_sale.setForeground(color_minor);
        UIToolkit.scaleFont(t_sale, 0.8f);
        i_gid.setForeground(color_minor);
        UIToolkit.scaleFont(i_gid, 0.8f);
        UIToolkit.scaleFont(i_price_a, 0.8f);
        UIToolkit.scaleFont(i_price_b, 0.8f);
        tags.setOpaque(false);
        FlowLayout layout = new FlowLayout();
        layout.setVgap(0);
        tags.setLayout(layout);
        
        JPanel panel0 = new JPanel();
        panel0.setOpaque(false);
        panel0.setLayout(new BorderLayout());
        panel0.add(c_name, BorderLayout.CENTER);
        panel0.add(tags, BorderLayout.EAST);
        
        JPanel panel_name = new JPanel();
        panel_name.setOpaque(false);
        panel_name.setLayout(new BorderLayout());
        panel_name.add(panel0, BorderLayout.CENTER);
        panel_name.add(i_gid, BorderLayout.EAST);
        
        JPanel panel_category = new JPanel();
        panel_category.setOpaque(false);
        panel_category.setLayout(new BorderLayout());
        panel_category.add(c_category, BorderLayout.CENTER);
        
        JPanel panel_sale = new JPanel();
        panel_sale.setOpaque(false);
        panel_sale.setLayout(new BorderLayout());
        panel_sale.add(t_sale, BorderLayout.CENTER);
        
        JPanel panel_price = new JPanel();
        panel_price.setOpaque(false);
        panel_price.setLayout(new GridLayout(1, 2));
        panel_price.add(i_price_a);
        panel_price.add(i_price_b);
        
        JPanel panel_main = new JPanel();
        panel_main.setOpaque(false);
        panel_main.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        panel_main.setLayout(new BoxLayout(panel_main, BoxLayout.Y_AXIS));
        panel_main.add(panel_name);
        panel_main.add(panel_category);
        panel_main.add(panel_sale);
        panel_main.add(panel_price);
        
        setLayout(new BorderLayout());
        add(c_url_icon, BorderLayout.WEST);
        add(panel_main, BorderLayout.CENTER);
        
        addActionListener(e->new ManageGame(data.i_gid).setVisible(true));
        
        update();
    }
    
    private void update() {
    	BeanGame data = getData();
    	
        UIToolkit.doLater(()->{
        	if (0 < data.c_url_icon.length()) {
	        	ImageIcon icon = UIToolkit.LoadImage(data.c_url_icon);
	        	icon.setImage(icon.getImage().getScaledInstance(c_url_icon.getWidth(), c_url_icon.getHeight(), Image.SCALE_DEFAULT));
	        	c_url_icon.setIcon(icon);
        	}
        });
        c_name.setText(data.getDiaplayName());
        i_gid.setText(String.format("0x%08X", data.i_gid));
        c_category.setText("游戏分类: " + (0 == data.c_category.length() ? "(没有分类)" : data.c_category));
        t_sale.setText("发售时间: " + (0 == data.t_sale.length() ? "(没有时间)" : data.t_sale));
        i_price_a.setText("A租价格: " + (null != CommonService.getGameRentPriceByGid(data.i_gid, CommonService.RENT_TYPE_A) ? CommonService.getGameRentPriceByGid(data.i_gid, CommonService.RENT_TYPE_A).i_price : 0.0f) + "元/天");
        i_price_b.setText("B租价格: " + (null != CommonService.getGameRentPriceByGid(data.i_gid, CommonService.RENT_TYPE_B) ? CommonService.getGameRentPriceByGid(data.i_gid, CommonService.RENT_TYPE_B).i_price : 0.0f) + "元/天");
        tags.removeAll();
        CommonService.getTagByInstance(CommonService.TAG_GAME, data.i_gid)
		        .stream()
		        .forEach(tag->{
		            JButton btn = new JButton(tag.c_tag);
		            btn.setFont(btn.getFont().deriveFont(8.0f));
		            btn.setMargin(new Insets(0, 0, 0, 0));
		            btn.setContentAreaFilled(false);
		            tags.add(btn);
		        });
        tags.revalidate();
    }
}