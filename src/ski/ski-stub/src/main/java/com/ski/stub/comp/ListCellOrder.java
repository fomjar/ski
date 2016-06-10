package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.fomjar.widget.FjListCell;
import com.ski.stub.Service;
import com.ski.stub.bean.BeanOrder;

public class ListCellOrder extends FjListCell<BeanOrder> {

    private static final long serialVersionUID = 809658844004339713L;
    private JLabel i_platform;
    private JLabel i_caid;
    private JLabel t_create;
    private JLabel i_oid;

    public ListCellOrder(BeanOrder data) {
        super(data);
        i_platform = new JLabel(0 == data.i_platform ? "[淘宝]" : "[微信]");
        i_platform.setForeground(color_major);
        i_platform.setFont(i_platform.getFont().deriveFont(Font.ITALIC));
        i_caid = new JLabel(Service.map_channel_account.get(data.i_caid).c_user);
        i_caid.setForeground(color_major);
        i_caid.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        t_create = new JLabel(data.t_create);
        t_create.setForeground(color_major);
        i_oid = new JLabel(String.format("0x%08X", data.i_oid));
        i_oid.setForeground(color_minor);
        
        setLayout(new BorderLayout());
        add(i_platform, BorderLayout.WEST);
        add(i_caid, BorderLayout.CENTER);
        add(t_create, BorderLayout.SOUTH);
        add(i_oid, BorderLayout.EAST);
        
        registerListener();
    }
    
    private void registerListener() {
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ManageOrder(SwingUtilities.getWindowAncestor(ListCellOrder.this), getData()).setVisible(true);
            }
        });
    }

}