package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.omc.UIToolkit;

import net.sf.json.JSONObject;

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
    private MainDetailPane                      detail;
    
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
        toolbar.add(new JButton("转"));
        
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
        
        listen();
        
        refresh();
    }
    
    private void listen() {
        ((JButton) toolbar.getComponent(0)).addActionListener(e->refresh());
        ((JButton) toolbar.getComponent(2)).addActionListener(e->new ListGame().setVisible(true));
        ((JButton) toolbar.getComponent(3)).addActionListener(e->new ListGameAccount().setVisible(true));
        ((JButton) toolbar.getComponent(4)).addActionListener(e->new ListTicket().setVisible(true));
        ((JButton) toolbar.getComponent(6)).addActionListener(e->{
            JSONObject args = new JSONObject();
            args.put("user", "fomjar@gmail.com");
            args.put("name", "杜逢佳");
            args.put("money", "0.1");
            args.put("remark", "测试转账");
            System.out.println(CommonService.send("bcs", CommonDefinition.ISIS.INST_ECOM_APPLY_MONEY_TRANSFER, args));
        });
        
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
            
            CommonService.updateGame();
            CommonService.updateGameAccount();
            CommonService.updateGameAccountGame();
            CommonService.updateGameAccountRent();
            CommonService.updateGameRentPrice();
            
            CommonService.updateChannelAccount();
            CommonService.updatePlatformAccount();
            CommonService.updatePlatformAccountMap();
            CommonService.updatePlatformAccountMoney();
            
            CommonService.updateOrder();
            CommonService.updateTag();
            CommonService.updateTicket();
            
            users.getList().removeAllCell();
            CommonService.getChannelAccountAll().values().forEach(user->{
                ListCellUser cell = new ListCellUser(user);
                users.getList().addCell(cell);
            });
            users.getSearchBar().doSearch();
            
            if (null != detail.getUser()) detail.setUser(detail.getUser().i_caid);
            
            toolbar.getComponent(0).setEnabled(true);
        });
    }
    
    public void setDetailUser(int caid) {
        detail.setUser(caid);
    }

}
