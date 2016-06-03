package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjEditLabel.EditListener;
import com.fomjar.widget.FjListPane;
import com.ski.common.SkiCommon;
import com.ski.stub.Service;
import com.ski.stub.UIToolkit;
import com.ski.stub.bean.BeanChannelAccount;
import com.ski.stub.bean.BeanOrder;
import com.ski.stub.bean.BeanOrderItem;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageOrder extends JDialog {
    
    private static final long serialVersionUID = 7688182519875816792L;
    
    private JToolBar    toolbar;
    private FjEditLabel i_oid;
    private FjEditLabel i_platform;
    private FjEditLabel i_caid;
    private FjEditLabel t_create;
    private FjListPane<BeanOrderItem> listpane;
    
    public ManageOrder(Window owner, BeanOrder order) {
        super(owner, String.format("管理订单“0x%08X”", order.i_oid));
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("更新"));
        toolbar.add(new JButton("新订单项"));
        i_oid       = new FjEditLabel(String.format("0x%08X", order.i_oid), false);
        i_platform  = new FjEditLabel(0 == order.i_platform ? "淘宝" : "微信");
        i_caid      = new FjEditLabel(Service.map_channel_account.get(order.i_caid).c_user);
        t_create    = new FjEditLabel(order.t_create);
        
        listpane    = new FjListPane<BeanOrderItem>();
        listpane.setBorder(BorderFactory.createTitledBorder(listpane.getBorder(), "订单项"));
        
        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createTitledBorder("基本信息"));
        panel_basic.setLayout(new GridLayout(4, 1));
        panel_basic.add(UIToolkit.createBasicInfoLabel("O ID", i_oid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("平台", i_platform));
        panel_basic.add(UIToolkit.createBasicInfoLabel("用户", i_caid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("创建", t_create));
        
        JPanel panel_center = new JPanel();
        panel_center.setLayout(new BorderLayout());
        panel_center.add(panel_basic, BorderLayout.NORTH);
        panel_center.add(listpane, BorderLayout.CENTER);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(panel_center, BorderLayout.CENTER);
        
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 400));
        setLocation(owner.getX() - (getWidth() - owner.getWidth()) / 2, owner.getY() - (getHeight() - owner.getHeight()) / 2);
        
        registerListener();
        
        updateOrderItem();
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
        t_create.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("oid", Integer.parseInt(i_oid.getText().split("x")[1], 16));
                args.put("create", new_value);
                t_create.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        ((JButton) toolbar.getComponent(0)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Service.doLater(new Runnable() {
                    @Override
                    public void run() {
                        if (args.isEmpty()) {
                            JOptionPane.showConfirmDialog(ManageOrder.this, "没有可更新的内容", "信息", JOptionPane.DEFAULT_OPTION);
                            return;
                        }
                        ((JButton) toolbar.getComponent(0)).setEnabled(false);
                        FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_ORDER, args);
                        JOptionPane.showConfirmDialog(ManageOrder.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.DEFAULT_OPTION);
                        if (null != rsp && Service.isResponseSuccess(rsp)) {
                            if (args.has("platform"))   i_platform.setForeground(Color.darkGray);
                            if (args.has("caid"))       i_caid.setForeground(Color.darkGray);
                            if (args.has("create"))     t_create.setForeground(Color.darkGray);
                            args.clear();
                        }
                        ((JButton) toolbar.getComponent(0)).setEnabled(true);
                    }
                });
            }
        });
        ((JButton) toolbar.getComponent(1)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UIToolkit.createOrderItem(Integer.parseInt(i_oid.getText().split("x")[1], 16));
                
                Service.updateOrder();
                updateOrderItem();
            }
        });
    }

    private void updateOrderItem() {
        listpane.getList().removeAllCell();
        Service.map_order.get(Integer.parseInt(i_oid.getText().split("x")[1], 16)).order_items.values().forEach(item->listpane.getList().addCell(new ListCellOrderItem(item)));
    }
}
