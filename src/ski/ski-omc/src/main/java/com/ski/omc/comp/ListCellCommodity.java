package com.ski.omc.comp;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.fomjar.widget.FjListCell;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanCommodity;
import com.ski.omc.UIToolkit;

public class ListCellCommodity extends FjListCell<BeanCommodity> {

    private static final long serialVersionUID = -9150442038302281057L;
    
    private JLabel i_csn;
    private JLabel c_commodity;
    private JLabel c_commodity2;
    private JLabel i_price;
    private JLabel t_time;
    private JLabel i_expense;
    private JLabel c_remark;
    
    private JPopupMenu menu;

    public ListCellCommodity(BeanCommodity data) {
        super(data);
        
        i_csn       = new JLabel();
        c_commodity = new JLabel();
        c_commodity2= new JLabel();
        i_price     = new JLabel();
        t_time      = new JLabel();
        i_expense   = new JLabel();
        c_remark    = new JLabel();
        
        setLayout(new GridLayout(7, 1));
        add(i_csn);
        add(c_commodity);
        add(c_commodity2);
        add(i_price);
        add(t_time);
        add(i_expense);
        add(c_remark);
        
        menu = new JPopupMenu();
        menu.add(new JMenuItem("租赁报告"));
        menu.add(new JMenuItem("账号信息"));
        menu.add(new JMenuItem("退    租"));
        if (0 < data.t_end.length()) menu.getComponent(2).setEnabled(false);
        
        ((JMenuItem) menu.getComponent(0)).addActionListener(e->new OCRDialog(data).setVisible(true));
        ((JMenuItem) menu.getComponent(1)).addActionListener(e->new ManageGameAccount(Integer.parseInt(data.c_arg0, 16)).setVisible(true));
        ((JMenuItem) menu.getComponent(2)).addActionListener(e->{
            UIToolkit.closeCommodity(data.i_oid, data.i_csn);
            updateCommodity();
        });
        
        addActionListener(e->{
            MouseEvent me = (MouseEvent) e.getSource();
            menu.show(ListCellCommodity.this, me.getX(), me.getY());
        });
        
        updateCommodity();
    }
    
    private void updateCommodity() {
        setData(CommonService.getOrderByOid(getData().i_oid).commodities.get(getData().i_csn));
        BeanCommodity data = getData();
        
        i_csn.setText("商品序号：" + String.format("0x%08X", data.i_csn));
        c_commodity.setText("商品信息：" + String.format("%s - %s", CommonService.getGameAccountByGaid(Integer.parseInt(data.c_arg0, 16)).c_user, data.c_arg1));
        c_commodity2.setText("辅助信息：" + CommonService.getGameByGaid(Integer.parseInt(data.c_arg0, 16)).stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")));
        i_price.setText("商品单价：" + data.i_price + "元/天");
        t_time.setText("起止日期：" + String.format("%s ~ %s", data.t_begin, 0 < data.t_end.length() ? data.t_end : "(尚未退租)"));
        i_expense.setText("商品总价：" + (0 < data.t_end.length() ? (data.i_expense + "元/天") : "(尚未结算)"));
        c_remark.setText("备    注：" + (0 == data.c_remark.length() ? "-" : data.c_remark));
        
        if (!data.isClose()) {
            i_csn.setForeground(color_minor);
            c_commodity.setForeground(color_major);
            c_commodity2.setForeground(color_major);
            i_price.setForeground(color_major);
            t_time.setForeground(color_major);
            i_expense.setForeground(color_major);
            c_remark.setForeground(color_minor);
        } else {
            i_csn.setForeground(Color.lightGray);
            c_commodity.setForeground(Color.lightGray);
            c_commodity2.setForeground(Color.lightGray);
            i_price.setForeground(Color.lightGray);
            t_time.setForeground(Color.lightGray);
            i_expense.setForeground(Color.lightGray);
            c_remark.setForeground(Color.lightGray);
        }
    }

}
