package com.ski.vcg.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.ski.vcg.common.CommonService;

public class ManageGameAccountRentHistory extends JDialog {

    private static final long serialVersionUID = -5775197067437121539L;

    public ManageGameAccountRentHistory(int gaid) {
        JTable table = new JTable();
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, renderer);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setColumnIdentifiers(new String[] {"时间", "类型", "动作", "用户"});
        CommonService.getGameAccountRentHistoryByGaid(gaid).forEach(m->{
            model.addRow(new String[] {
                    m.t_change,
                    m.i_type == CommonService.RENT_TYPE_A ? "A" : m.i_type == CommonService.RENT_TYPE_B ? "B" : "?",
                    m.i_state == CommonService.RENT_STATE_RENT ? "起租" : m.i_state == CommonService.RENT_STATE_IDLE ? "退租" : "未知",
                    CommonService.getChannelAccountByCaid(m.i_caid).getDisplayName()
            });
        });
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);


        setTitle("管理游戏帐号流水");
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(800, 200));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
    }

}
