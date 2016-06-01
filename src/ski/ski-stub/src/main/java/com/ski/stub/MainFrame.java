package com.ski.stub;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import com.fomjar.widget.FjList;
import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;
import com.ski.stub.comp.ListCellGame;
import com.ski.stub.comp.ListCellGameAccount;
import com.ski.stub.comp.ManageChannelAccount;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = -4646332990528380747L;
    
    private JTabbedPane tabs;
    private JToolBar    toolbar;
    
    public MainFrame() {
        setTitle("SKI-STUB-0.0.1 [" + Service.getWsiUrl() + ']');
        setSize(500, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
        
        tabs = new JTabbedPane();
        tabs.setFont(UIToolkit.FONT.deriveFont(12.0f));
        tabs.add("游戏管理", new FjListPane<BeanGame>());
        tabs.add("账号管理", new FjListPane<BeanGameAccount>());
        ((FjListPane<?>) tabs.getComponentAt(0)).enableSearchBar();
        ((FjListPane<?>) tabs.getComponentAt(0)).getSearchBar().setSearchTips("键入关键词搜索");
        ((FjListPane<?>) tabs.getComponentAt(1)).enableSearchBar();
        ((FjListPane<?>) tabs.getComponentAt(1)).getSearchBar().setSearchTypes(new String[] {"按账号", "按游戏名"});
        ((FjListPane<?>) tabs.getComponentAt(1)).getSearchBar().setSearchTips("键入关键词搜索");
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("刷新"));
        toolbar.add(new JButton("新游戏"));
        toolbar.add(new JButton("新账号"));
        toolbar.add(new JButton("管理用户"));
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(toolbar, BorderLayout.NORTH);
        
        registerListener();
        
        updateAll();
    }
    
    @SuppressWarnings("unchecked")
    private void registerListener() {
        ((JButton) toolbar.getComponent(0)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {updateAll();}
                catch (Exception e1){e1.printStackTrace();}
            }
        });
        ((JButton) toolbar.getComponent(1)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UIToolkit.createGame(MainFrame.this);
                
                Service.updateGame();
                updateAll();
            }
        });
        ((JButton) toolbar.getComponent(2)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UIToolkit.createGameAccount(MainFrame.this);
                
                Service.updateGameAccount();
                updateAll();
            }
        });
        ((JButton) toolbar.getComponent(3)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ManageChannelAccount(MainFrame.this).setVisible(true);
            }
        });
        FjListPane<BeanGame> panegame = ((FjListPane<BeanGame>) tabs.getComponentAt(0));
        panegame.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanGame>(panegame.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanGame celldata) {
                    int count = 0;
                    for (String word : words) if (celldata.c_name_zh.contains(word)) count++;
                    return count == words.length;
            }
        });
        FjListPane<BeanGameAccount> panegameaccount = ((FjListPane<BeanGameAccount>) tabs.getComponentAt(1));
        panegameaccount.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanGameAccount>(panegameaccount.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanGameAccount celldata) {
                if (null == type) return true;
                
                switch(type) {
                case "按账号":
                    int count = 0;
                    for (String word : words) if (celldata.c_user.contains(word)) count++;
                    return count == words.length;
                case "按游戏名":
                    List<BeanGame> games = Service.map_game.values().stream().filter(game->{
                        int count1 = 0;
                        for (String word : words) if (game.c_name_zh.contains(word)) count1++;
                        if (count1 == words.length) return true;
                        else return false;
                    }).collect(Collectors.toList());
                    Map<Integer, BeanGameAccount> accounts = new LinkedHashMap<Integer, BeanGameAccount>();
                    games.forEach(game->{
                        Service.set_game_account_game.forEach(pair->{
                            if (game.i_gid == pair.i_gid) {
                                if (!accounts.containsKey(pair.i_gaid)) {
                                    accounts.put(pair.i_gaid, Service.map_game_account.get(pair.i_gaid));
                                }
                            }
                        });
                    });
                    for (BeanGameAccount account : accounts.values())
                        if (account.i_gaid == celldata.i_gaid) return true;
                    return false;
                default:
                    return true;
                }
            }
        });
    }
    
    private void updateAll() {
        Service.doLater(new Runnable() {
            @Override
            @SuppressWarnings("unchecked")
            public void run() {
                ((JButton) toolbar.getComponent(0)).setEnabled(false);
                boolean isfail = false;
                try {
                    if (Service.updateGame()) {
                        FjList<BeanGame> list = ((FjListPane<BeanGame>) tabs.getComponentAt(0)).getList();
                        list.removeAllCell();
                        Service.map_game.values().forEach(data->list.addCell(new ListCellGame(data)));
                    } else isfail = true;
                    
                    if (Service.updateGameAccount()) {
                        FjList<BeanGameAccount> list = ((FjListPane<BeanGameAccount>) tabs.getComponentAt(1)).getList();
                        list.removeAllCell();
                        Service.map_game_account.values().forEach(data->list.addCell(new ListCellGameAccount(data)));
                    } else isfail = true;
                    
                    if (Service.updateGameAccountGame()) {}
                    else isfail = true;
                    
                    if (Service.updateChannelAccount()) {}
                    else isfail = true;
                    
                    if (Service.updateOrder()) {}
                    else isfail = true;
                } catch (Exception e) {
                    isfail = true;
                    e.printStackTrace();
                }
                
                if (isfail) JOptionPane.showConfirmDialog(null, "刷新过程中可能发生错误，重新尝试可能解决", "错误", JOptionPane.DEFAULT_OPTION);
                
                ((JButton) toolbar.getComponent(0)).setEnabled(true);
            }
        });
    }
}