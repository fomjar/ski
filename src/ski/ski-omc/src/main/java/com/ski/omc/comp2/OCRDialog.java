package com.ski.omc.comp2;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanCommodity;
import com.ski.omc.Report;

public class OCRDialog extends ReportDialog {

    private static final long serialVersionUID = -203234202066475026L;
    
    public OCRDialog(BeanCommodity item) {
        BeanChannelAccount user = CommonService.getChannelAccountByCaid(CommonService.getOrderByOid(item.i_oid).i_caid);
        setTitle(String.format("%s的租赁报告", user.getDisplayName()));
        setReport(Report.createOCR(item));
        pack();
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
    }
}
