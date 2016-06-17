package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjListCell;
import com.ski.omc.Service;
import com.ski.omc.bean.BeanGame;
import com.ski.omc.bean.BeanGameAccount;
import com.ski.omc.bean.BeanOrderItem;

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
        c_oper_args = new JLabel("<html>" + generateArgsDesc(data));
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
        
        addActionListener(e->{
            new OIRDialog(data).setVisible(true);
        });
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
    
    private static String generateArgsDesc(BeanOrderItem item) {
        switch (item.i_oper_type) {
        case Service.OPER_TYPE_BUY:         return item.c_oper_arg0;
        case Service.OPER_TYPE_RECHARGE:    return String.format(
                  "金额：%s元<br/>"
                + "备注：%s",
                item.c_oper_arg0,
                0 == item.c_remark.length() ? "-" : item.c_remark);
        case Service.OPER_TYPE_RENT_BEGIN:  {
            BeanGameAccount account = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
            List<BeanGame>  games   = null != account ? Service.getGameAccountGames(account.i_gaid) : null;
            return String.format(
                      "单价：%s元/天<br/>"
                    + "账号：%s - %s<br/>"
                    + "游戏：%s<br/>"
                    + "备注：%s",
                    item.c_oper_arg0,
                    null != account ? account.c_user : "-", item.c_oper_arg2,
                    !games.isEmpty() ? games.stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")) : "-",
                    0 == item.c_remark.length() ? "-" : item.c_remark);
        }
        case Service.OPER_TYPE_RENT_END:    {
            BeanGameAccount account = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
            List<BeanGame>  games   = null != account ? Service.getGameAccountGames(account.i_gaid) : null;
            return String.format(
                      "账号：%s - %s<br/>"
                    + "游戏：%s<br/>"
                    + "备注：%s",
                    null != account ? account.c_user : "-", item.c_oper_arg2,
                    !games.isEmpty() ? games.stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")) : "-",
                    0 == item.c_remark.length() ? "-" : item.c_remark);
        }
        case Service.OPER_TYPE_RENT_PAUSE:  {
            BeanGameAccount account = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
            List<BeanGame>  games   = null != account ? Service.getGameAccountGames(account.i_gaid) : null;
            return String.format(
                      "账号：%s - %s<br/>"
                    + "游戏：%s<br/>"
                    + "备注：%s",
                    null != account ? account.c_user : "-", item.c_oper_arg2,
                    !games.isEmpty() ? games.stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")) : "-",
                    0 == item.c_remark.length() ? "-" : item.c_remark);
        }
        case Service.OPER_TYPE_RENT_RESUME: {
            BeanGameAccount account = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
            List<BeanGame>  games   = null != account ? Service.getGameAccountGames(account.i_gaid) : null;
            return String.format(
                      "账号：%s - %s<br/>"
                    + "游戏：%s<br/>"
                    + "备注：%s",
                    null != account ? account.c_user : "-", item.c_oper_arg2,
                    !games.isEmpty() ? games.stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")) : "-",
                    0 == item.c_remark.length() ? "-" : item.c_remark);
        }
        case Service.OPER_TYPE_RENT_SWAP:   {
            BeanGameAccount account1 = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg1, 16));
            List<BeanGame>  games1   = null != account1 ? Service.getGameAccountGames(account1.i_gaid) : null;
            BeanGameAccount account2 = Service.map_game_account.get(Integer.parseInt(item.c_oper_arg3, 16));
            List<BeanGame>  games2   = null != account2 ? Service.getGameAccountGames(account2.i_gaid) : null;
            return String.format(
                      "(老)账号：%s - %s<br/>"
                    + "(老)游戏：%s<br/>"
                    + "(新)账号：%s - %s<br/>"
                    + "(新)游戏：%s<br/>"
                    + "(新)单价：%s元/天<br/>"
                    + "备注：%s",
                    null != account2 ? account2.c_user : "-", item.c_oper_arg4,
                    !games2.isEmpty() ? games2.stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")) : "-",
                    null != account1 ? account1.c_user : "-", item.c_oper_arg2,
                    !games1.isEmpty() ? games1.stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")) : "-",
                    item.c_oper_arg0,
                    0 == item.c_remark.length() ? "-" : item.c_remark);
        }
        case Service.OPER_TYPE_COUPON:      return String.format(
                  "金额：%s元<br/>"
                + "备注：%s",
                item.c_oper_arg0,
                0 == item.c_remark.length() ? "-" : item.c_remark);
        default: return "(未知订单项参数)";
        }
    }

}
