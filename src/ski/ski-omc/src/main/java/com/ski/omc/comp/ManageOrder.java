package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjEditLabel.EditListener;
import com.fomjar.widget.FjListPane;
import com.ski.common.SkiCommon;
import com.ski.omc.Service;
import com.ski.omc.UIToolkit;
import com.ski.omc.bean.BeanChannelAccount;
import com.ski.omc.bean.BeanCommodity;
import com.ski.omc.bean.BeanOrder;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageOrder extends JDialog {
    
    private static final long serialVersionUID = 7688182519875816792L;
    
    private BeanChannelAccount user;
    private JToolBar    toolbar;
    private FjEditLabel i_oid;
    private FjEditLabel i_platform;
    private FjEditLabel i_caid;
    private FjEditLabel t_open;
    private FjEditLabel t_close;
    private FjListPane<BeanCommodity> pane_commodity;
    
    public ManageOrder(int oid) {
        BeanOrder order = Service.map_order.get(oid);
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        toolbar.add(new JButton("创建商品"));
        toolbar.add(new JButton("更新订单"));
        toolbar.add(new JButton("关闭订单"));
        toolbar.addSeparator();
        toolbar.add(new JButton("管理用户"));
        i_oid       = new FjEditLabel(String.format("0x%08X", order.i_oid), false);
        i_platform  = new FjEditLabel(0 == order.i_platform ? "淘宝" : "微信");
        user = Service.map_channel_account.get(order.i_caid);
        i_caid      = new FjEditLabel(user.c_user);
        t_open      = new FjEditLabel(order.t_open);
        t_close     = new FjEditLabel(order.t_close);
        
        pane_commodity    = new FjListPane<BeanCommodity>();
        pane_commodity.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "商品列表"));
        
        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "基本信息"));
        panel_basic.setLayout(new GridLayout(5, 1));
        panel_basic.add(UIToolkit.createBasicInfoLabel("订单编号", i_oid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("来源平台", i_platform));
        panel_basic.add(UIToolkit.createBasicInfoLabel("渠道用户", i_caid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("打开时间", t_open));
        panel_basic.add(UIToolkit.createBasicInfoLabel("关闭时间", t_close));
        
        JPanel panel_north = new JPanel();
        panel_north.setLayout(new BoxLayout(panel_north, BoxLayout.Y_AXIS));
        panel_north.add(toolbar);
        panel_north.add(panel_basic);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel_north, BorderLayout.NORTH);
        getContentPane().add(pane_commodity, BorderLayout.CENTER);
        
        setTitle(String.format("管理订单“0x%08X”", order.i_oid));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(500, 500));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        registerListener();
        
        updateAccountNCommodity();
    }
    
    private JSONObject args = new JSONObject();
    
    private void registerListener() {
        i_platform.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("oid", Integer.parseInt(i_oid.getText().split("x")[1], 16));
                args.put("platform", new_value.contains("淘宝") ? 0 : 1);
                i_platform.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        i_caid.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                boolean isexist = false;
                for (BeanChannelAccount account : Service.map_channel_account.values()) {
                    if (account.c_user.equals(new_value)) {
                        isexist = true;
                        break;
                    }
                }
                if (!isexist) {
                    JOptionPane.showConfirmDialog(ManageOrder.this, "输入的用户不存在", "错误", JOptionPane.DEFAULT_OPTION);
                    i_caid.setText(old_value);
                    return;
                }
                args.put("oid", Integer.parseInt(i_oid.getText().split("x")[1], 16));
                args.put("caid", new_value);
                i_caid.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        t_open.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("oid", Integer.parseInt(i_oid.getText().split("x")[1], 16));
                args.put("open", new_value);
                t_open.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        t_close.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("oid", Integer.parseInt(i_oid.getText().split("x")[1], 16));
                args.put("close", new_value);
                t_close.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            UIToolkit.createCommodity(Integer.parseInt(i_oid.getText().split("x")[1], 16));
            
            Service.updateOrder();
            Service.updateGameAccountRent();
            updateAccountNCommodity();
        });
        ((JButton) toolbar.getComponent(1)).addActionListener(e->{
            if (args.isEmpty()) {
                JOptionPane.showConfirmDialog(ManageOrder.this, "没有可更新的内容", "信息", JOptionPane.DEFAULT_OPTION);
                return;
            }
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_ORDER, args);
            JOptionPane.showConfirmDialog(ManageOrder.this, rsp, "服务器响应", JOptionPane.DEFAULT_OPTION);
            if (null != rsp && Service.isResponseSuccess(rsp)) {
                if (args.has("platform"))   i_platform.setForeground(Color.darkGray);
                if (args.has("caid"))       i_caid.setForeground(Color.darkGray);
                if (args.has("open"))       t_open.setForeground(Color.darkGray);
                if (args.has("close"))      t_close.setForeground(Color.darkGray);
                args.clear();
            }
        });
        ((JButton) toolbar.getComponent(2)).addActionListener(e->{
            if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(ManageOrder.this, "确定关闭此订单", "提示", JOptionPane.OK_CANCEL_OPTION))
                return;
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            JSONObject args = new JSONObject();
            args.put("oid", Integer.parseInt(i_oid.getText().split("x")[1], 16));
            args.put("end", sdf.format(new Date(System.currentTimeMillis())));
            FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_ORDER, args);
            JOptionPane.showConfirmDialog(ManageOrder.this, rsp, "服务器响应", JOptionPane.DEFAULT_OPTION);
        });
        ((JButton) toolbar.getComponent(4)).addActionListener(e->{
            new ManageChannelAccount(user.i_caid).setVisible(true);
        });
    }

    private void updateAccountNCommodity() {
        Collection<BeanCommodity> commodities = Service.map_order.get(Integer.parseInt(i_oid.getText().split("x")[1], 16)).commodities.values();
        pane_commodity.getList().removeAllCell();
        commodities.forEach(item->pane_commodity.getList().addCell(new ListCellCommodity(item)));
    }
}
