package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjTextField;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanGame;
import com.ski.omc.UIToolkit;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageGame extends JDialog {

    private static final long serialVersionUID = 6823417802672054421L;
    
    private BeanGame 	game;
    
    private JToolBar    toolbar;
    
    private JTextField 	i_gid;
    private JTextField 	c_name_zh_cn;
    private JTextField	c_name_zh_hk;
    private JTextField 	c_name_en;
    private JTextField 	c_name_ja;
    private JTextField 	c_name_ko;
    private JTextField 	c_name_other;
    private JTextField 	c_platform;
    private JTextField 	c_category;
    private JTextField 	c_language;
    private JTextField 	c_size;
    private JTextField 	c_vendor;
    private JTextField 	t_sale;
    private JTextField 	c_url_icon;
    private JLabel		c_url_icon_label;
    private JTextField 	c_url_cover;
    private JLabel		c_url_cover_label;
    private JPanel 		c_url_poster;
    private JTextArea 	c_introduction;
    private JTextArea 	c_version;
    
    private FjEditLabel	i_price_a;
    private FjEditLabel i_price_b;
    
    private JPanel      panel_tag;
    
    public ManageGame(int gid) {
        super(MainFrame.getInstance());
        
        game = CommonService.getGameByGid(gid);
        
        toolbar         = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("更新"));
        toolbar.add(new JButton("加标签"));
        
        i_gid           = new JTextField();
        i_gid.setEditable(false);
        i_gid.setForeground(Color.gray);
        c_name_zh_cn    = new JTextField();
        c_name_zh_hk    = new JTextField();
        c_name_en       = new JTextField();
        c_name_ja       = new JTextField();
        c_name_ko       = new JTextField();
        c_name_other    = new JTextField();
        c_platform      = new JTextField();
        c_category      = new JTextField();
        c_language      = new JTextField();
        c_size          = new JTextField();
        c_vendor		= new JTextField();
        t_sale			= new JTextField();
        
        c_url_icon      = new JTextField();
        c_url_icon_label = new JLabel();
        c_url_cover    	= new JTextField();
        c_url_cover_label = new JLabel();
        c_url_poster	= new JPanel();
        
        c_introduction	= new JTextArea();
        c_introduction.setLineWrap(true);
        c_introduction.setRows(3);
        c_version		= new JTextArea();
        c_version.setLineWrap(true);
        c_version.setRows(3);
        
        i_price_a       = new FjEditLabel();
        i_price_b       = new FjEditLabel();
        
        panel_tag        = new JPanel();
        
        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "基本信息"));
        panel_basic.setLayout(new BoxLayout(panel_basic, BoxLayout.Y_AXIS));
        panel_basic.add(UIToolkit.createBasicInfoLabel("游戏编号", i_gid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("简中名称", c_name_zh_cn));
        panel_basic.add(UIToolkit.createBasicInfoLabel("繁中名称", c_name_zh_hk));
        panel_basic.add(UIToolkit.createBasicInfoLabel("英文名称", c_name_en));
        panel_basic.add(UIToolkit.createBasicInfoLabel("日文名称", c_name_ja));
        panel_basic.add(UIToolkit.createBasicInfoLabel("韩文名称", c_name_ko));
        panel_basic.add(UIToolkit.createBasicInfoLabel("其他名称", c_name_other));
        panel_basic.add(UIToolkit.createBasicInfoLabel("游戏平台", c_platform));
        panel_basic.add(UIToolkit.createBasicInfoLabel("游戏分类", c_category));
        panel_basic.add(UIToolkit.createBasicInfoLabel("游戏语言", c_language));
        panel_basic.add(UIToolkit.createBasicInfoLabel("游戏大小", c_size));
        panel_basic.add(UIToolkit.createBasicInfoLabel("发行组织", c_vendor));
        panel_basic.add(UIToolkit.createBasicInfoLabel("发行日期", t_sale));
        
        JPanel panel_url = new JPanel();
        panel_url.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "图片管理"));
        panel_url.setLayout(new BoxLayout(panel_url, BoxLayout.Y_AXIS));
        panel_url.add(UIToolkit.createBasicInfoLabel("图标链接", c_url_icon));
        panel_url.add(c_url_icon_label);
        panel_url.add(UIToolkit.createBasicInfoLabel("封面链接", c_url_cover));
        panel_url.add(c_url_cover_label);
        c_url_poster.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "海报"));
        panel_url.add(c_url_poster);
        
        JScrollPane panel_introduction = new JScrollPane(c_introduction);
        panel_introduction.getVerticalScrollBar().setUnitIncrement(20);
        panel_introduction.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "游戏简介"));
        
        JScrollPane panel_version = new JScrollPane(c_version);
        panel_version.getVerticalScrollBar().setUnitIncrement(20);
        panel_version.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "版本说明"));
        
        JPanel panel_price = new JPanel();
        panel_price.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "价格管理"));
        panel_price.setLayout(new BoxLayout(panel_price, BoxLayout.Y_AXIS));
        panel_price.add(UIToolkit.createBasicInfoLabel("A 类租赁", i_price_a));
        panel_price.add(UIToolkit.createBasicInfoLabel("B 类租赁", i_price_b));
        
        panel_tag.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "TAG"));
        panel_tag.setLayout(new FlowLayout(FlowLayout.LEADING));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(panel_basic);
        panel.add(panel_url);
        panel.add(panel_introduction);
        panel.add(panel_version);
        panel.add(panel_price);
        panel.add(panel_tag);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        JScrollPane jsp = new JScrollPane(panel);
        jsp.getVerticalScrollBar().setUnitIncrement(20);
        getContentPane().add(jsp, BorderLayout.CENTER);
        
        setTitle(String.format("管理游戏 - %s", game.c_name_zh_cn));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 440));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        registerListener();
        
        updateBasic();
        updatePrice();
        updateTag();
    }
    
    private JSONObject args = new JSONObject();
    
    private void registerListener() {
    	c_platform.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				List<String> platforms = UIToolkit.chooseMultipleValue(new String[] {"PS3", "PS4", "PSV", "XBOXONE", "XBOX360"}, c_platform.getText().split(" "));
				if (null != platforms) c_platform.setText(platforms.stream().collect(Collectors.joining(" ")));
			}
		});
    	c_category.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				List<String> categories = UIToolkit.chooseMultipleValue(new String[] {"角色扮演", "策略", "动作", "冒险", "恐怖", "射击", "模拟", "赛车", "运动", "休闲", "其他"}, c_category.getText().split(" "));
				if (null != categories) c_category.setText(categories.stream().collect(Collectors.joining(" ")));
			}
		});
    	c_language.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				List<String> languages = UIToolkit.chooseMultipleValue(new String[] {"简体中文", "繁体中文", "英文", "日文", "韩文", "其他"}, c_language.getText().split(" "));
				if (null != languages) c_language.setText(languages.stream().collect(Collectors.joining(" ")));
			}
		});
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            if (args.isEmpty()) {
                JOptionPane.showMessageDialog(ManageGame.this, "没有可更新的内容", "信息", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME, args);
            if (!UIToolkit.showServerResponse(rsp)) return;
            
            if (args.has("platform"))   c_platform.setForeground(Color.darkGray);
            if (args.has("url_icon"))   c_url_icon.setForeground(Color.darkGray);
            if (args.has("url_poster")) c_url_cover.setForeground(Color.darkGray);
            if (args.has("sale"))       t_sale.setForeground(Color.darkGray);
            if (args.has("name_zh_cn"))	c_name_zh_cn.setForeground(Color.darkGray);
            if (args.has("name_en"))    c_name_en.setForeground(Color.darkGray);
            args.clear();
        });
        ((JButton) toolbar.getComponent(1)).addActionListener(e->{
            FjTextField tag = new FjTextField();
            tag.setDefaultTips("(标签)");
            while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ManageGame.this, tag, "加标签", JOptionPane.OK_CANCEL_OPTION)) {
                if (0 == tag.getText().length()) {
                    JOptionPane.showMessageDialog(ManageGame.this, "标签不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                JSONObject args = new JSONObject();
                args.put("type",        CommonService.TAG_GAME);
                args.put("instance",    Integer.parseInt(i_gid.getText().split("x")[1], 16));
                args.put("tag",         tag.getText());
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_TAG, args);
                CommonService.updateTag();
                UIToolkit.showServerResponse(rsp);
                updateTag();
                break;
            }
        });
    }
    
    private void updateBasic() {
    	i_gid.setText(String.format("0x%08X", game.i_gid));
        c_name_zh_cn.setText(game.c_name_zh_cn);
        c_name_zh_hk.setText(game.c_name_zh_hk);
        c_name_en.setText(game.c_name_en);
        c_name_ja.setText(game.c_name_ja);
        c_name_ko.setText(game.c_name_ko);
        c_name_other.setText(game.c_name_other);
        c_platform.setText(game.c_platform);
        c_category.setText(game.c_category);
        c_language.setText(game.c_language);
        c_size.setText(game.c_size);
        c_vendor.setText(game.c_vendor);
        t_sale.setText(game.t_sale);
        c_url_icon.setText(game.c_url_icon);
        c_url_cover.setText(game.c_url_poster);
        t_sale.setText(game.t_sale);
    }
    
    private void updatePrice() {
        i_price_a.setText((null != CommonService.getGameRentPriceByGid(game.i_gid, CommonService.RENT_TYPE_A) ? CommonService.getGameRentPriceByGid(game.i_gid, CommonService.RENT_TYPE_A).i_price : 0.0f) + "元/天");
        i_price_b.setText((null != CommonService.getGameRentPriceByGid(game.i_gid, CommonService.RENT_TYPE_B) ? CommonService.getGameRentPriceByGid(game.i_gid, CommonService.RENT_TYPE_B).i_price : 0.0f) + "元/天");
    }
    
    private void updateTag() {
        panel_tag.removeAll();
        CommonService.getTagByInstance(CommonService.TAG_GAME, game.i_gid)
                .stream()
                .forEach(tag->{
                    JButton btn = new JButton(tag.c_tag);
                    btn.setContentAreaFilled(false);
                    btn.addActionListener(e->{
                        UIToolkit.deleteTag(tag);
                        CommonService.updateTag();
                        updateTag();
                    });
                    panel_tag.add(btn);
                });
        panel_tag.revalidate();
        panel_tag.repaint();
    }
    
    private static class PosterPanel extends JPanel {
    	
    	private JTextField 	url;
    	private JLabel		img;
    	
    	public PosterPanel(String url) {
    		this.url = new JTextField();
    		this.img = new JLabel();
    		setUrl(url);
    		
    		this.url.addActionListener(e->setUrl(this.url.getText()));
    		
    		setLayout(new BorderLayout());
    		add(this.url, BorderLayout.NORTH);
    		add(this.img, BorderLayout.CENTER);
    	}
    	
    	public void setUrl(String url) {
    		this.url.setText(url);
    		this.img.setIcon(UIToolkit.LoadImage(url));
    	}
    }
}
