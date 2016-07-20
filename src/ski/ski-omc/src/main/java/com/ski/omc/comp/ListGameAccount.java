package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JToolBar;

import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanGame;
import com.ski.common.bean.BeanGameAccount;
import com.ski.omc.UIToolkit;

public class ListGameAccount extends JDialog {

    private static final long serialVersionUID = 8198733950250010530L;
    private JToolBar                    toolbar;
    private FjListPane<BeanGameAccount> pane;
    
    public ListGameAccount() {
        super(MainFrame.getInstance(), "账号清单");
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(800, 600));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("新账号"));
        
        pane = new FjListPane<BeanGameAccount>();
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTypes(new String[] {"按游戏名", "按用户名", "按账号名"});
        pane.getSearchBar().setSearchTips("键入关键词搜索");
        
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            UIToolkit.createGameAccount();
            refresh();
        });
        
        pane.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanGameAccount>(pane.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanGameAccount celldata) {
                if (null == type) return true;
                
                switch(type) {
                case "按游戏名": {
                    List<BeanGame> games = CommonService.getGameAll().values()
                            .stream()
                            .filter(game->{
                                int count = 0;
                                for (String word : words) if (game.c_name_zh.contains(word)) count++;
                                if (count == words.length) return true;
                                else return false;
                            }).collect(Collectors.toList());
                    List<BeanGameAccount> accounts = CommonService.getGameAccountGameAll()
                            .stream()
                            .filter(gag->{
                                for (BeanGame game : games) {
                                    if (game.i_gid == gag.i_gid) return true;
                                }
                                return false;
                            })
                            .map(gag->CommonService.getGameAccountByGaid(gag.i_gaid))
                            .collect(Collectors.toList());
                    for (BeanGameAccount account : accounts) {
                        if (null == account) continue;
                        if (account.i_gaid == celldata.i_gaid) return true;
                    }
                    return false;
                }
                case "按用户名": {
                    List<BeanChannelAccount> users = CommonService.getChannelAccountAll().values()
                            .stream()
                            .filter(user->{
                                int count1 = 0;
                                for (String word : words) if (user.getDisplayName().contains(word)) count1++;
                                if (count1 == words.length) return true;
                                else return false;
                            }).collect(Collectors.toList());
                    List<BeanGameAccount> accounts = CommonService.getGameAccountRentAll()
                            .stream()
                            .filter(rent->{
                                if (CommonService.RENT_STATE_RENT != rent.i_state) return false;
                                for (BeanChannelAccount user : users) {
                                    if (user.i_caid == rent.i_caid) return true;
                                }
                                return false;
                            })
                            .map(rent->CommonService.getGameAccountByGaid(rent.i_gaid))
                            .collect(Collectors.toList());
                    for (BeanGameAccount account : accounts) {
                        if (null == account) continue;
                        if (account.i_gaid == celldata.i_gaid) return true;
                    }
                    return false;
                }
                case "按账号名": {
                    int count = 0;
                    for (String word : words) if (celldata.c_user.contains(word)) count++;
                    return count == words.length;
                }
                default:
                    return true;
                }
            }
        });
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(pane, BorderLayout.CENTER);
        
        refresh();
    }
    
    private void refresh() {
        pane.getList().removeAllCell();
        CommonService.getGameAccountAll().values().forEach(data->pane.getList().addCell(new ListCellGameAccount(data)));
    }

}
