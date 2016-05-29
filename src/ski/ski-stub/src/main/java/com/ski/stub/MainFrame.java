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
import com.fomjar.widget.FjListCell;
import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar.FjSearchListener;
import com.ski.common.SkiCommon;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;
import com.ski.stub.comp.ListCellGame;
import com.ski.stub.comp.ListCellGameAccount;
import com.ski.stub.comp.NewGame;
import com.ski.stub.comp.NewGameAccount;

import fomjar.server.msg.FjDscpMessage;

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
        ((FjListPane<?>) tabs.getComponentAt(0)).getSearchBar().setSearchTypes(new String[] {"按游戏名"});
        ((FjListPane<?>) tabs.getComponentAt(0)).getSearchBar().setSearchTips("键入关键词搜索");
        ((FjListPane<?>) tabs.getComponentAt(1)).enableSearchBar();
        ((FjListPane<?>) tabs.getComponentAt(1)).getSearchBar().setSearchTypes(new String[] {"按账号", "按游戏名"});
        ((FjListPane<?>) tabs.getComponentAt(1)).getSearchBar().setSearchTips("键入关键词搜索");
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("刷新"));
        toolbar.add(new JButton("新游戏"));
        toolbar.add(new JButton("新账号"));
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(toolbar, BorderLayout.NORTH);
        
        registerListener();
        
        updateAll();
    }
    
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
                NewGame game = new NewGame();
                int ret = JOptionPane.showConfirmDialog(MainFrame.this, game, "新游戏", JOptionPane.OK_CANCEL_OPTION);
                if (JOptionPane.OK_OPTION == ret) {
                    FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME, game.toJson());
                    JOptionPane.showConfirmDialog(MainFrame.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.CLOSED_OPTION);
                }
            }
        });
        ((JButton) toolbar.getComponent(2)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewGameAccount gameaccount = new NewGameAccount();
                int ret = JOptionPane.showConfirmDialog(MainFrame.this, gameaccount, "新账号", JOptionPane.OK_CANCEL_OPTION);
                if (JOptionPane.OK_OPTION == ret) {
                    FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, gameaccount.toJson());
                    JOptionPane.showConfirmDialog(MainFrame.this, null != rsp ? rsp.toString() : null, "服务器响应", JOptionPane.CLOSED_OPTION);
                }
            }
        });
        ((FjListPane<?>) tabs.getComponentAt(0)).getSearchBar().addSearchListener(new FjSearchListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void searchPerformed(String type, String[] words) {
                if (null == type) return;
                List<FjListCell<BeanGame>> cells = ((FjListPane<BeanGame>) tabs.getComponentAt(0)).getList().getCells();
                if (null == words || 0 == words.length) {
                    cells.forEach(cell->cell.setVisible(true));
                    return;
                }
                
                switch(type) {
                case "按游戏名":
                    cells.forEach(cell->{
                        BeanGame bean = cell.getData();
                        int count = 0;
                        for (String word : words) if (bean.c_name_zh.contains(word)) count++;
                        
                        if (count == words.length) cell.setVisible(true);
                        else cell.setVisible(false);
                    });
                    ((FjListPane<BeanGame>) tabs.getComponentAt(0)).getList().revalidate();
                    break;
                }
            }
        });
        ((FjListPane<?>) tabs.getComponentAt(1)).getSearchBar().addSearchListener(new FjSearchListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void searchPerformed(String type, String[] words) {
                if (null == type) return;
                List<FjListCell<BeanGameAccount>> cells = ((FjListPane<BeanGameAccount>) tabs.getComponentAt(1)).getList().getCells();
                if (null == words || 0 == words.length) {
                    cells.forEach(cell->cell.setVisible(true));
                    return;
                }
                
                switch(type) {
                case "按账号":
                    cells.forEach(cell->{
                        BeanGameAccount bean = cell.getData();
                        int count = 0;
                        for (String word : words) if (bean.c_user.contains(word)) count++;
                        
                        if (count == words.length) cell.setVisible(true);
                        else cell.setVisible(false);
                    });
                    ((FjListPane<BeanGameAccount>) tabs.getComponentAt(1)).getList().revalidate();
                    break;
                case "按游戏名":
                    List<BeanGame> games = Service.map_game.values().stream().filter(game->{
                        int count = 0;
                        for (String word : words) if (game.c_name_zh.contains(word)) count++;
                        if (count == words.length) return true;
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
                    cells.forEach(cell->{
                        boolean found = false;
                        for (BeanGameAccount account : accounts.values()) {
                            if (account.i_gaid == cell.getData().i_gaid) {
                                found = true;
                                break;
                            }
                        }
                        if (found) cell.setVisible(true);
                        else cell.setVisible(false);
                    });
                    break;
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
                try {
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
                    
                    Service.updateGameAccountGame();
                } catch (Exception e) {e.printStackTrace();}
                
                ((JButton) toolbar.getComponent(0)).setEnabled(true);
            }
        });
    }
}