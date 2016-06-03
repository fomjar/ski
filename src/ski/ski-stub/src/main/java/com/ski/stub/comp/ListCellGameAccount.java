package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.fomjar.widget.FjListCell;
import com.ski.stub.bean.BeanGameAccount;

public class ListCellGameAccount extends FjListCell<BeanGameAccount> {
    
    private static final long serialVersionUID = 6352907380098820766L;
    
    private JLabel c_user;
    private JLabel c_pass;
    private JLabel i_gaid;
    private JLabel t_birth;
    
    public ListCellGameAccount(BeanGameAccount data) {
        super(data);
        
        c_user  = new JLabel(data.c_user);
        c_user.setForeground(color_major);
        c_pass  = new JLabel(data.c_pass_curr);
        c_pass.setForeground(color_major);
        i_gaid  = new JLabel(String.format("0x%08X", data.i_gaid));
        i_gaid.setForeground(color_minor);
        t_birth = new JLabel(data.t_birth);
        t_birth.setForeground(color_minor);
        
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        JPanel panel_center = new JPanel();
        panel_center.setOpaque(false);
        panel_center.setLayout(new GridLayout(2, 1));
        panel_center.add(c_user);
        panel_center.add(c_pass);
        JPanel panel_east = new JPanel();
        panel_east.setOpaque(false);
        panel_east.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        panel_east.setLayout(new GridLayout(2, 1));
        panel_east.add(i_gaid);
        panel_east.add(t_birth);
        panel.add(panel_center, BorderLayout.CENTER);
        panel.add(panel_east, BorderLayout.EAST);
        
        add(panel, BorderLayout.CENTER);
        
        registerListener();
    }

    private void registerListener() {
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ManageGameAccount(SwingUtilities.getWindowAncestor(ListCellGameAccount.this), getData()).setVisible(true);
            }
        });
    }
}