package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.fomjar.widget.FjListCell;
import com.fomjar.widget.FjTextField;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanTicket;
import com.ski.omc.UIToolkit;

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
        
        i_type      = new JLabel("[" + getType2String(data.i_type) + "] ");
        i_type.setFont(i_type.getFont().deriveFont(Font.ITALIC));
        c_title     = new JLabel(data.c_title);
        c_title.setPreferredSize(new Dimension(1, 0));
        i_caid      = new JLabel(CommonService.getChannelAccountByCaid(data.i_caid).getDisplayName());
        i_state     = new JLabel(" [" + getState2String(data.i_state) + "]");
        i_state.setFont(i_state.getFont().deriveFont(Font.ITALIC));
        c_content   = new JLabel(data.c_content);
        c_content.setPreferredSize(new Dimension(1, 0));
        t_time      = new JLabel(" [" + String.format("%s ~ %s", data.t_open, data.t_close) + "]");
        int width   = 400;
        if (data.isClose()) setToolTipText(String.format("<html><h1 width='%dpx'>%s</h1><p width='%dpx'>%s</p><h1 width='%dpx'>%s</h1><p width='%dpx'>%s</p>",
                width,
                "处理意见",
                width,
                data.c_result,
                width,
                data.c_title,
                width,
                data.c_content));
        else setToolTipText(String.format("<html><h1 width='%dpx'>%s</h1><p width='%dpx'>%s</p>", width, data.c_title, width, data.c_content));
        
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
            JLabel      caid  = new JLabel("来源用户: " + CommonService.getChannelAccountByCaid(data.i_caid).getDisplayName());
            JTextField  title = new JTextField(data.c_title);
            title.setEditable(false);
            JTextArea   content = new JTextArea(data.c_content);
            content.setEditable(false);
            content.setColumns(60);
            content.setLineWrap(true);
            content.setRows(10);
            FjTextField result = new FjTextField();
            result.setDefaultTips("(请输入处理意见)");
            
            JPanel head = new JPanel();
            head.setLayout(new GridLayout(2, 1));
            head.add(caid);
            head.add(title);
            
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(head, BorderLayout.NORTH);
            panel.add(new JScrollPane(content), BorderLayout.CENTER);
            panel.add(result, BorderLayout.SOUTH);
            
            if (data.isClose()) {
                result.setEditable(false);
                result.setText("[" + getState2String(data.i_state) + "] " + data.c_result);
                JOptionPane.showMessageDialog(null, panel, "处理结果", JOptionPane.PLAIN_MESSAGE);
            } else {
                int option = JOptionPane.CLOSED_OPTION;
                if (JOptionPane.CLOSED_OPTION != (option = JOptionPane.showOptionDialog(ListCellTicket.this, panel, "处理工单", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"接受", "拒绝"}, "接受"))) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    JSONObject args = new JSONObject();
                    args.put("tid", data.i_tid);
                    args.put("close", sdf.format(new Date()));
                    args.put("state", option == JOptionPane.YES_OPTION ? CommonService.TICKET_STATE_CLOSE : CommonService.TICKET_STATE_CANCEL);
                    args.put("result", result.getText());
                    FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_TICKET, args);
                    UIToolkit.showServerResponse(rsp);
                }
            }
        });
    }
    
    private static String getType2String(int type) {
        switch (type) {
        case CommonService.TICKET_TYPE_REFUND:  return "退款申请";
        case CommonService.TICKET_TYPE_ADVICE:  return "意见建议";
        case CommonService.TICKET_TYPE_MEMORY:  return "备忘事项";
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
    
}
