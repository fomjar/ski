package com.ski.vcg.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.SwingUtilities;

import com.fomjar.widget.FjListCell;
import com.fomjar.widget.FjTextField;
import com.ski.vcg.common.CommonDefinition;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanChannelAccount;
import com.ski.vcg.common.bean.BeanTicket;
import com.ski.vcg.omc.UIToolkit;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ListCellTicket extends FjListCell<BeanTicket> {

    private static final long serialVersionUID = 2600073462045086881L;
    
    private JLabel i_type;
    private JLabel c_title;
    private JLabel i_caid;
    private JLabel i_state;
    private JLabel t_time;
    private JLabel c_content;

    public ListCellTicket(BeanTicket data) {
        super(data);
        
        BeanChannelAccount user = CommonService.getChannelAccountByCaid(data.i_caid);
        i_type      = new JLabel("[" + getType2String(data.i_type) + "] ");
        i_type.setFont(i_type.getFont().deriveFont(Font.ITALIC));
        c_title     = new JLabel(data.c_title);
        c_title.setPreferredSize(new Dimension(1, 0));
        i_caid      = new JLabel(user.getDisplayName() + " [" + getPlatform(user.i_channel) + "]");
        i_state     = new JLabel(" [" + getState2String(data.i_state) + "]");
        i_state.setFont(i_state.getFont().deriveFont(Font.ITALIC));
        c_content   = new JLabel(data.c_content);
        c_content.setPreferredSize(new Dimension(1, 0));
        t_time      = new JLabel(String.format("[%19s ~ %19s]", data.t_open, data.t_close));

        if (data.isClose()) {
            i_type.setForeground(Color.lightGray);
            c_title.setForeground(Color.lightGray);
            i_caid.setForeground(Color.lightGray);
            i_state.setForeground(Color.lightGray);
            c_content.setForeground(Color.lightGray);
            t_time.setForeground(Color.lightGray);
        } else {
            i_type.setForeground(color_major);
            c_title.setForeground(color_major);
            i_caid.setForeground(color_minor);
            i_state.setForeground(color_major);
            c_content.setForeground(color_minor);
            t_time.setForeground(color_minor);
        }
        
        JPanel panel_state = new JPanel();
        panel_state.setOpaque(false);
        panel_state.setLayout(new BorderLayout());
        panel_state.add(i_caid, BorderLayout.CENTER);
        panel_state.add(i_state, BorderLayout.EAST);
        
        JPanel panel_up = new JPanel();
        panel_up.setOpaque(false);
        panel_up.setLayout(new BorderLayout());
        panel_up.add(i_type, BorderLayout.WEST);
        panel_up.add(c_title, BorderLayout.CENTER);
        panel_up.add(panel_state, BorderLayout.EAST);
        
        JPanel panel_down = new JPanel();
        panel_down.setOpaque(false);
        panel_down.setLayout(new BorderLayout());
        panel_down.add(c_content, BorderLayout.CENTER);
        panel_down.add(t_time, BorderLayout.EAST);
        
        setLayout(new GridLayout(2, 1));
        add(panel_up);
        add(panel_down);
        
        addActionListener(e->{
            JTextField  caid  = new JTextField(String.format("[%s] %s", getPlatform(user.i_channel), user.getDisplayName()));
            caid.setEditable(false);
            JLabel      time  = new JLabel(String.format("%19s ~ %19s", data.t_open, data.t_close));
            JLabel      title = new JLabel(data.c_title);
            JTextArea   content = new JTextArea(data.c_content.replace("|", "\n"));
            content.setEditable(false);
            content.setLineWrap(true);
            FjTextField result = new FjTextField();
            result.setDefaultTips("(请输入处理意见)");
            JButton        btn_accept = new JButton("接受");
            JButton        btn_refuse = new JButton("拒绝");
            JButton        btn_cancel = new JButton("取消");
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
            
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "处理工单");
            dialog.setSize(400, 300);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setLocation((screen.width - dialog.getWidth()) / 2, (screen.height - dialog.getHeight()) / 2);
            dialog.setModal(false);
            dialog.setLayout(new BorderLayout());
            dialog.add(panel, BorderLayout.CENTER);
            dialog.add(buttons, BorderLayout.SOUTH);
            
            ActionListener a = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                    
                    if (btn_cancel == e.getSource()) return;
                    
                    int state = CommonService.TICKET_STATE_OPEN;
                    if (btn_accept == e.getSource())         state = CommonService.TICKET_STATE_CLOSE;
                    else if (btn_refuse == e.getSource())     state = CommonService.TICKET_STATE_CANCEL;
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    JSONObject args = new JSONObject();
                    args.put("tid",     data.i_tid);
                    args.put("close",     sdf.format(new Date()));
                    args.put("state",     state);
                    args.put("result",     result.getText());
                    FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET, args);
                    CommonService.updateTicket();
                    UIToolkit.showServerResponse(rsp);
                    ListTicket.getInstance().refresh();
                }
            };
            btn_accept.addActionListener(a);
            btn_refuse.addActionListener(a);
            btn_cancel.addActionListener(a);
            
            if (data.isClose()) {
                result.setEditable(false);
                result.setText("[" + getState2String(data.i_state) + "] " + data.c_result);
                buttons.setVisible(false);
            }
            dialog.setVisible(true);
        });
    }
    
    private static String getType2String(int type) {
        switch (type) {
        case CommonService.TICKET_TYPE_REFUND:  return "退款申请";
        case CommonService.TICKET_TYPE_ADVICE:  return "意见建议";
        case CommonService.TICKET_TYPE_NOTIFY:  return "通知提醒";
        case CommonService.TICKET_TYPE_RESERVE: return "预约预定";
        case CommonService.TICKET_TYPE_COMMENT: return "备忘纪要";
        default: return "未    知";
        }
    }
    
    public static String getState2String(int state) {
        switch (state) {
        case CommonService.TICKET_STATE_OPEN:   return "打开";
        case CommonService.TICKET_STATE_CLOSE:  return "关闭";
        case CommonService.TICKET_STATE_CANCEL: return "取消";
        default: return "未知";
        }
    }
    
    private static String getPlatform(int channel) {
        switch (channel) {
        case CommonService.CHANNEL_TAOBAO: return "淘  宝";
        case CommonService.CHANNEL_WECHAT: return "微  信";
        case CommonService.CHANNEL_ALIPAY: return "支付宝";
        default: return "未  知";
        }
    }
}
