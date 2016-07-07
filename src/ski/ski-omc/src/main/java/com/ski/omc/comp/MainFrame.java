package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import com.fomjar.widget.FjList;
import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanGame;
import com.ski.common.bean.BeanGameAccount;
import com.ski.common.bean.BeanTicket;
import com.ski.omc.UIToolkit;

public class MainFrame extends JFrame {
    
    private static MainFrame instance = null;
    
    public static synchronized MainFrame getInstance() {
        if (null == instance) instance = new MainFrame();
        return instance;
    }

    private static final long serialVersionUID = -4646332990528380747L;
    private static final String SWITCH_TITLE_OPEN = "当前显示：未处理工单 (点击切换)";
    private static final String SWITCH_TITLE_ALL  = "当前显示：全部工单 (点击切换)";
    
    private JTabbedPane tabs;
    private JMenuBar    menubar;
    private JToolBar    toolbar;
    private JButton     ticket_switch;
    
    private MainFrame() {
        setTitle(String.format("SKI-OMC-%s [%s]", CommonDefinition.VERSION, CommonService.getWsiUrl()));
        setSize(600, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
        
        tabs = new JTabbedPane();
        tabs.setFont(UIToolkit.FONT.deriveFont(12.0f));
        tabs.add("游戏清单", new FjListPane<BeanGame>());
        tabs.add("账号清单", new FjListPane<BeanGameAccount>());
        tabs.add("用户清单", new FjListPane<BeanChannelAccount>());
        tabs.add("工单清单", new FjListPane<BeanTicket>());
        ((FjListPane<?>) tabs.getComponentAt(0)).enableSearchBar();
        ((FjListPane<?>) tabs.getComponentAt(0)).getSearchBar().setSearchTips("键入游戏名搜索");
        ((FjListPane<?>) tabs.getComponentAt(1)).enableSearchBar();
        ((FjListPane<?>) tabs.getComponentAt(1)).getSearchBar().setSearchTypes(new String[] {"按游戏名", "按用户名", "按账号名"});
        ((FjListPane<?>) tabs.getComponentAt(1)).getSearchBar().setSearchTips("键入关键词搜索");
        ((FjListPane<?>) tabs.getComponentAt(2)).enableSearchBar();
        ((FjListPane<?>) tabs.getComponentAt(2)).getSearchBar().setSearchTypes(new String[] {"按用户名", "按手机号"});
        ((FjListPane<?>) tabs.getComponentAt(2)).getSearchBar().setSearchTips("键入关键词搜索");
        ((FjListPane<?>) tabs.getComponentAt(3)).enableSearchBar();
        ((FjListPane<?>) tabs.getComponentAt(3)).getSearchBar().setSearchTips("键入关键词搜索");
        
        ticket_switch = new JButton(SWITCH_TITLE_OPEN);
        ticket_switch.setMargin(new Insets(0, 0, 0, 0));
        ((FjListPane<?>) tabs.getComponentAt(3)).getSearchBar().add(ticket_switch, BorderLayout.NORTH);
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton refresh = new JButton(new ImageIcon(UIToolkit.loadImage("/com/ski/omc/refresh.png", 0.22)));
        toolbar.add(refresh);
        
        menubar = createMenuBar();

        setJMenuBar(menubar);
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
                updateAll();
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
                    List<BeanGameAccount> accounts = CommonService.getRentGameAccountAll()
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
        FjListPane<BeanChannelAccount> paneuser = ((FjListPane<BeanChannelAccount>) tabs.getComponentAt(2));
        paneuser.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanChannelAccount>(paneuser.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanChannelAccount celldata) {
                int count = 0;
                switch (type) {
                case "按用户名":
                    for (String word : words) if (celldata.getDisplayName().contains(word)) count++;
                    return count == words.length;
                case "按手机号":
                    for (String word : words) if (celldata.c_phone.contains(word)) count++;
                    return count == words.length;
                default:
                    return true;
                }
            }
        });
        FjListPane<BeanTicket> paneticket = ((FjListPane<BeanTicket>) tabs.getComponentAt(3));
        paneticket.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanTicket>(paneticket.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanTicket celldata) {
                if (SWITCH_TITLE_OPEN.equals(ticket_switch.getText()) && celldata.isClose()) return false;
                
                int count = 0;
                for (String word : words) {
                    if (celldata.c_title.contains(word) || celldata.c_content.contains(word))
                        count++;
                }
                return count == words.length;
            }
        });
        ticket_switch.addActionListener(e->{
            switch (ticket_switch.getText()) {
            case SWITCH_TITLE_OPEN:
                ticket_switch.setText(SWITCH_TITLE_ALL);
                break;
            case SWITCH_TITLE_ALL:
                ticket_switch.setText(SWITCH_TITLE_OPEN);
                break;
            }
            paneticket.getSearchBar().doSearch();
            paneticket.getList().repaint();
        });
    }
    
    private JMenuBar createMenuBar() {
        JMenu       mconfig = new JMenu("创建");
        JMenuItem   micreategame        = new JMenuItem("创建游戏");
        JMenuItem   micreategameaccount = new JMenuItem("创建账号");
        JMenuItem   micreateuser        = new JMenuItem("创建用户");
        JMenuItem   micreateorder       = new JMenuItem("创建订单");
        JMenuItem   micreateticket      = new JMenuItem("创建工单");
        mconfig.add(micreategame);
        mconfig.add(micreategameaccount);
        mconfig.add(micreateuser);
        mconfig.add(micreateorder);
        mconfig.add(micreateticket);
        
        JMenu       mhelp   = new JMenu("帮助");
        JMenuItem   miabout = new JMenuItem("关于");
        mhelp.add(miabout);
        
        JMenuBar    menubar = new JMenuBar();
        menubar.add(mconfig);
        menubar.add(mhelp);
        
        micreategame        .addActionListener(e->UIToolkit.createGame());
        micreategameaccount .addActionListener(e->UIToolkit.createGameAccount());
        micreateuser        .addActionListener(e->UIToolkit.createChannelAccount());
        micreateorder       .addActionListener(e->UIToolkit.createOrder());
        micreateticket      .addActionListener(e->UIToolkit.createTicket());
        
        miabout.addActionListener(e->JOptionPane.showMessageDialog(MainFrame.this, "SKI平台操作维护中心(OMC)，版本：" + CommonDefinition.VERSION, "关于", JOptionPane.PLAIN_MESSAGE));
        
        return menubar;
    }
    
    public void updateAll() {
        UIToolkit.doLater(()->{
            ((JButton) toolbar.getComponent(0)).setEnabled(false);
            boolean isfail = false;
            try {
                CommonService.updateGame();
                CommonService.updateGameRentPrice();
                
                CommonService.updateGameAccount();
                CommonService.updateGameAccountGame();
                CommonService.updateGameAccountRent();
                
                CommonService.updateChannelAccount();
                CommonService.updatePlatformAccount();
                CommonService.updatePlatformAccountMap();
                CommonService.updatePlatformAccountMoney();
                
                CommonService.updateOrder();
                
                CommonService.updateTag();
                
                CommonService.updateTicket();
                
                @SuppressWarnings("unchecked")
                FjList<BeanGame> list_game = ((FjListPane<BeanGame>) tabs.getComponentAt(0)).getList();
                list_game.removeAllCell();
                CommonService.getGameAll().values().forEach(data->list_game.addCell(new ListCellGame(data)));
                ((FjListPane<?>) tabs.getComponentAt(0)).getSearchBar().doSearch();
                
                @SuppressWarnings("unchecked")
                FjList<BeanGameAccount> list_game_account = ((FjListPane<BeanGameAccount>) tabs.getComponentAt(1)).getList();
                list_game_account.removeAllCell();
                CommonService.getGameAccountAll().values().forEach(data->list_game_account.addCell(new ListCellGameAccount(data)));
                ((FjListPane<?>) tabs.getComponentAt(1)).getSearchBar().doSearch();
                
                @SuppressWarnings("unchecked")
                FjList<BeanChannelAccount> list_channel_account = ((FjListPane<BeanChannelAccount>) tabs.getComponentAt(2)).getList();
                list_channel_account.removeAllCell();
                CommonService.getChannelAccountAll().values().forEach(account->{list_channel_account.addCell(new ListCellChannelAccount(account));});
                ((FjListPane<?>) tabs.getComponentAt(2)).getSearchBar().doSearch();
                
                @SuppressWarnings("unchecked")
                FjList<BeanTicket> list_ticket = ((FjListPane<BeanTicket>) tabs.getComponentAt(3)).getList();
                list_ticket.removeAllCell();
                CommonService.getTicketAll().values().forEach(data->list_ticket.addCell(new ListCellTicket(data)));
                ((FjListPane<?>) tabs.getComponentAt(3)).getSearchBar().doSearch();
            } catch (Exception e) {
                isfail = true;
                e.printStackTrace();
            }
            
            if (isfail) JOptionPane.showMessageDialog(MainFrame.this, "刷新过程中可能发生错误，重新尝试可能解决", "错误", JOptionPane.ERROR_MESSAGE);
            
            ((JButton) toolbar.getComponent(0)).setEnabled(true);
        });
    }
}