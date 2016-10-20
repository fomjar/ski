package com.ski.vcg.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjEditLabel.EditListener;
import com.fomjar.widget.FjTextField;
import com.ski.vcg.common.CommonDefinition;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanGame;
import com.ski.vcg.omc.UIToolkit;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageGame extends JDialog {

    private static final long serialVersionUID = 6823417802672054421L;
    
    private BeanGame     game;
    
    private JToolBar    toolbar;
    
    private JTextField      i_gid;
    private JTextField      c_name_zh_cn;
    private JTextField      c_name_zh_hk;
    private JTextField      c_name_en;
    private JTextField      c_name_ja;
    private JTextField      c_name_ko;
    private JTextField      c_name_other;
    private JTextField      c_platform;
    private JTextField      c_category;
    private JTextField      c_language;
    private JTextField      c_size;
    private JTextField      c_vendor;
    private JTextField      t_sale;
    private JTextField      c_url_icon;
    private JLabel          c_url_icon_label;
    private JTextField      c_url_cover;
    private JLabel          c_url_cover_label;
    private JPanel          c_url_poster;
    private JButton         c_url_poster_add;
    private JTextArea       c_introduction;
    private JTextArea       c_version;
    private JTextField      c_vedio;
    private JComboBox<String>  i_associator;
    private JTextField      c_play_mode;
    private JTextField      c_peripheral;
    private JTextArea       c_editor_word;
    private JTextField      i_ign_score;
    private JTextField      c_producer;
    
    private FjEditLabel     i_price_a;
    private FjEditLabel     i_price_b;
    
    private JPanel      panel_tag;
    
    public ManageGame(int gid) {
        super(MainFrame.getInstance());
        
        game = CommonService.getGameByGid(gid);
        
        setTitle(String.format("管理游戏 - %s", game.c_name_zh_cn));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(500, 440));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
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
        c_vendor        = new JTextField();
        t_sale            = new JTextField();
        
        c_url_icon      = new JTextField();
        c_url_icon_label = new JLabel();
        c_url_cover        = new JTextField();
        c_url_cover_label = new JLabel();
        c_url_poster    = new JPanel();
        c_url_poster_add = new JButton("增加海报");
        
        c_introduction    = new JTextArea();
        c_introduction.setLineWrap(true);
        c_introduction.setRows(3);
        c_version        = new JTextArea();
        c_version.setLineWrap(true);
        c_version.setRows(3);
        
        c_vedio = new JTextField();
        
        i_associator    = new JComboBox<String>(new String[] {"否", "是"});
        c_play_mode     = new JTextField();
        c_peripheral    = new JTextField();
        c_editor_word   = new JTextArea();
        c_editor_word.setLineWrap(true);
        c_editor_word.setRows(3);
        i_ign_score     = new JTextField();
        c_producer      = new JTextField();
        
        i_gid.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_name_zh_cn.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_name_zh_hk.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_name_en.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_name_ja.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_name_ko.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_name_other.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_platform.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_category.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_language.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_size.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_vendor.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        t_sale.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        
        i_associator.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_play_mode.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_peripheral.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_editor_word.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        i_ign_score.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_producer.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        
        c_url_icon.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        c_url_cover.setPreferredSize(new Dimension(1, i_gid.getPreferredSize().height));
        
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
        panel_basic.add(UIToolkit.createBasicInfoLabel("开发组织", c_vendor));
        panel_basic.add(UIToolkit.createBasicInfoLabel("发行日期", t_sale));
        panel_basic.add(UIToolkit.createBasicInfoLabel("需要会员", i_associator));
        panel_basic.add(UIToolkit.createBasicInfoLabel("游戏模式", c_play_mode));
        panel_basic.add(UIToolkit.createBasicInfoLabel("外接设备", c_peripheral));
        panel_basic.add(UIToolkit.createBasicInfoLabel("IGN评分", i_ign_score));
        panel_basic.add(UIToolkit.createBasicInfoLabel("制作人", c_producer));
        panel_basic.add(UIToolkit.createBasicInfoLabel("编辑推荐", new JScrollPane(c_editor_word)));
        
        JPanel panel_url_icon = new JPanel();
        panel_url_icon.setLayout(new BorderLayout());
        panel_url_icon.add(UIToolkit.createBasicInfoLabel("图标链接", c_url_icon), BorderLayout.NORTH);
        panel_url_icon.add(c_url_icon_label, BorderLayout.SOUTH);
        JPanel panel_url_cover = new JPanel();
        panel_url_cover.setLayout(new BorderLayout());
        panel_url_cover.add(UIToolkit.createBasicInfoLabel("封面链接", c_url_cover), BorderLayout.NORTH);
        panel_url_cover.add(c_url_cover_label, BorderLayout.SOUTH);
        JPanel panel_url = new JPanel();
        panel_url.setLayout(new BoxLayout(panel_url, BoxLayout.Y_AXIS));
        panel_url.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "图片管理"));
        panel_url.add(panel_url_icon);
        panel_url.add(panel_url_cover);
        
        c_url_poster.setLayout(new BoxLayout(c_url_poster, BoxLayout.Y_AXIS));
        JPanel panel_poster = new JPanel();
        panel_poster.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "海报"));
        panel_poster.setLayout(new BorderLayout());
        panel_poster.add(c_url_poster_add, BorderLayout.NORTH);
        panel_poster.add(c_url_poster, BorderLayout.CENTER);
        panel_url.add(panel_poster);
        
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
        panel.add(UIToolkit.createBasicInfoLabel("视频脚本", c_vedio));
        panel.add(panel_price);
        panel.add(panel_tag);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        JScrollPane jsp = new JScrollPane(panel);
        jsp.getVerticalScrollBar().setUnitIncrement(20);
        getContentPane().add(jsp, BorderLayout.CENTER);
        
        registerListener();
        
        updateBasic();
        updatePrice();
        updateTag();
    }
    
    private void registerListener() {
        c_platform.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                List<String> platforms = UIToolkit.chooseMultipleValue(new String[] {"PC", "PS3", "PS4", "PSV", "XBOXONE", "XBOX360"}, c_platform.getText().split(" "));
                if (null != platforms) c_platform.setText(platforms.stream().collect(Collectors.joining(" ")));
            }
        });
        c_category.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                List<String> categories = UIToolkit.chooseMultipleValue(new String[] {"角色扮演", "策略", "动作", "冒险", "恐怖", "射击", "模拟", "赛车", "运动", "休闲", "音乐", "格斗", "其他"}, c_category.getText().split(" "));
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
        c_vendor.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String vendor = UIToolkit.chooseSingleValue(new String[] {"BANDAI NAMCO Entertainment", "Bethesda Softworks", "Blizzard Entertainment", "CAPCOM", "Electronic Arts", "Konami Digital", "KOEI TECMO GAMES", "Naughty Dog", "SEGA Games", "SQUARE ENIX", "Take2 International Asia", "Ubisoft Entertainment", "other"}, c_vendor.getText());
                if (null != vendor) c_vendor.setText(vendor);
            }
        });
        c_play_mode.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                List<String> modes = UIToolkit.chooseMultipleValue(new String[] {"单人", "1-2人本地同屏", "1-3人本地同屏", "1-4人本地同屏", "在线"}, c_play_mode.getText().split(" "));
                if (null != modes) c_play_mode.setText(modes.stream().collect(Collectors.joining(" ")));
            }
        });
        c_peripheral.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                List<String> peripherals = UIToolkit.chooseMultipleValue(new String[] {"体感摄像头", "体感棒", "VR头盔", "其它"}, c_peripheral.getText().split(" "));
                if (null != peripherals) c_peripheral.setText(peripherals.stream().collect(Collectors.joining(" ")));
            }
        });
        c_url_icon.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                UIToolkit.doLater(()->{
                    c_url_icon_label.setIcon(new ImageIcon(UIToolkit.LoadImage(c_url_icon.getText())));
                    ManageGame.this.revalidate();
                });
            }
        });
        c_url_cover.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                UIToolkit.doLater(()->{
                    c_url_cover_label.setIcon(new ImageIcon(UIToolkit.LoadImage(c_url_cover.getText())));
                    ManageGame.this.revalidate();
                });
            }
        });
        c_url_poster_add.addActionListener(e->{
            c_url_poster.add(new PosterPanel());
            c_url_poster.revalidate();
        });
        
        i_price_a.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                String price = new_value;
                if (price.endsWith("元/天")) price = price.substring(0, price.length() - 3);
                JSONObject args = new JSONObject();
                args.put("gid", game.i_gid);
                args.put("type", CommonService.RENT_TYPE_A);
                args.put("price", Float.parseFloat(price.trim()));
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_RENT_PRICE, args);
                CommonService.updateGameRentPrice();
                UIToolkit.showServerResponse(rsp);
                updatePrice();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        i_price_b.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                String price = new_value;
                if (price.endsWith("元/天")) price = price.substring(0, price.length() - 3);
                JSONObject args = new JSONObject();
                args.put("gid", game.i_gid);
                args.put("type", CommonService.RENT_TYPE_B);
                args.put("price", Float.parseFloat(price.trim()));
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_RENT_PRICE, args);
                CommonService.updateGameRentPrice();
                UIToolkit.showServerResponse(rsp);
                updatePrice();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            JSONObject args = new JSONObject();
            args.put("gid",         game.i_gid);
            args.put("name_zh_cn",     c_name_zh_cn.getText());
            args.put("name_zh_hk",     c_name_zh_hk.getText());
            args.put("name_en",     c_name_en.getText());
            args.put("name_ja",     c_name_ja.getText());
            args.put("name_ko",     c_name_ko.getText());
            args.put("name_other",     c_name_other.getText());
            args.put("platform",     c_platform.getText());
            args.put("category",     c_category.getText());
            args.put("language",     c_language.getText());
            args.put("size",         c_size.getText());
            args.put("vendor",         c_vendor.getText());
            args.put("sale",         t_sale.getText());
            args.put("url_icon",     c_url_icon.getText());
            args.put("url_cover",     c_url_cover.getText());
            String poster = Arrays.asList(c_url_poster.getComponents())
                    .stream()
                    .filter(c->c instanceof PosterPanel)
                    .map(c->(PosterPanel) c)
                    .map(p->p.getUrl())
                    .collect(Collectors.joining(" "));
            args.put("url_poster",     poster);
            
            args.put("introduction", c_introduction.getText().replace("\n", "|"));
            args.put("version",     c_version.getText().replace("\n", "|"));
            args.put("vedio",         c_vedio.getText().replace("\"", "'"));
            
            args.put("associator",         i_associator.getSelectedIndex());
            args.put("play_mode",     c_play_mode.getText());
            args.put("peripheral",         c_peripheral.getText());
            args.put("editor_word",     c_editor_word.getText().replace("\n", "|"));
            args.put("ign_score",         i_ign_score.getText());
            args.put("producer",         c_producer.getText());
            
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME, args);
            CommonService.updateGame();
            if (!UIToolkit.showServerResponse(rsp)) return;
            
            game = CommonService.getGameByGid(game.i_gid);
            updateBasic();
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
                ListGame.getInstance().refresh();
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
        UIToolkit.doLater(()->{
            c_url_icon_label.setIcon(new ImageIcon(UIToolkit.LoadImage(c_url_icon.getText())));
        });
        c_url_cover.setText(game.c_url_cover);
        UIToolkit.doLater(()->{
            c_url_cover_label.setIcon(new ImageIcon(UIToolkit.LoadImage(c_url_cover.getText())));
        });
        c_url_poster.removeAll();
        for (String poster : game.c_url_poster.split(" ")) {
            if (0 < poster.length()) c_url_poster.add(new PosterPanel(poster));
        }
        
        c_introduction.setText(game.c_introduction.replace("|", "\n"));
        c_version.setText(game.c_version.replace("|", "\n"));
        c_vedio.setText(game.c_vedio);
        
        i_associator.setSelectedIndex(game.i_associator);
        c_play_mode.setText(game.c_play_mode);
        c_peripheral.setText(game.c_peripheral);
        c_editor_word.setText(game.c_editor_word.replace("|", "\n"));
        i_ign_score.setText(String.valueOf(game.i_ign_score));
        c_producer.setText(game.c_producer);
    }
    
    private void updatePrice() {
        i_price_a.setText((null != CommonService.getGameRentPriceByGid(game.i_gid, CommonService.RENT_TYPE_A) ? CommonService.getGameRentPriceByGid(game.i_gid, CommonService.RENT_TYPE_A).i_price : 0.0f) + "元/天");
        i_price_b.setText((null != CommonService.getGameRentPriceByGid(game.i_gid, CommonService.RENT_TYPE_B) ? CommonService.getGameRentPriceByGid(game.i_gid, CommonService.RENT_TYPE_B).i_price : 0.0f) + "元/天");
    }
    
    private void updateTag() {
        panel_tag.removeAll();
        CommonService.getTagByTypeInstance(CommonService.TAG_GAME, game.i_gid)
                .stream()
                .forEach(tag->{
                    JButton btn = new JButton(tag.c_tag);
                    btn.setContentAreaFilled(false);
                    btn.addActionListener(e->{
                        UIToolkit.deleteTag(tag);
                        updateTag();
                    });
                    panel_tag.add(btn);
                });
        panel_tag.revalidate();
        panel_tag.repaint();
    }
    
    private class PosterPanel extends JPanel {
        
        private static final long serialVersionUID = -2572068071678674205L;
        
        private JTextField     url;
        private JButton        del;
        private JLabel        img;
        
        public PosterPanel() {
            this(null);
        }
        
        public PosterPanel(String url) {
            this.url = new JTextField();
            this.url.setPreferredSize(new Dimension(1, this.url.getPreferredSize().height));
            this.del = new JButton("删除");
            this.img = new JLabel();
            
            this.url.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    UIToolkit.doLater(()->{
                        PosterPanel.this.img.setIcon(new ImageIcon(imageFitWidth(UIToolkit.LoadImage(PosterPanel.this.url.getText()), ManageGame.this.getWidth() - 50)));
                        revalidate();
                    });
                }
            });
            
            setLayout(new BorderLayout());
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(this.url, BorderLayout.CENTER);
            panel.add(this.del, BorderLayout.EAST);
            add(panel, BorderLayout.NORTH);
            add(this.img, BorderLayout.SOUTH);
            
            this.del.addActionListener(e->{
                c_url_poster.remove(this);
                c_url_poster.revalidate();
            });
            
            if (null != url) {
                this.url.setText(url);
                UIToolkit.doLater(()->{
                    this.img.setIcon(new ImageIcon(imageFitWidth(UIToolkit.LoadImage(this.url.getText()), ManageGame.this.getWidth() - 50)));
                    revalidate();
                });
            }
        }
        
        public String getUrl() {
            return this.url.getText();
        }
    }
    
    private static Image imageFitWidth(Image img, int width) {
        int height = img.getHeight(null) * width / img.getWidth(null);
        return img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
    }
}
