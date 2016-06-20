package com.ski.omc.comp;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.ski.omc.Report;
import com.ski.omc.Service;
import com.ski.omc.bean.BeanChannelAccount;
import com.ski.omc.bean.BeanCommodity;

public class OCRDialog extends ReportDialog {

    private static final long serialVersionUID = -203234202066475026L;
    
    public OCRDialog(BeanCommodity item) {
        BeanChannelAccount user = Service.map_channel_account.get(Service.map_order.get(item.i_oid).i_caid);
        setTitle(String.format("%s的租赁报告", user.c_user));
        setReport(Report.createOCR(item));
        pack();
        setSize(new Dimension(400, getHeight()));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
    }
}
