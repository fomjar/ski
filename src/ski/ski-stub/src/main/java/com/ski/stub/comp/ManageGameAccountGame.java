package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import com.fomjar.widget.FjList;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;

public class ManageGameAccountGame extends JComponent {

    private static final long serialVersionUID = -51034836551447291L;
    
    private final BeanGameAccount account;
    private JToolBar toolbar;
    private FjList<BeanGame> list;
    
    public ManageGameAccountGame(BeanGameAccount account) {
        this.account = account;
        
        toolbar = new JToolBar();
        toolbar.add(new JButton("添加游戏"));
        list = new FjList<BeanGame>();
        list.setBorder(BorderFactory.createTitledBorder("账号下的游戏"));
        
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(list, BorderLayout.CENTER);
        
        setPreferredSize(new Dimension(400, 400));
        
        registerListener();
        
        updateAll();
    }
    
    private void registerListener() {
        
    }
    
    private void updateAll() {
        
    }
    
}
