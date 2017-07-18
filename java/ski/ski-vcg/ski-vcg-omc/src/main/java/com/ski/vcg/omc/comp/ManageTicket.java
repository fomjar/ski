package com.ski.vcg.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.fomjar.widget.FjTextField;
import com.ski.vcg.common.CommonDefinition;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanChannelAccount;
import com.ski.vcg.common.bean.BeanTicket;
import com.ski.vcg.omc.UIToolkit;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageTicket extends JDialog {

    private static final long serialVersionUID = 6629310776993089521L;
    private JTextField  caid;
    private JLabel      time;
    private JLabel      title;
    private JTextArea   content;
    private FjTextField result;
    private JButton        btn_accept;
    private JButton        btn_refuse;
    private JButton        btn_cancel;

    public ManageTicket(int tid) {
        super(MainFrame.getInstance());

        BeanTicket             ticket     = CommonService.getTicketByTid(tid);
        BeanChannelAccount     user     = CommonService.getChannelAccountByCaid(ticket.i_caid);

        caid  = new JTextField(String.format("[%s] %s", getChannel2String(user.i_channel), user.getDisplayName()));
        caid.setEditable(false);
        time  = new JLabel(String.format("%19s ~ %19s", ticket.t_open, ticket.t_close));
        title = new JLabel(String.format("[%s] %s", getTypeDesc(ticket.i_type), ticket.c_title));
        content = new JTextArea(ticket.c_content.replace("|", "\n"));
        content.setEditable(false);
        content.setLineWrap(true);
        result = new FjTextField();
        result.setDefaultTips("(请输入处理意见)");
        btn_accept = new JButton("接受");
        btn_refuse = new JButton("拒绝");
        btn_cancel = new JButton("取消");
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1, 3));
        buttons.add(btn_accept);
        buttons.add(btn_refuse);
        buttons.add(btn_cancel);

        JPanel head = new JPanel();
        head.setLayout(new GridLayout(3, 1));
        head.add(UIToolkit.createBasicInfoLabel("来源用户", caid));
        head.add(UIToolkit.createBasicInfoLabel("时    间", time));
        head.add(UIToolkit.createBasicInfoLabel("标    题", title));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(head, BorderLayout.NORTH);
        panel.add(new JScrollPane(content), BorderLayout.CENTER);
        panel.add(UIToolkit.createBasicInfoLabel("处理意见", result), BorderLayout.SOUTH);

        setTitle(String.format("管理工单 - 0x%08X", tid));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 300));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();

                if (btn_cancel == e.getSource()) return;

                int state = CommonService.TICKET_STATE_OPEN;
                if (btn_accept == e.getSource())         state = CommonService.TICKET_STATE_CLOSE;
                else if (btn_refuse == e.getSource())     state = CommonService.TICKET_STATE_CANCEL;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                JSONObject args = new JSONObject();
                args.put("tid",     ticket.i_tid);
                args.put("close",     sdf.format(new Date()));
                args.put("state",     state);
                args.put("result",     result.getText());
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET, args);
                CommonService.updateTicket();
                UIToolkit.showServerResponse(rsp);

                ListTicket.getInstance().refresh();
                if (MainFrame.getInstance().getDetailUser() == user.i_caid) MainFrame.getInstance().setDetailUser(user.i_caid);
            }
        };
        btn_accept.addActionListener(a);
        btn_refuse.addActionListener(a);
        btn_cancel.addActionListener(a);

        if (ticket.isClose()) {
            result.setEditable(false);
            result.setText("[" + getStateDesc(ticket.i_state) + "] " + ticket.c_result);
            buttons.setVisible(false);
        }
    }

    private static String getTypeDesc(int type) {
        switch (type) {
        case CommonService.TICKET_TYPE_ADVICE:     return "意见建议";
        case CommonService.TICKET_TYPE_COMMENT: return "备忘既要";
        case CommonService.TICKET_TYPE_NOTIFY:     return "通知提醒";
        case CommonService.TICKET_TYPE_REFUND:     return "退款申请";
        case CommonService.TICKET_TYPE_RESERVE: return "预约预定";
        default: return "未    知";
        }
    }

    private static String getStateDesc(int state) {
        switch (state) {
        case CommonService.TICKET_STATE_OPEN:     return "打开";
        case CommonService.TICKET_STATE_CLOSE:     return "关闭";
        case CommonService.TICKET_STATE_CANCEL: return "撤销";
        default: return "未知";
        }
    }

    private static String getChannel2String(int channel) {
        switch (channel) {
        case CommonService.CHANNEL_TAOBAO: return "淘  宝";
        case CommonService.CHANNEL_WECHAT: return "微  信";
        case CommonService.CHANNEL_ALIPAY: return "支付宝";
        default: return "未  知";
        }
    }
}
