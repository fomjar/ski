package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.NoSuchElementException;

import javax.swing.JLabel;

import com.fomjar.widget.FjListCell;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;

public class ListCellUser extends FjListCell<BeanChannelAccount> {
    
    private static final long serialVersionUID = -1579462449977954180L;
    
    private JLabel plat;
    private JLabel name;
    private JLabel rent;

    public ListCellUser(BeanChannelAccount data) {
        super(data);
        
        plat = new JLabel("[" + getPlatform(data.i_channel) + "] ");
        name = new JLabel(data.getDisplayName());
        name.setPreferredSize(new Dimension(0, 0));
        rent = new JLabel(getMinorString(data));
        
        plat.setFont(plat.getFont().deriveFont(Font.ITALIC));
        if (rent.getText().startsWith("00 /")) {
            plat.setForeground(Color.lightGray);
            name.setForeground(Color.lightGray);
            rent.setForeground(Color.lightGray);
        } else {
            plat.setForeground(color_major);
            name.setForeground(color_major);
            rent.setForeground(color_major);
        }
        
        setLayout(new BorderLayout());
        add(plat, BorderLayout.WEST);
        add(name, BorderLayout.CENTER);
        add(rent, BorderLayout.EAST);
        
        addActionListener(e->MainFrame.getInstance().setDetailUser(data.i_caid));
    }

    private static String getMinorString(BeanChannelAccount data) {
        int renting = CommonService.getRentGameAccountByCaid(data.i_caid, CommonService.RENT_TYPE_A).size()
                + CommonService.getRentGameAccountByCaid(data.i_caid, CommonService.RENT_TYPE_B).size();
        int all = 0;
        try {all = CommonService.getOrderByCaid(data.i_caid).stream().map(order->order.commodities.size()).reduce(0, (c1, c2)->c1 + c2).intValue();}
        catch (NoSuchElementException e) {}
        return String.format("%02d / %02d", renting, all);
    }
    
    private static String getPlatform(int channel) {
        switch (channel) {
        case CommonService.CHANNEL_TAOBAO: return "淘  宝";
        case CommonService.CHANNEL_WECHAT: return "微  信";
        case CommonService.CHANNEL_ALIPAY: return "支付宝";
        default: return "未  知";
        }
    }

}
