package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjListCell;
import com.ski.stub.bean.BeanChannelAccount;

public class ListCellChannelAccount extends FjListCell<BeanChannelAccount> {

    private static final long serialVersionUID = 6691136971313150684L;
    
    private FjEditLabel i_channel;
    private FjEditLabel c_user;
    private FjEditLabel c_phone;
    private FjEditLabel i_caid;
    private JButton     b_update;

    public ListCellChannelAccount(BeanChannelAccount data) {
        super(data);
        
        i_channel = new FjEditLabel(0 == data.i_channel ? "[淘宝]" : "[微信]");
        i_channel.setFont(i_channel.getFont().deriveFont(Font.ITALIC));
        c_user    = new FjEditLabel(data.c_user);
        c_user.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        c_phone   = new FjEditLabel(data.c_phone);
        i_caid    = new FjEditLabel(String.format("0x%08X", data.i_caid), false);
        b_update  = new JButton("更新");
        
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
    }

}
