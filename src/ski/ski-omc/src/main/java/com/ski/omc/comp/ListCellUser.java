package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjListCell;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;

public class ListCellUser extends FjListCell<BeanChannelAccount> {
    
    private static final long serialVersionUID = -1579462449977954180L;
    
    private static final int COVER_SIZE = 40;
    
    private static final Map<String, Image> cache_cover = new ConcurrentHashMap<String, Image>();
    
    private JLabel cove;
    private JLabel plat;
    private JLabel name;
    private JLabel phon;
    private JLabel rent;

    public ListCellUser(BeanChannelAccount data) {
        super(data);
        
        cove = new JLabel();
        plat = new JLabel("[" + getPlatform(data.i_channel) + "] ");
        name = new JLabel(data.getDisplayName());
        phon = new JLabel("电话: " + data.c_phone);
        rent = new JLabel(getMinorString(data));
        
        Image image = null;
        if (0 < data.c_url_cover.length()) {
            try {image = ImageIO.read(new URL(data.c_url_cover)).getScaledInstance(COVER_SIZE, COVER_SIZE, BufferedImage.SCALE_SMOOTH);}
            catch (IOException e) {e.printStackTrace();}
        } else {
            String s = data.getDisplayName();
            while (3 > s.length()) s += "-";
            
            if (cache_cover.containsKey(s)) image = cache_cover.get(s);
            else {
                BufferedImage buf = new BufferedImage(COVER_SIZE, COVER_SIZE, BufferedImage.TYPE_INT_RGB);
                Graphics g = buf.getGraphics();
                Color bg = new Color(s.charAt(0) % 255, s.charAt(1) % 255, s.charAt(2) % 255);
                bg.darker();
                g.setColor(bg);
                g.fillRect(0, 0, COVER_SIZE, COVER_SIZE);
                
                Color fg = Color.white;
                g.setColor(fg);
                g.setFont(g.getFont().deriveFont(Font.BOLD).deriveFont(COVER_SIZE * 0.6f));
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawString(s.substring(0, 1).toUpperCase(), COVER_SIZE * 2 / 10, COVER_SIZE * 7 / 10);
                
                g.dispose();
                image = buf;
                cache_cover.put(s, image);
            }
        }
        cove.setIcon(new ImageIcon(image));
        cove.setPreferredSize(new Dimension(COVER_SIZE, COVER_SIZE));
        cove.setBorder(BorderFactory.createLineBorder(Color.black));
        name.setPreferredSize(new Dimension(1, 0));
        plat.setFont(plat.getFont().deriveFont(Font.ITALIC));
        
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BorderLayout());
        top.add(plat, BorderLayout.WEST);
        top.add(name, BorderLayout.CENTER);
        JPanel center = new JPanel();
        center.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        center.setOpaque(false);
        center.setLayout(new BorderLayout());
        center.add(top, BorderLayout.NORTH);
        center.add(phon, BorderLayout.SOUTH);
        setLayout(new BorderLayout());
        add(cove, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        add(rent, BorderLayout.EAST);

        addActionListener(e->MainFrame.getInstance().setDetailUser(data.i_caid));
        
        if (rent.getText().startsWith("00 /")) {
            setForeground(Color.lightGray);
            plat.setForeground(Color.lightGray);
            name.setForeground(Color.lightGray);
            phon.setForeground(Color.lightGray);
        } else {
            setForeground(color_major);
            plat.setForeground(color_major);
            name.setForeground(color_major);
            phon.setForeground(color_major);
        }
    }

    private static String getMinorString(BeanChannelAccount data) {
        int renting = CommonService.getGameAccountByCaid(data.i_caid, CommonService.RENT_TYPE_A).size()
                + CommonService.getGameAccountByCaid(data.i_caid, CommonService.RENT_TYPE_B).size();
        int all = 0;
        try {all = CommonService.getOrderByCaid(data.i_caid).stream().map(order->order.commodities.size()).reduce(0, (c1, c2)->c1 + c2).intValue();}
        catch (NoSuchElementException e) {}
        return String.format("%02d / %02d", renting, all);
    }
    
    private static String getPlatform(int channel) {
        switch (channel) {
        case CommonService.CHANNEL_TAOBAO: return "淘  宝";
        case CommonService.CHANNEL_WECHAT: return "微  信";
        case CommonService.CHANNEL_ALIPAY: return "支付宝";
        default: return "未  知";
        }
    }

}
