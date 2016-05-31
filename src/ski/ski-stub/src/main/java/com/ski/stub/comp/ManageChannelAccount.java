package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JToolBar;

import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.ski.stub.Service;
import com.ski.stub.UIToolkit;
import com.ski.stub.bean.BeanChannelAccount;

public class ManageChannelAccount extends JDialog {

    private static final long serialVersionUID = 3337413978610757590L;
    private JToolBar toolbar;
    private FjListPane<BeanChannelAccount> pane;
    
    public ManageChannelAccount(Window owner) {
        super(owner, "管理用户");
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("新用户"));
        pane = new FjListPane<BeanChannelAccount>();
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTypes(new String[] {"按用户名", "按手机号"});
        pane.getSearchBar().setSearchTips("键入关键词搜索");
        
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(pane, BorderLayout.CENTER);
        
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 500);
        setLocation(owner.getX() - (getWidth() - owner.getWidth()) / 2, owner.getY() - (getHeight() - owner.getHeight()) / 2);
        
        registerListener();
        
        updateAll();
    }
    
    private void registerListener() {
        ((JButton) toolbar.getComponent(0)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UIToolkit.createChannelAccount(ManageChannelAccount.this);
                
                Service.updateChannelAccount();
                updateAll();
            }
        });
        
        pane.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanChannelAccount>(pane.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanChannelAccount celldata) {
                int count = 0;
                switch (type) {
                case "按用户名":
                    for (String word : words) if (celldata.c_user.contains(word)) count++;
                    return count == words.length;
                case "按手机号":
                    for (String word : words) if (celldata.c_phone.contains(word)) count++;
                    return count == words.length;
                default:
                    return true;
                }
            }
        });
    }
    
    private void updateAll() {
        pane.getList().removeAllCell();
        Service.map_channel_account.values().forEach(account->{
            pane.getList().addCell(new ListCellChannelAccount(account));
        });
    }

}
