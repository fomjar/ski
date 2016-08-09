package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanTicket;
import com.ski.omc.UIToolkit;

public class ListTicket extends JDialog {

    private static final long serialVersionUID = 1066704316297382763L;
    
    private static ListTicket instance = null;
    public static synchronized ListTicket getInstance() {
    	if (null == instance) instance = new ListTicket();
    	return instance;
    }
    
    private static final String TOGGLE_TITLE_OPEN = "当前显示：开启工单(点击切换)";
    private static final String TOGGLE_TITLE_ALL  = "当前显示：所有工单(点击切换)";
    
    private JToolBar    toolbar;
    private JCheckBox   filter_refund;
    private JCheckBox   filter_advice;
    private JCheckBox   filter_notify;
    private JCheckBox   filter_reserve;
    private JCheckBox   filter_comment;
    private JButton     toggle;
    private FjListPane<BeanTicket>  pane;
    
    private ListTicket() {
        super(MainFrame.getInstance(), "工单清单");
        
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(800, 600));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("新工单"));
        
        filter_refund   = new JCheckBox("退款申请", true);
        filter_advice   = new JCheckBox("意见建议", true);
        filter_notify   = new JCheckBox("通知提醒", true);
        filter_reserve  = new JCheckBox("预约预定", true);
        filter_comment  = new JCheckBox("备忘纪要", true);
        toggle = new JButton(TOGGLE_TITLE_OPEN);
        toggle.setMargin(new Insets(0, 0, 0, 0));
        
        JPanel option_filter = new JPanel();
        option_filter.setLayout(new GridLayout(1, 5));
        option_filter.add(filter_refund);
        option_filter.add(filter_advice);
        option_filter.add(filter_notify);
        option_filter.add(filter_reserve);
        option_filter.add(filter_comment);
        JPanel options = new JPanel();
        options.setLayout(new BorderLayout());
        options.add(option_filter, BorderLayout.WEST);
        options.add(toggle, BorderLayout.CENTER);
        
        pane = new FjListPane<BeanTicket>();
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTips("键入关键词搜索");
        pane.getSearchBar().add(options, BorderLayout.NORTH);
        
        pane.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanTicket>(pane.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanTicket celldata) {
                if (TOGGLE_TITLE_OPEN.equals(toggle.getText()) && celldata.isClose()) return false;
                switch (celldata.i_type) {
                case CommonService.TICKET_TYPE_REFUND:
                    if (!filter_refund.isSelected()) return false;
                    break;
                case CommonService.TICKET_TYPE_ADVICE:
                    if (!filter_advice.isSelected()) return false;
                    break;
                case CommonService.TICKET_TYPE_NOTIFY:
                    if (!filter_notify.isSelected()) return false;
                    break;
                case CommonService.TICKET_TYPE_RESERVE:
                    if (!filter_reserve.isSelected()) return false;
                    break;
                case CommonService.TICKET_TYPE_COMMENT:
                    if (!filter_comment.isSelected()) return false;
                    break;
                }
                
                int count = 0;
                for (String word : words) {
                    if (celldata.c_title.toLowerCase().contains(word.toLowerCase()) || celldata.c_content.toLowerCase().contains(word.toLowerCase()))
                        count++;
                }
                return count == words.length;
            }
        });
        
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            UIToolkit.createTicket();
            refresh();
            ListTicket.getInstance().refresh();
        });
        
        toggle.addActionListener(e->{
            switch (toggle.getText()) {
            case TOGGLE_TITLE_OPEN:
                toggle.setText(TOGGLE_TITLE_ALL);
                break;
            case TOGGLE_TITLE_ALL:
                toggle.setText(TOGGLE_TITLE_OPEN);
                break;
            }
            pane.getSearchBar().doSearch();
        });
        filter_refund.addActionListener(e->pane.getSearchBar().doSearch());
        filter_advice.addActionListener(e->pane.getSearchBar().doSearch());
        filter_notify.addActionListener(e->pane.getSearchBar().doSearch());
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(pane, BorderLayout.CENTER);

        refresh();
    }
    
    public void refresh() {
        pane.getList().removeAllCell();
        CommonService.getTicketAll().values().forEach(data->{
            ListCellTicket cell = new ListCellTicket(data);
            pane.getList().addCell(cell);
        });
        
        pane.getSearchBar().doSearch();
    }

}
