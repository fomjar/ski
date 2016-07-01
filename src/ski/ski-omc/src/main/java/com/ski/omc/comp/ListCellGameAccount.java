package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.fomjar.widget.FjListCell;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanGame;
import com.ski.common.bean.BeanGameAccount;

public class ListCellGameAccount extends FjListCell<BeanGameAccount> {
    
    private static final long serialVersionUID = 6352907380098820766L;
    
    private JLabel c_user;
    private JLabel c_pass;
    private JLabel t_birth;
    private JLabel i_gaid;
    private JLabel c_channel_account_a;
    private JLabel c_channel_account_b;
    private JLabel c_games;
    
    public ListCellGameAccount(BeanGameAccount data) {
        super(data);
        
        c_user  = new JLabel(data.c_user);
        c_user.setForeground(color_major);
        c_pass  = new JLabel(data.c_pass_curr);
        c_pass.setForeground(color_major);
        t_birth = new JLabel(0 == data.t_birth.length() ? "(没有生日)" : data.t_birth);
        t_birth.setForeground(color_minor);
        i_gaid  = new JLabel(String.format("0x%08X", data.i_gaid));
        i_gaid.setForeground(color_minor);
        i_gaid.setHorizontalAlignment(SwingConstants.RIGHT);
        
        BeanChannelAccount user_a = CommonService.getChannelAccountByCaid(CommonService.getRentChannelAccountByGaid(data.i_gaid, CommonService.RENT_TYPE_A));
        c_channel_account_a = new JLabel("A租用户: " + (null != user_a ? user_a.c_user : "-"));
        c_channel_account_a.setForeground(color_minor);
        c_channel_account_a.setPreferredSize(new Dimension(100, 0));
        BeanChannelAccount user_b = CommonService.getChannelAccountByCaid(CommonService.getRentChannelAccountByGaid(data.i_gaid, CommonService.RENT_TYPE_B));
        c_channel_account_b = new JLabel("B租用户: " + (null != user_b ? user_b.c_user : "-"));
        c_channel_account_b.setForeground(color_minor);
        c_channel_account_b.setPreferredSize(new Dimension(100, 0));
        List<BeanGame> games = CommonService.getGameByGaid(data.i_gaid);
        c_games = new JLabel("包含游戏:" + (!games.isEmpty() ? games.stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")) : "-"));
        c_games.setForeground(color_minor);
        c_games.setPreferredSize(new Dimension(280, 0));
        
        setLayout(new BorderLayout());
        JPanel panel1 = new JPanel();
        panel1.setOpaque(false);
        panel1.setLayout(new GridLayout(1, 4));
        panel1.add(c_user);
        panel1.add(c_pass);
        panel1.add(t_birth);
        panel1.add(i_gaid);
        
        JPanel panel2 = new JPanel();
        panel2.setOpaque(false);
        panel2.setLayout(new GridLayout(1, 2));
        panel2.add(c_channel_account_a);
        panel2.add(c_channel_account_b);
        
        JPanel panel3 = new JPanel();
        panel3.setOpaque(false);
        panel3.setLayout(new BorderLayout());
        panel3.add(c_games, BorderLayout.WEST);
        panel3.add(panel2, BorderLayout.CENTER);
        
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridLayout(2, 1));
        panel.add(panel1);
        panel.add(panel3);
        
        add(panel, BorderLayout.CENTER);
        
        registerListener();
    }

    private void registerListener() {
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ManageGameAccount(getData().i_gaid).setVisible(true);
            }
        });
    }
}