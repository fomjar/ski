package com.ski.stub.comp;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.ski.stub.Report;
import com.ski.stub.bean.BeanChannelAccount;

public class UCRDialog extends ReportDialog {

    private static final long serialVersionUID = 7694639516573286407L;
    
    public UCRDialog(BeanChannelAccount user) {
        setTitle(String.format("%s的消费报告", user.c_user));
        setReport(Report.createUCR(user.i_caid));
        pack();
        setSize(new Dimension(800, 600));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
    }

}
