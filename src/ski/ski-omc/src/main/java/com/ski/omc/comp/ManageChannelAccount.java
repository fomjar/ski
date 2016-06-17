package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.ski.omc.Report;
import com.ski.omc.Service;
import com.ski.omc.UIToolkit;
import com.ski.omc.bean.BeanChannelAccount;
import com.ski.omc.bean.BeanGameAccount;
import com.ski.omc.bean.BeanOrder;

public class ManageChannelAccount extends JDialog {

    private static final long serialVersionUID = -1539252669784510561L;
    
    public ManageChannelAccount(int caid) {
        BeanChannelAccount user = Service.map_channel_account.get(caid);
        Map<String, Object> ucd = Report.createUCD(user.i_caid);
        
        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createTitledBorder("基本信息"));
        panel_basic.setLayout(new GridLayout(2, 1));
        DecimalFormat df = new DecimalFormat("###,###,###.##");
        panel_basic.add(UIToolkit.createBasicInfoLabel("账户余额", new JLabel(df.format(ucd.get("balance")) + "元")));
        panel_basic.add(UIToolkit.createBasicInfoLabel("可退金额", new JLabel(df.format(ucd.get("refund")) + "元")));
        
        FjListPane<String> pane_account = new FjListPane<String>();
        pane_account.setBorder(BorderFactory.createTitledBorder(pane_account.getBorder(), "在租账号"));
        List<BeanGameAccount> rents_a = Service.getGameAccountByUser(user.i_caid, Service.RENT_TYPE_A);
        rents_a.forEach(r->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", r.i_gaid, r.c_user), "A类");
            cell.addActionListener(e->new ManageGameAccount(r.i_gaid).setVisible(true));
            pane_account.getList().addCell(cell);
        });
        List<BeanGameAccount> rents_b = Service.getGameAccountByUser(user.i_caid, Service.RENT_TYPE_B);
        rents_b.forEach(r->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", r.i_gaid, r.c_user), "B类");
            cell.addActionListener(e->new ManageGameAccount(r.i_gaid).setVisible(true));
            pane_account.getList().addCell(cell);
        });
        
        FjListPane<String> pane_order = new FjListPane<String>();
        pane_order.setBorder(BorderFactory.createTitledBorder(pane_order.getBorder(), "相关订单"));
        List<BeanOrder> orders = Service.map_order.values()
                .stream()
                .filter(order->order.i_caid == user.i_caid)
                .collect(Collectors.toList());
        orders.forEach(o->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X", o.i_oid), o.t_create);
            cell.addActionListener(e->new ManageOrder(o.i_oid).setVisible(true));
            pane_order.getList().addCell(cell);
        });
        
        JPanel panel_center = new JPanel();
        panel_center.setLayout(new BorderLayout());
        panel_center.add(pane_account, BorderLayout.NORTH);
        panel_center.add(pane_order, BorderLayout.CENTER);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel_basic, BorderLayout.NORTH);
        getContentPane().add(panel_center, BorderLayout.CENTER);
        
        setTitle(String.format("管理用户“%s”", user.c_user));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(300, 240));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
    }

}
