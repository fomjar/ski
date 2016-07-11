package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjListCell;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanPlatformAccount;
import com.ski.omc.UIToolkit;

public class ListCellDetailUser extends FjListCell<BeanChannelAccount> {

    private static final long serialVersionUID = -6820038235647058975L;
    
    public ListCellDetailUser(BeanChannelAccount user) {
        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(user.i_caid));
        
        JLabel      platf = new JLabel("[" + getPlatform(user.i_channel) + "] ");
        FjEditLabel infos = new FjEditLabel(String.format("用户名：%s 电话：%s", user.getDisplayName(), user.c_phone));
        JLabel      money = new JLabel(String.format("%.2f元/%.2f元", puser.i_cash, puser.i_coupon));
        
        platf.setFont(platf.getFont().deriveFont(Font.ITALIC));
        
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        info.setLayout(new BorderLayout());
        info.add(platf, BorderLayout.WEST);
        info.add(infos, BorderLayout.CENTER);
        info.add(money, BorderLayout.EAST);
        
        JPanel oper = new JPanel();
        oper.setOpaque(false);
        oper.setLayout(new BoxLayout(oper, BoxLayout.X_AXIS));
        oper.add(UIToolkit.createDetailButton("基", e->{
            if (null != user) new ManageUser(user.i_caid).setVisible(true);
        }));
        oper.add(Box.createHorizontalStrut(4));
        oper.add(UIToolkit.createDetailButton("流", e->{
            if (null != user) new UserFlow(CommonService.getPlatformAccountByCaid(user.i_caid));
        }));
        
        setLayout(new BorderLayout());
        add(info, BorderLayout.CENTER);
        add(oper, BorderLayout.EAST);
        
        setColorDefault(Color.white);
        setColorOver(Color.white);
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
