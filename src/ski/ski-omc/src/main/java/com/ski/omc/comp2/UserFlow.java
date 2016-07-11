package com.ski.omc.comp2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.ski.common.CommonService;
import com.ski.common.bean.BeanPlatformAccountMoney;

public class UserFlow extends JDialog {

    private static final long serialVersionUID = 4379993252792720443L;
    
    public UserFlow(int paid) {
        super(MainFrame2.getInstance());
        
        List<BeanPlatformAccountMoney> money = CommonService.getPlatformAccountMoneyByPaid(paid);
        JTable table = new JTable();
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, renderer);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setColumnIdentifiers(new String[] {"平台用户", "备注", "时间", "操作类型", "基准值", "变化值"});
        money.forEach(m->{
            model.addRow(new String[] {
                    String.format("0x%08X", m.i_paid),
                    m.c_remark,
                    m.t_time,
                    CommonService.MONEY_CONSUME == m.i_type ? "消费" : CommonService.MONEY_CASH == m.i_type ? "充值" : CommonService.MONEY_COUPON == m.i_type ? "充券" : "未知",
                    m.i_base + "元",
                    m.i_money + "元"});
        });
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        
        setTitle("操作记录");
        setModal(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(new Dimension(800, 200));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        setVisible(true);
        
        table.getColumn("备注").setPreferredWidth(280);
        table.getColumn("备注").setCellRenderer(new DefaultTableCellRenderer()); // alignment to left
        table.getColumn("时间").setPreferredWidth(160);
    }

}
