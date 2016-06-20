package com.ski.omc.comp;

import java.awt.GridLayout;

import javax.swing.JLabel;

import com.fomjar.widget.FjListCell;
import com.ski.omc.Service;
import com.ski.omc.bean.BeanCommodity;

public class ListCellCommodity extends FjListCell<BeanCommodity> {

    private static final long serialVersionUID = -9150442038302281057L;
    
    private JLabel i_csn;
    private JLabel c_commodity;
    private JLabel i_price;
    private JLabel t_time;
    private JLabel i_expense;
    private JLabel c_remark;

    public ListCellCommodity(BeanCommodity data) {
        super(data);
        
        i_csn = new JLabel(String.format("0x%08X", data.i_csn));
        i_csn.setForeground(color_minor);
        c_commodity = new JLabel(String.format("0x%08X - %s - %s", Integer.parseInt(data.c_arg0, 16), Service.map_game_account.get(Integer.parseInt(data.c_arg0, 16)), data.c_arg1));
        c_commodity.setForeground(color_major);
        i_price = new JLabel(data.i_price + "元/天");
        i_price.setForeground(color_major);
        t_time = new JLabel(String.format("%s ~ %s", data.t_begin, data.t_end));
        t_time.setForeground(color_major);
        i_expense = new JLabel(data.i_expense + "元/天");
        i_expense.setForeground(color_major);
        c_remark = new JLabel(0 == data.c_remark.length() ? "-" : data.c_remark);
        c_remark.setForeground(color_minor);
        
        setLayout(new GridLayout(1, 6));
        add(i_csn);
        add(c_commodity);
        add(i_price);
        add(t_time);
        add(i_expense);
        add(c_remark);
        
        addActionListener(e->{
            new OCRDialog(data).setVisible(true);
        });
    }

}
