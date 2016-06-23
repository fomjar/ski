package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

import com.fomjar.widget.FjListCell;
import com.ski.omc.Service;
import com.ski.omc.bean.BeanOrder;

public class ListCellOrder extends FjListCell<BeanOrder> {

    private static final long serialVersionUID = 809658844004339713L;
    private JLabel i_platform;
    private JLabel i_caid;
    private JLabel t_time;
    private JLabel i_oid;

    public ListCellOrder(BeanOrder data) {
        super(data);
        i_platform = new JLabel(0 == data.i_platform ? "[淘宝] " : "[微信] ");
        i_platform.setFont(i_platform.getFont().deriveFont(Font.ITALIC));
        i_caid = new JLabel(Service.map_channel_account.get(data.i_caid).c_user);
        t_time = new JLabel(String.format("%s ~ %s", data.t_open, data.t_close));
        i_oid = new JLabel(String.format("0x%08X", data.i_oid));
        if (!data.isClose()) {
            i_platform.setForeground(color_major);
            i_caid.setForeground(color_major);
            t_time.setForeground(color_major);
            i_oid.setForeground(color_minor);
        } else {
            i_platform.setForeground(Color.lightGray);
            i_caid.setForeground(Color.lightGray);
            t_time.setForeground(Color.lightGray);
            i_oid.setForeground(Color.lightGray);
        }
        
        setLayout(new BorderLayout());
        add(i_platform, BorderLayout.WEST);
        add(i_caid, BorderLayout.CENTER);
        add(t_time, BorderLayout.SOUTH);
        add(i_oid, BorderLayout.EAST);
        
        registerListener();
    }
    
    private void registerListener() {
        addActionListener(e->new ManageOrder(getData().i_oid).setVisible(true));
    }

}
