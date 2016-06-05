package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjListCell;
import com.ski.stub.bean.BeanOrderItem;

public class ListCellOrderItem extends FjListCell<BeanOrderItem> {

    private static final long serialVersionUID = -9150442038302281057L;
    
    private JLabel i_oper_type;
    private JLabel t_oper_time;
    private JLabel c_oper_args;

    public ListCellOrderItem(BeanOrderItem data) {
        super(data);
        
        i_oper_type = new JLabel(getOperTypeInt2String(data.i_oper_type));
        i_oper_type.setFont(i_oper_type.getFont().deriveFont(Font.BOLD));
        i_oper_type.setForeground(color_major);
        t_oper_time = new JLabel(data.t_oper_time);
        t_oper_time.setForeground(color_minor);
        c_oper_args = new JLabel(String.format("%s - %s - %s - %s - %s", data.c_oper_arg0, data.c_oper_arg1, data.c_oper_arg2, data.c_oper_arg3, data.c_oper_arg4));
        c_oper_args.setForeground(color_minor);
        if (0 != data.c_remark.length()) setToolTipText(data.c_remark);
        
        JPanel panel1 = new JPanel();
        panel1.setOpaque(false);
        panel1.setLayout(new BorderLayout());
        panel1.add(i_oper_type, BorderLayout.CENTER);
        panel1.add(t_oper_time, BorderLayout.EAST);
        
        setLayout(new GridLayout(2, 1));
        add(panel1);
        add(c_oper_args);
    }
    
    private static String getOperTypeInt2String(int oper_type) {
        switch (oper_type) {
        case 0: return "购买";
        case 1: return "充值";
        case 2: return "起租";
        case 3: return "退租";
        case 4: return "停租";
        case 5: return "续租";
        case 6: return "换租";
        case 7: return "赠券";
        }
        return null;
    }

}
