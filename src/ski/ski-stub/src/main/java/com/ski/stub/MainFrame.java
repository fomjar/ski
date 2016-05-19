package com.ski.stub;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = -4646332990528380747L;
    
    public MainFrame() {
        setTitle("SKI-STUB [" + Service.getWsiUrl() + ']');
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
        
        JTabbedPane tabs = new JTabbedPane();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs);
        
        tabs.setFont(CommonUI.getCommonFont().deriveFont(12.0f));
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {((TabPaneBase)tabs.getSelectedComponent()).update();}
                catch (Exception e1) {e1.printStackTrace();}
            }
        });
        
        tabs.add("更新游戏", new TabUpdateGame());
        tabs.add("更新游戏账户", new TabUpdateGameAccount());
        tabs.add("更新产品", new TabUpdateProduct());
        tabs.add("更新渠道账户", new TabUpdateChannelAccount());
        tabs.add("更新游戏账户租赁", new TabUpdateGameAccountRent());
        tabs.add("游戏账户操作", new TabGameAccountOperate());
        tabs.add("通用消息接口", new TabCMI());
    }

}
