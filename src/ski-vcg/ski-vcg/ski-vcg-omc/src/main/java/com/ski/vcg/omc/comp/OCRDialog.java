package com.ski.vcg.omc.comp;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanChannelAccount;
import com.ski.vcg.common.bean.BeanCommodity;
import com.ski.vcg.omc.Report;

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
