package com.ski.stub.comp;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.ski.stub.Report;
import com.ski.stub.Service;
import com.ski.stub.bean.BeanChannelAccount;
import com.ski.stub.bean.BeanOrderItem;

public class OIRDialog extends ReportDialog {

    private static final long serialVersionUID = -203234202066475026L;
    
    public OIRDialog(BeanOrderItem item) {
        BeanChannelAccount user = Service.map_channel_account.get(Service.map_order.get(item.i_oid).i_caid);
        setTitle(String.format("%s的%s报告", user.c_user, getOperTypeInt2String(item.i_oper_type)));
        setReport(Report.createOIR(item));
        pack();
        setSize(new Dimension(400, getHeight()));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
    }
    
    private static String getOperTypeInt2String(int oper_type) {
        switch (oper_type) {
        case 0: return "购买";
        case 1: return "充值";
        case 2: return "起租";
        case 3: return "退租";
        case 4: return "停租";
        case 5: return "续租";
        case 6: return "换租";
        case 7: return "赠券";
        }
        return null;
    }
}
