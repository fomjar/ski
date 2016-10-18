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

public class ListCellDetailUser extends FjListCell<BeanChannelAccount> {

    private static final long serialVersionUID = -6820038235647058975L;
    
    public ListCellDetailUser(BeanChannelAccount user) {
        JLabel      platf = new JLabel("[" + getPlatform(user.i_channel) + "] ");
        FjEditLabel infos = new FjEditLabel(String.format("用户名：%s 电话：%s", user.getDisplayName(), user.c_phone));
        
        platf.setFont(platf.getFont().deriveFont(Font.ITALIC));
        
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        info.setLayout(new BorderLayout());
        info.add(platf, BorderLayout.WEST);
        info.add(infos, BorderLayout.CENTER);
        
        JPanel oper = new JPanel();
        oper.setOpaque(false);
        oper.setLayout(new BoxLayout(oper, BoxLayout.X_AXIS));
        oper.add(DetailPane.createToolBarButton("信息", e->{
            if (null != user) new ManageUser(user.i_caid).setVisible(true);
        }));
        oper.add(Box.createHorizontalStrut(4));
        oper.add(DetailPane.createToolBarButton("流水", e->{
            if (null != user) new ManageFlow(CommonService.getPlatformAccountByCaid(user.i_caid));
        }));
        
        setLayout(new BorderLayout());
        add(info, BorderLayout.CENTER);
        add(oper, BorderLayout.EAST);
        
        setColorDefault(Color.white);
        
        passthroughMouseEvent(info);
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
