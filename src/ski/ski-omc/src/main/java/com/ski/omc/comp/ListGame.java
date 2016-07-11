package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JToolBar;

import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanGame;
import com.ski.omc.UIToolkit;
import com.ski.omc.comp.ListCellGame;

public class ListGame extends JDialog {

    private static final long serialVersionUID = -4974710094129285415L;
    private JToolBar                toolbar;
    private FjListPane<BeanGame>    pane;
    
    public ListGame() {
        super(MainFrame.getInstance(), "游戏清单");
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(500, 600));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("新游戏"));
        
        pane = new FjListPane<BeanGame>();
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTips("键入游戏名搜索");
        
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            UIToolkit.createGame();
            refresh();
        });
        
        pane.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanGame>(pane.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanGame celldata) {
                    int count = 0;
                    for (String word : words) if (celldata.c_name_zh.contains(word)) count++;
                    return count == words.length;
            }
        });
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(pane, BorderLayout.CENTER);

        refresh();
    }
    
    private void refresh() {
        pane.getList().removeAllCell();
        CommonService.getGameAll().values().forEach(data->pane.getList().addCell(new ListCellGame(data)));
    }

}
