package com.ski.stub;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import com.fomjar.widget.FjList;
import com.fomjar.widget.FjListPane;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;
import com.ski.stub.comp.ListCellGame;
import com.ski.stub.comp.ListCellGameAccount;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = -4646332990528380747L;
    
    private JTabbedPane tabs;
    private JToolBar    toolbar;
    
    public MainFrame() {
        setTitle("SKI-STUB [" + Service.getWsiUrl() + ']');
        setSize(500, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
        
        tabs = new JTabbedPane();
        tabs.setFont(CommonUI.FONT.deriveFont(12.0f));
        tabs.add("游戏清单", new FjListPane<BeanGame>());
        tabs.add("游戏账户", new FjListPane<BeanGameAccount>());
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("刷新"));
        toolbar.add(new JButton("新游戏"));
        toolbar.add(new JButton("新账号"));
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(toolbar, BorderLayout.NORTH);
        
        ((JButton) toolbar.getComponent(0)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {updateAll();}
                catch (Exception e1){e1.printStackTrace();}
            }
        });
        
        updateAll();
    }
    
    private void updateAll() {
        new Thread() {
            @Override
            @SuppressWarnings("unchecked")
            public void run() {
                ((JButton) toolbar.getComponent(0)).setEnabled(false);
                Service.updateGame();
                if (!Service.map_game.isEmpty()) {
                    FjList<BeanGame> list = ((FjListPane<BeanGame>) tabs.getComponentAt(0)).getList();
                    list.removeAllCell();
                    Service.map_game.values().forEach(data->list.addCell(new ListCellGame(data)));
                }
                
                Service.updateGameAccount();
                if (!Service.map_game_account.isEmpty()) {
                    FjList<BeanGameAccount> list = ((FjListPane<BeanGameAccount>) tabs.getComponentAt(1)).getList();
                    list.removeAllCell();
                    Service.map_game_account.values().forEach(data->list.addCell(new ListCellGameAccount(data)));
                }
                
                ((JButton) toolbar.getComponent(0)).setEnabled(true);
            }
        }.start();
    }

}
