package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.ski.common.SkiCommon;
import com.ski.omc.Service;
import com.ski.omc.UIToolkit;
import com.ski.omc.bean.BeanChannelAccount;
import com.ski.omc.bean.BeanPlatformAccount;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageChannelAccount extends JDialog {

    private static final long serialVersionUID = -1539252669784510561L;
    
    private JToolBar toolbar;
    private BeanChannelAccount user;
    private FjListPane<String> pane_user;
    private FjListPane<String> pane_account;
    private FjListPane<String> pane_order;
    
    public ManageChannelAccount(int caid) {
        user = Service.map_channel_account.get(caid);
        BeanPlatformAccount platform = Service.map_platform_account.get(Service.getPlatformAccountByChannelAccount(caid));
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("关联其他用户"));
        
        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createTitledBorder("基本信息"));
        panel_basic.setLayout(new GridLayout(2, 1));
        DecimalFormat df = new DecimalFormat("###,###,###.##");
        panel_basic.add(UIToolkit.createBasicInfoLabel("账户余额",  new JLabel(df.format(platform.i_balance) + "元")));
        panel_basic.add(UIToolkit.createBasicInfoLabel("优惠券金额", new JLabel(df.format(platform.i_coupon) + "元")));
        
        pane_user = new FjListPane<String>();
        pane_user.setBorder(BorderFactory.createTitledBorder(pane_user.getBorder(), "关联用户"));
        pane_account = new FjListPane<String>();
        pane_account.setBorder(BorderFactory.createTitledBorder(pane_account.getBorder(), "在租账号"));
        pane_order = new FjListPane<String>();
        pane_order.setBorder(BorderFactory.createTitledBorder(pane_order.getBorder(), "打开订单"));
        
        JPanel panel0 = new JPanel();
        panel0.setLayout(new BorderLayout());
        panel0.add(toolbar, BorderLayout.NORTH);
        panel0.add(panel_basic, BorderLayout.CENTER);
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.add(panel0, BorderLayout.NORTH);
        panel1.add(pane_user, BorderLayout.CENTER);
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout());
        panel2.add(panel1, BorderLayout.NORTH);
        panel2.add(pane_account, BorderLayout.CENTER);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel2, BorderLayout.NORTH);
        getContentPane().add(pane_order, BorderLayout.CENTER);
        
        setTitle(String.format("管理用户“%s”", user.c_user));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 300));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        registerListener();
        
        updateListPane();

    }
    
    private void registerListener() {
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            BeanChannelAccount user2 = UIToolkit.chooseChannelAccount();
            if (null == user2) return;
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(ManageChannelAccount.this, String.format("即将关联用户%s和%s，关联之后将无法回退", user.c_user, user2.c_user), "提示", JOptionPane.OK_CANCEL_OPTION))
                return;
            
            int paid_to = Service.getPlatformAccountByChannelAccount(user.i_caid);
            int paid_from = Service.getPlatformAccountByChannelAccount(user2.i_caid);
            JSONObject args = new JSONObject();
            args.put("paid_to", paid_to);
            args.put("paid_from", paid_from);
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, args);
            JOptionPane.showConfirmDialog(ManageChannelAccount.this, rsp.toString(), "服务器响应", JOptionPane.DEFAULT_OPTION);
            
            Service.updatePlatformAccount();
            Service.updatePlatformAccountMap();
            updateListPane();
        });
    }
    
    private void updateListPane() {
        pane_user.getList().removeAllCell();
        Service.getChannelAccountRelated(user.i_caid).forEach(user->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", user.i_caid, user.c_user));
            cell.addActionListener(e->new ManageChannelAccount(user.i_caid).setVisible(true));
            pane_user.getList().addCell(cell);
        });
        
        pane_account.getList().removeAllCell();
        Service.getRentGameAccountByChannelAccount(user.i_caid, Service.RENT_TYPE_A).forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", account.i_gaid, account.c_user), "A类");
            cell.addActionListener(e->new ManageGameAccount(account.i_gaid).setVisible(true));
            pane_account.getList().addCell(cell);
        });
        Service.getRentGameAccountByChannelAccount(user.i_caid, Service.RENT_TYPE_B).forEach(account->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", account.i_gaid, account.c_user), "B类");
            cell.addActionListener(e->new ManageGameAccount(account.i_gaid).setVisible(true));
            pane_account.getList().addCell(cell);
        });
        
        pane_order.getList().removeAllCell();
        Service.map_order.values()
                .stream()
                .filter(order->order.i_caid == user.i_caid)
                .filter(order->!order.isClose())
                .forEach(order->{
                    FjListCellString cell = new FjListCellString(String.format("0x%08X", order.i_oid), String.format("%s ~ %s", order.t_open, order.t_close));
                    cell.addActionListener(e->new ManageOrder(order.i_oid).setVisible(true));
                    pane_order.getList().addCell(cell);
                });
    }

}
