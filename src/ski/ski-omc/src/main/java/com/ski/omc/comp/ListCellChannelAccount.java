package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.fomjar.widget.FjListCell;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanGameAccount;

public class ListCellChannelAccount extends FjListCell<BeanChannelAccount> {

    private static final long serialVersionUID = 6691136971313150684L;
    
    private JLabel  i_channel;
    private JLabel  c_user;
    private JLabel  c_phone;
    private JLabel  i_caid;
    private JLabel  c_accounts_a;
    private JLabel  c_accounts_b;

    public ListCellChannelAccount(BeanChannelAccount user) {
        super(user);
        
        i_channel = new JLabel("[" + getChannel2String(user.i_channel) + "] ");
        i_channel.setForeground(color_major);
        i_channel.setFont(i_channel.getFont().deriveFont(Font.ITALIC));
        c_user    = new JLabel(0 < user.c_name.length() ? (String.format("%s(%s)", user.c_name, user.c_user)) : user.c_user);
        c_user.setForeground(color_major);
        c_phone   = new JLabel(0 == user.c_phone.length() ? "(没有电话)" : user.c_phone);
        c_phone.setForeground(color_major);
        i_caid    = new JLabel(String.format("0x%08X", user.i_caid));
        i_caid.setForeground(color_minor);
        i_caid.setHorizontalAlignment(SwingConstants.RIGHT);
        List<BeanGameAccount> accounts_a = CommonService.getRentGameAccountByCaid(user.i_caid, CommonService.RENT_TYPE_A);
        c_accounts_a = new JLabel("A租账号: " + (!accounts_a.isEmpty() ? accounts_a.stream().map(a->a.c_user).collect(Collectors.joining("; ")) : "-"));
        c_accounts_a.setForeground(color_minor);
        c_accounts_a.setPreferredSize(new Dimension(240, 0));
        List<BeanGameAccount> accounts_b = CommonService.getRentGameAccountByCaid(user.i_caid, CommonService.RENT_TYPE_B);
        c_accounts_b = new JLabel("B租账号: " + (!accounts_b.isEmpty() ? accounts_b.stream().map(b->b.c_user).collect(Collectors.joining("; ")) : "-"));
        c_accounts_b.setForeground(color_minor);
        c_accounts_b.setPreferredSize(new Dimension(240, 0));
        
        JPanel panel1 = new JPanel();
        panel1.setOpaque(false);
        panel1.setLayout(new GridLayout(1, 2));
        panel1.add(c_phone);
        panel1.add(i_caid);
        
        JPanel panel_up = new JPanel();
        panel_up.setOpaque(false);
        panel_up.setLayout(new BorderLayout());
        panel_up.add(i_channel, BorderLayout.WEST);
        panel_up.add(c_user, BorderLayout.CENTER);
        panel_up.add(panel1, BorderLayout.EAST);
        
        JPanel panel_down = new JPanel();
        panel_down.setOpaque(false);
        panel_down.setLayout(new GridLayout(1, 2));
        panel_down.add(c_accounts_a);
        panel_down.add(c_accounts_b);
        
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridLayout(2, 1));
        panel.add(panel_up);
        panel.add(panel_down);
        
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        
        addActionListener(e->new ManageChannelAccount(user.i_caid).setVisible(true));
    }
    
    private static String getChannel2String(int channel) {
        switch (channel) {
        case CommonService.CHANNEL_TAOBAO: return "淘  宝";
        case CommonService.CHANNEL_WECHAT: return "微  信";
        case CommonService.CHANNEL_ALIPAY: return "支付宝";
        default: return "未  知";
        }
    }
}
