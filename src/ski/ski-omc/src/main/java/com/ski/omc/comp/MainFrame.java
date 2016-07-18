package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.omc.UIToolkit;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 8460467197557992622L;
    
    private static MainFrame instance = null;
    public static MainFrame getInstance() {
        if (null == instance) instance = new MainFrame();
        return instance;
    }
    
    private JToolBar                        toolbar;
    private JPanel                          toolbar_user;
    private FjListPane<BeanChannelAccount>  users;
    private MainDetailPane                  detail;
    private JProgressBar                    progress;
    
    public MainFrame() {
        setTitle(String.format("SKI-OMC-%s [%s]", CommonDefinition.VERSION, CommonService.getWsiUrl()));
        setSize(1280, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("刷"));
        toolbar.addSeparator();
        toolbar.add(new JButton("游"));
        toolbar.add(new JButton("帐"));
        toolbar.add(new JButton("工"));
        toolbar.addSeparator();
        toolbar.add(new JButton("统"));
        
        toolbar_user = new JPanel();
        toolbar_user.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        toolbar_user.setLayout(new BoxLayout(toolbar_user, BoxLayout.X_AXIS));
        toolbar_user.add(DetailPane.createToolBarButton("新用户", e->{
            UIToolkit.createChannelAccount();
            refresh();
        }));
        users   = new FjListPane<BeanChannelAccount>();
        detail  = new MainDetailPane();
        
        users.enableSearchBar();
        users.getSearchBar().setSearchTypes(new String[] {"用户名", "手机号"});
        users.getSearchBar().setSearchTips("键入关键词搜索");
        
        progress = new JProgressBar();
        progress.setFont(UIToolkit.FONT);
        progress.setStringPainted(true);
        
        JPanel panel_user = new JPanel();
        panel_user.setLayout(new BorderLayout());
        panel_user.add(toolbar_user, BorderLayout.NORTH);
        panel_user.add(users, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(detail);
        scroll.getVerticalScrollBar().setUnitIncrement(8);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel_user, scroll);
        split.setDividerSize(2);
        split.setDividerLocation(getWidth() / 4);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(split, BorderLayout.CENTER);
        getContentPane().add(progress, BorderLayout.SOUTH);
        
        listen();
        
        refresh();
    }
    
    private void listen() {
        ((JButton) toolbar.getComponent(0)).addActionListener(e->refresh());
        ((JButton) toolbar.getComponent(2)).addActionListener(e->new ListGame().setVisible(true));
        ((JButton) toolbar.getComponent(3)).addActionListener(e->new ListGameAccount().setVisible(true));
        ((JButton) toolbar.getComponent(4)).addActionListener(e->new ListTicket().setVisible(true));
        ((JButton) toolbar.getComponent(6)).addActionListener(e->new ChartFrame().setVisible(true));
        
        users.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanChannelAccount>(users.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanChannelAccount celldata) {
                int count = 0;
                switch (type) {
                case "用户名":
                    for (String word : words) if (celldata.getDisplayName().contains(word)) count++;
                    return count == words.length;
                case "手机号":
                    for (String word : words) if (celldata.c_phone.contains(word)) count++;
                    return count == words.length;
                default:
                    return true;
                }
            }
        });
    }
    
    private void refresh() {
        UIToolkit.doLater(()->{
            toolbar.getComponent(0).setEnabled(false);

            try {
                int max = 12;
                int cur = 0;
                progress.setMaximum(max);
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载游戏...", cur, max));
                CommonService.updateGame();
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载账号...", cur, max));
                CommonService.updateGameAccount();
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载账号和游戏映射关系...", cur, max));
                CommonService.updateGameAccountGame();
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载账号租赁情况...", cur, max));
                CommonService.updateGameAccountRent();
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载游戏价格...", cur, max));
                CommonService.updateGameRentPrice();
                
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载渠道用户...", cur, max));
                CommonService.updateChannelAccount();
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载平台用户...", cur, max));
                CommonService.updatePlatformAccount();
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载平台用户与渠道用户映射关系...", cur, max));
                CommonService.updatePlatformAccountMap();
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载平台用户金额流水...", cur, max));
                CommonService.updatePlatformAccountMoney();
                
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载订单...", cur, max));
                CommonService.updateOrder();
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载TAG...", cur, max));
                CommonService.updateTag();
                progress.setValue(++cur);   progress.setString(String.format("(%d/%d)正在加载工单...", cur, max));
                CommonService.updateTicket();
                
                progress.setString("加载完成");
                
                users.getList().removeAllCell();
                CommonService.getChannelAccountAll().values().forEach(user->{
                    ListCellUser cell = new ListCellUser(user);
                    users.getList().addCell(cell);
                });
                users.getSearchBar().doSearch();
                
                if (null != detail.getUser()) detail.setUser(detail.getUser().i_caid);
            } catch (Exception e) {progress.setString("加载失败：" + e.getMessage());}
            
            toolbar.getComponent(0).setEnabled(true);
        });
    }
    
    public void setDetailUser(int caid) {
        detail.setUser(caid);
    }

}
