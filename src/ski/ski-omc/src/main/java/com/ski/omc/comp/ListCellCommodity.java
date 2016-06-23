package com.ski.omc.comp;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.fomjar.widget.FjListCell;
import com.ski.omc.Service;
import com.ski.omc.bean.BeanCommodity;

import net.sf.json.JSONObject;

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
        
        i_csn       = new JLabel("商品序号：" + String.format("0x%08X", data.i_csn));
        i_csn.setForeground(color_minor);
        c_commodity = new JLabel("商品信息：" + String.format("%s - %s", Service.map_game_account.get(Integer.parseInt(data.c_arg0, 16)).c_user, data.c_arg1));
        c_commodity.setForeground(color_major);
        c_commodity2= new JLabel("辅助信息：" + Service.getGameAccountGames(Integer.parseInt(data.c_arg0, 16)).stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")));
        i_price     = new JLabel("商品单价：" + data.i_price + "元/天");
        i_price.setForeground(color_major);
        t_time      = new JLabel("起止日期：" + String.format("%s ~ %s", data.t_begin, 0 < data.t_end.length() ? data.t_end : "(尚未结束)"));
        t_time.setForeground(color_major);
        i_expense   = new JLabel("商品总价：" + (0 < data.t_end.length() ? (data.i_expense + "元/天") : "(尚未计算消费)"));
        i_expense.setForeground(color_major);
        c_remark    = new JLabel("备    注：" + (0 == data.c_remark.length() ? "-" : data.c_remark));
        c_remark.setForeground(color_minor);
        
        setLayout(new GridLayout(7, 1));
        add(i_csn);
        add(c_commodity);
        add(c_commodity2);
        add(i_price);
        add(t_time);
        add(i_expense);
        add(c_remark);
        
        menu = new JPopupMenu();
        menu.add(new JMenuItem("退    租"));
        menu.add(new JMenuItem("账号信息"));
        menu.add(new JMenuItem("租赁报告"));
        if (0 < data.t_end.length()) menu.getComponent(0).setEnabled(false);
        
        ((JMenuItem) menu.getComponent(1)).addActionListener(e->{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            JSONObject args = new JSONObject();
            args.put("oid", data.i_oid);
            args.put("csn", data.i_csn);
            args.put("end", sdf.format(new Date(System.currentTimeMillis())));
            
            JSONObject args2;
        });
        ((JMenuItem) menu.getComponent(1)).addActionListener(e->new ManageChannelAccount(Service.map_order.get(data.i_oid).i_caid).setVisible(true));
        ((JMenuItem) menu.getComponent(2)).addActionListener(e->new OCRDialog(data).setVisible(true));
        
        addActionListener(e->{
            MouseEvent me = (MouseEvent) e.getSource();
            menu.show(ListCellCommodity.this, me.getX(), me.getY());
        });
    }

}
