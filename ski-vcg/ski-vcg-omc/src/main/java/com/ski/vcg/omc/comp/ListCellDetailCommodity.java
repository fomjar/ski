package com.ski.vcg.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjListCell;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanCommodity;
import com.ski.vcg.omc.UIToolkit;

public class ListCellDetailCommodity extends FjListCell<BeanCommodity> {

    private static final long serialVersionUID = -9150442038302281057L;

    private FjEditLabel basic;
    private JLabel      assit;

    public ListCellDetailCommodity(BeanCommodity c) {
        basic   = new FjEditLabel(String.format("%s | %s-%s/%s",
                CommonService.getGameByGaid(Integer.parseInt(c.c_arg0, 16)).get(0).c_name_zh_cn,
                CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16)).c_user,
                c.c_arg1,
                CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16)).c_pass));
        basic.setPreferredSize(new Dimension(0, 0));
        assit   = new JLabel(String.format("%.2f元/天 | %.2f元 | %s",
                c.i_price,
                c.i_expense,
                c.t_begin));

        JPanel panel_info = new JPanel();
        panel_info.setOpaque(false);
        panel_info.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        panel_info.setLayout(new BorderLayout());
        panel_info.add(basic, BorderLayout.CENTER);
        panel_info.add(assit, BorderLayout.EAST);

        JPanel panel_oper = new JPanel();
        panel_oper.setOpaque(false);
        panel_oper.setLayout(new BoxLayout(panel_oper, BoxLayout.X_AXIS));
        panel_oper.add(DetailPane.createToolBarButton("报告", e->new OCRDialog(c).setVisible(true)));
        panel_oper.add(Box.createHorizontalStrut(4));
        panel_oper.add(DetailPane.createToolBarButton("账号", e->new ManageGameAccount(Integer.parseInt(c.c_arg0, 16)).setVisible(true)));
        panel_oper.add(Box.createHorizontalStrut(4));
        JButton b = null;
        panel_oper.add(b = DetailPane.createToolBarButton("退租", e->{
            if (!c.isClose()) {
                UIToolkit.closeCommodity(c.i_oid, c.i_csn);
                MainFrame.getInstance().setDetailUser(CommonService.getOrderByOid(c.i_oid).i_caid);
            }
        }));
        if (c.isClose()) b.setEnabled(false);
        panel_oper.add(Box.createHorizontalStrut(4));
        panel_oper.add(DetailPane.createToolBarButton("流水", e->new ManageGameAccountRentHistory(Integer.parseInt(c.c_arg0, 16)).setVisible(true)));

        setColorDefault(Color.white);

        setLayout(new BorderLayout());
        add(panel_info, BorderLayout.CENTER);
        add(panel_oper, BorderLayout.EAST);

        if (c.isClose()) {
            basic.setForeground(Color.lightGray);
            assit.setForeground(Color.lightGray);
        }

        passthroughMouseEvent(basic);
    }

}
