package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JToolBar;

import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanTicket;
import com.ski.omc.UIToolkit;

public class ListTicket extends JDialog {

    private static final long serialVersionUID = 1066704316297382763L;
    private static final String SWITCH_TITLE_OPEN = "当前显示：未处理工单 (点击切换)";
    private static final String SWITCH_TITLE_ALL  = "当前显示：全部工单 (点击切换)";
    
    private JToolBar                toolbar;
    private JButton                 toggle;
    private FjListPane<BeanTicket>  pane;
    
    public ListTicket() {
        super(MainFrame.getInstance(), "工单清单");
        
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(600, 600));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("新工单"));
        
        toggle = new JButton(SWITCH_TITLE_OPEN);
        toggle.setMargin(new Insets(0, 0, 0, 0));
        
        pane = new FjListPane<BeanTicket>();
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTips("键入关键词搜索");
        pane.getSearchBar().add(toggle, BorderLayout.NORTH);
        
        pane.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanTicket>(pane.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanTicket celldata) {
                if (SWITCH_TITLE_OPEN.equals(toggle.getText()) && celldata.isClose()) return false;
                
                int count = 0;
                for (String word : words) {
                    if (celldata.c_title.contains(word) || celldata.c_content.contains(word))
                        count++;
                }
                return count == words.length;
            }
        });
        
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            UIToolkit.createTicket();
            refresh();
        });
        
        toggle.addActionListener(e->{
            switch (toggle.getText()) {
            case SWITCH_TITLE_OPEN:
                toggle.setText(SWITCH_TITLE_ALL);
                break;
            case SWITCH_TITLE_ALL:
                toggle.setText(SWITCH_TITLE_OPEN);
                break;
            }
            pane.getSearchBar().doSearch();
            pane.getList().repaint();
        });
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(pane, BorderLayout.CENTER);

        refresh();
        
        pane.getSearchBar().doSearch();
    }
    
    private void refresh() {
        pane.getList().removeAllCell();
        CommonService.getTicketAll().values().forEach(data->pane.getList().addCell(new ListCellTicket(data)));
    }

}
