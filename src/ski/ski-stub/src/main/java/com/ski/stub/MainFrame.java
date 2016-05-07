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
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
        
        JTabbedPane tabs = new JTabbedPane();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs);
        
        tabs.setFont(CommonUI.getCommonFont());
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {((TabPaneBase)tabs.getSelectedComponent()).update();}
                catch (Exception e1) {e1.printStackTrace();}
            }
        });
        
        tabs.add("录入游戏", new TabRecordGame());
        tabs.add("录入账号", new TabRecordAccount());
        tabs.add("创建产品", new TabRecordProduct());
    }

}
