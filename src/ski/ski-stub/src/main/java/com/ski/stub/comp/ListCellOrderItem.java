package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjListCell;
import com.ski.stub.Service;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;
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
        t_oper_time.setForeground(color_major);
        c_oper_args = new JLabel("<html>" + generateArgsDescription(data));
        c_oper_args.setForeground(color_minor);
        
        JPanel panel1 = new JPanel();
        panel1.setOpaque(false);
        panel1.setLayout(new BorderLayout());
        panel1.add(i_oper_type, BorderLayout.CENTER);
        panel1.add(t_oper_time, BorderLayout.EAST);
        
        JPanel panel2 = new JPanel();
        panel2.setOpaque(false);
        panel2.setLayout(new BorderLayout());
        panel2.add(c_oper_args, BorderLayout.CENTER);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(panel1);
        add(panel2);
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
    
    private static String generateArgsDescription(BeanOrderItem item) {
        switch (item.i_oper_type) {
        // 0 - 购买
        case 0: return item.c_oper_arg0;
        // 1 - 充值
        case 1: return String.format(
                  "金额：%s元<br/>"
                + "备注：%s",
                item.c_oper_arg0,
                item.c_remark);
        // 2 - 起租
        case 2: {
            BeanGameAccount account = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
            BeanGame        game    = null != account ? Service.getOneGameOfGameAccount(account.i_gaid) : null;
            return String.format(
                      "单价：%s元/天<br/>"
                    + "账号：%s - %s<br/>"
                    + "游戏：%s<br/>"
                    + "备注：%s",
                    item.c_oper_arg0,
                    null != account ? account.c_user : "-", item.c_oper_arg2,
                    null != game ? game.c_name_zh : "-",
                    item.c_remark);
        }
        // 3 - 退租
        case 3: {
            BeanGameAccount account = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
            BeanGame        game    = null != account ? Service.getOneGameOfGameAccount(account.i_gaid) : null;
            return String.format(
                      "账号：%s - %s<br/>"
                    + "游戏：%s<br/>"
                    + "备注：%s",
                    null != account ? account.c_user : "-", item.c_oper_arg2,
                    null != game ? game.c_name_zh : "-",
                    item.c_remark);
        }
        // 4 - 停租
        case 4: {
            BeanGameAccount account = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
            BeanGame        game    = null != account ? Service.getOneGameOfGameAccount(account.i_gaid) : null;
            return String.format(
                      "账号：%s - %s<br/>"
                    + "游戏：%s<br/>"
                    + "备注：%s",
                    null != account ? account.c_user : "-", item.c_oper_arg2,
                    null != game ? game.c_name_zh : "-",
                    item.c_remark);
        }
        // 5 - 续租
        case 5: {
            BeanGameAccount account = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
            BeanGame        game    = null != account ? Service.getOneGameOfGameAccount(account.i_gaid) : null;
            return String.format(
                      "账号：%s - %s<br/>"
                    + "游戏：%s<br/>"
                    + "备注：%s",
                    null != account ? account.c_user : "-", item.c_oper_arg2,
                    null != game ? game.c_name_zh : "-",
                    item.c_remark);
        }
        // 6 - 换租
        case 6: {
            BeanGameAccount account_1 = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
            BeanGame        game_1    = null != account_1 ? Service.getOneGameOfGameAccount(account_1.i_gaid) : null;
            BeanGameAccount account_2 = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg3, 16));
            BeanGame        game_2    = null != account_2 ? Service.getOneGameOfGameAccount(account_2.i_gaid) : null;
            return String.format(
                      "(新)单价：%s元/天<br/>"
                    + "(新)账号：%s - %s<br/>"
                    + "(新)游戏：%s<br/>"
                    + "(老)账号：%s - %s<br/>"
                    + "(老)游戏：%s<br/>"
                    + "备注：%s",
                    item.c_oper_arg0,
                    null != account_1 ? account_1.c_user : "-", item.c_oper_arg2,
                    null != game_1 ? game_1.c_name_zh : "-",
                    null != account_2 ? account_2.c_user : "-", item.c_oper_arg4,
                    null != game_2 ? game_2.c_name_zh : "-",
                    item.c_remark);
        }
        // 7 - 赠券
        case 7: return String.format(
                  "金额：%s元<br/>"
                + "备注：%s",
                item.c_oper_arg0,
                item.c_remark);
        default: return "(未知订单项参数)";
        }
    }

}
