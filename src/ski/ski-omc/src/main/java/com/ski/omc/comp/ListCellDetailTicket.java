package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjListCell;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanTicket;

public class ListCellDetailTicket extends FjListCell<BeanTicket> {

    private static final long serialVersionUID = 7022627587968587017L;

    public ListCellDetailTicket(BeanTicket ticket) {
        JLabel      type = new JLabel("[" + getTypeDesc(ticket.i_type) + "] ");
        FjEditLabel info = new FjEditLabel(String.format("%s %s", ticket.c_title, ticket.c_content));
        
        type.setFont(type.getFont().deriveFont(Font.ITALIC));
        
        JPanel infos = new JPanel();
        infos.setOpaque(false);
        infos.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        infos.setLayout(new BorderLayout());
        infos.add(type, BorderLayout.WEST);
        infos.add(info, BorderLayout.CENTER);
        
        JPanel oper = new JPanel();
        oper.setOpaque(false);
        oper.setLayout(new BoxLayout(oper, BoxLayout.X_AXIS));
        oper.add(DetailPane.createToolBarButton("信息", e->new ManageTicket(ticket.i_tid).setVisible(true)));
        
        setLayout(new BorderLayout());
        add(infos, BorderLayout.CENTER);
        add(oper, BorderLayout.EAST);
        
        setColorDefault(Color.white);
        if (ticket.isClose()) {
            type.setForeground(Color.lightGray);
            info.setForeground(Color.lightGray);
        }
        
        passthroughMouseEvent(info);
    }
    
    private static String getTypeDesc(int type) {
        switch (type) {
        case CommonService.TICKET_TYPE_ADVICE:     return "意见建议";
        case CommonService.TICKET_TYPE_COMMENT: return "备忘既要";
        case CommonService.TICKET_TYPE_NOTIFY:     return "通知提醒";
        case CommonService.TICKET_TYPE_REFUND:     return "退款申请";
        case CommonService.TICKET_TYPE_RESERVE: return "预约预定";
        default: return "未    知";
        }
    }

}
