package com.ski.omc.comp;

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
    private JLabel c_games;
    private JLabel c_user_a;
    private JLabel c_user_b;
    
    public ListCellGameAccount(BeanGameAccount data) {
        super(data);
        
        c_user  = new JLabel(data.c_user);
        c_pass  = new JLabel(data.c_pass);
        c_pass.setHorizontalAlignment(SwingConstants.CENTER);
        t_birth = new JLabel(0 == data.t_birth.length() ? "(没有生日)" : data.t_birth);
        t_birth.setHorizontalAlignment(SwingConstants.CENTER);
        i_gaid  = new JLabel(String.format("0x%08X", data.i_gaid));
        i_gaid.setHorizontalAlignment(SwingConstants.RIGHT);
        
        c_user.setForeground(color_major);
        c_pass.setForeground(color_major);
        t_birth.setForeground(color_minor);
        i_gaid.setForeground(color_minor);
        
        List<BeanGame> games = CommonService.getGameByGaid(data.i_gaid);
        c_games = new JLabel("包含游戏:" + (!games.isEmpty() ? games.stream().map(game->game.getDisplayName()).collect(Collectors.joining("; ")) : "-"));
        c_games.setForeground(color_minor);
        BeanChannelAccount user_a = CommonService.getChannelAccountByCaid(CommonService.getChannelAccountByGaid(data.i_gaid, CommonService.RENT_TYPE_A));
        c_user_a = new JLabel("A租用户: " + (null != user_a ? user_a.getDisplayName() : "-"));
        c_user_a.setForeground(color_minor);
        BeanChannelAccount user_b = CommonService.getChannelAccountByCaid(CommonService.getChannelAccountByGaid(data.i_gaid, CommonService.RENT_TYPE_B));
        c_user_b = new JLabel("B租用户: " + (null != user_b ? user_b.getDisplayName() : "-"));
        c_user_b.setForeground(color_minor);
        
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
        panel2.add(c_user_a);
        panel2.add(c_user_b);
        
        setLayout(new GridLayout(3, 1));
        add(panel1);
        add(c_games);
        add(panel2);
        
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