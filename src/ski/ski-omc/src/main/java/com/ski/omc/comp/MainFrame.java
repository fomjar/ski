package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjProgressBar;
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
    private FjProgressBar                   progress;
    
    private MainFrame() {
        setTitle(String.format("SKI-OMC-%s [%s]", CommonDefinition.VERSION, CommonService.getWsiUrl()));
        setSize(1280, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
        
        toolbar = new JToolBar() {
            private static final long serialVersionUID = 2704826017104493269L;
            @Override
            public void setEnabled(boolean b) {
                super.setEnabled(b);
                for (Component c : getComponents()) c.setEnabled(b);
            }
        };
        toolbar.setFloatable(false);
        toolbar.add(new JButton("刷新"));
        toolbar.addSeparator();
        toolbar.add(new JButton("游戏"));
        toolbar.add(new JButton("账号"));
        toolbar.add(new JButton("工单"));
        toolbar.addSeparator();
        toolbar.add(new JButton("统计"));
        
        toolbar_user = new JPanel();
        toolbar_user.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        toolbar_user.setLayout(new BoxLayout(toolbar_user, BoxLayout.X_AXIS));
        toolbar_user.add(DetailPane.createToolBarButton("新用户", e->{
            if (-1 != UIToolkit.createChannelAccount()) refreshUser();
        }));
        users   = new FjListPane<BeanChannelAccount>();
        detail  = new MainDetailPane();
        
        users.getList().setSelectable(true);
        users.enableSearchBar();
        users.getSearchBar().setSearchTypes(new String[] {"全搜索", "搜淘宝", "搜微信", "支付宝"});
        users.getSearchBar().setSearchTips("键入关键词搜索");
        
        progress = new FjProgressBar();
        progress.setStringPainted(true);
        
        JPanel panel_user = new JPanel();
        panel_user.setLayout(new BorderLayout());
        panel_user.add(toolbar_user, BorderLayout.NORTH);
        panel_user.add(users, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(detail);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
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
        ((JButton) toolbar.getComponent(2)).addActionListener(e->{ListGame.getInstance().setVisible(true);ListGame.getInstance().refresh();});
        ((JButton) toolbar.getComponent(3)).addActionListener(e->{ListGameAccount.getInstance().setVisible(true);ListGameAccount.getInstance().refresh();});
        ((JButton) toolbar.getComponent(4)).addActionListener(e->{ListTicket.getInstance().setVisible(true);ListTicket.getInstance().refresh();});
        ((JButton) toolbar.getComponent(6)).addActionListener(e->new ChartFrame().setVisible(true));
        
        users.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanChannelAccount>(users.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanChannelAccount celldata) {
                int count = 0;
                switch (type) {
                case "全搜索":
                    for (String word : words) if (celldata.getDisplayName().toLowerCase().contains(word.toLowerCase())) count++;
                    return count == words.length;
                case "搜淘宝":
                	if (celldata.i_channel != CommonService.CHANNEL_TAOBAO) return false;
                    for (String word : words) if (celldata.getDisplayName().toLowerCase().contains(word.toLowerCase())) count++;
                    return count == words.length;
                case "搜微信":
                	if (celldata.i_channel != CommonService.CHANNEL_WECHAT) return false;
                    for (String word : words) if (celldata.getDisplayName().toLowerCase().contains(word.toLowerCase())) count++;
                    return count == words.length;
                case "支付宝":
                	if (celldata.i_channel != CommonService.CHANNEL_ALIPAY) return false;
                    for (String word : words) if (celldata.getDisplayName().toLowerCase().contains(word.toLowerCase())) count++;
                    return count == words.length;
                default:
                    return true;
                }
            }
        });
    }
    
    private void refresh() {
        UIToolkit.doLater(()->{
            toolbar.setEnabled(false);

            try {
                int max = 11;
                int cur = 0;
                String format = "(%d/%d)%s";
                progress.setMaximum(max);
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载游戏..."));
                CommonService.updateGame();
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载账号..."));
                CommonService.updateGameAccount();
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载账号和游戏映射关系..."));
                CommonService.updateGameAccountGame();
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载账号租赁情况..."));
                CommonService.updateGameAccountRent();
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载游戏价格..."));
                CommonService.updateGameRentPrice();
                
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载渠道用户..."));
                CommonService.updateChannelAccount();
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载平台用户..."));
                CommonService.updatePlatformAccount();
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载平台用户与渠道用户映射关系..."));
                CommonService.updatePlatformAccountMap();
                
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载订单..."));
                CommonService.updateOrder();
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载TAG..."));
                CommonService.updateTag();
                progress.setValue(++cur);   progress.setString(String.format(format, cur, max, "正在加载工单..."));
                CommonService.updateTicket();
                
                progress.setString("加载完成");
                
                refreshUser();
            } catch (Exception e) {progress.setString("加载失败：" + e.getMessage());}
            
            toolbar.setEnabled(true);
        });
    }
    
    public void refreshUser() {
    	int caid = null != users.getList().getSelectedCell() ? users.getList().getSelectedCell().getData().i_caid : -1;
    	
        users.getList().removeAllCell();
        List<ListCellUser> user_hl = new LinkedList<ListCellUser>();
        List<ListCellUser> user_ll = new LinkedList<ListCellUser>();
        CommonService.getChannelAccountAll().values().forEach(user->{
            ListCellUser cell = new ListCellUser(user);
            if (user.i_caid == caid) cell.setSelected(true);
            
            if (cell.getForeground() == Color.lightGray) user_ll.add(cell);
            else user_hl.add(cell);
        });
        user_hl.forEach(user->users.getList().addCell(user));
        user_ll.forEach(user->users.getList().addCell(user));
        users.getSearchBar().doSearch();
        
        if (null != detail.getUser()) detail.setUser(detail.getUser().i_caid);
    }
    
    public int getDetailUser() {return detail.getUser().i_caid;}
    
    public void setDetailUser(int caid) {
        detail.setUser(caid);
    }

}
