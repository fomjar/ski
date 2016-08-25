package com.ski.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.ski.common.CommonService;
import com.ski.common.bean.BeanGame;

public class ExecutorMakeIntroduction implements ToolExecutor {
    
    private static int      g_width     = 750;
    private static int      g_top       = 150;
    private static int      g_margin    = 30;
    private static int      g_cover     = 180;
    private static int      g_poster    = 400;
    private static Color    g_bg        = new Color(2, 0, 20);
    private static Color    g_fg        = Color.white;
    private static Font     g_font      = new Font("黑体", Font.PLAIN, 20);
    private static String   g_base      = ".";
    private static String   g_format    = "jpg";
    private static String   g_icon      = "https://img.alicdn.com/imgextra/i3/2859081856/TB24W0saM_xQeBjy0FoXXX1vpXa_!!2859081856.png";
    private static int      g_iconsize  = 70;
    
    @Override
    public void execute(Map<String, String> args) {
        args.forEach((k, v)->{
            switch (k) {
            case "width":       g_width     = Integer.parseInt(v);                      break;
            case "top":         g_top       = Integer.parseInt(v);                      break;
            case "margin":      g_margin    = Integer.parseInt(v);                      break;
            case "cover":       g_cover     = Integer.parseInt(v);                      break;
            case "poster":      g_poster    = Integer.parseInt(v);                      break;
            case "bg":          g_bg        = new Color(Integer.parseInt(v));           break;
            case "fg":          g_fg        = new Color(Integer.parseInt(v));           break;
            case "font":        g_font      = g_font.deriveFont(Float.parseFloat(v));   break;
            case "base":        g_base      = v;                                        break;
            case "format":      g_format    = v;                                        break;
            case "icon":        g_icon      = v;                                        break;
            case "iconsize":    g_iconsize  = Integer.parseInt(v);                      break;
            default:
                System.out.println(String.format("unknown argument: %s:%s", k, v));
                break;
            }
        });
        
        System.out.print(String.format("%-40s", "fetching game data..."));
        CommonService.updateGame();
        System.out.println(" done!");
        
        CommonService.getGameAll().values().parallelStream().forEach(game->{
            try {
                makeIntr(game, g_width, g_base);
                System.out.print(String.format("make introduction for [%-40s]", game.c_name_zh_cn));
                System.out.println(" done!");
            } catch (Exception e) {
                System.out.print(String.format("make introduction for [%-40s]", game.c_name_zh_cn));
                System.out.println(" fail!");
                e.printStackTrace();
            }
        });
    }
    
    private static void makeIntr(BeanGame game, int width, String dir) throws IOException {
        String[] posters = game.c_url_poster.split(" ");
        if (1 == posters.length && 0 == posters[0].length()) posters = new String[] {};
        String[] introduction = convertArticle(game.c_introduction, width - g_margin * 2);
        int height = g_top                                                                // top
                + g_margin + g_cover                                                    // cover
                + g_margin                                                                // separator
                + (0 == posters.length ? 0 : posters.length * (g_margin + g_poster))    // poster
                + (g_margin + introduction.length * g_font.getSize() * 2)                // introduction
                + g_margin;                                                                // bottom
        
        // 初始化缓存区
        BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = buffer.getGraphics();
        g.setColor(g_bg);
        g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        
        // 初始设定
        g.setFont(g_font);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(g_fg);
        
        int[] current = new int[] {0};
        current[0] = g_top;
        // 画封面
        drawCover(current, g, game.c_url_cover);
        // 画字段
        drawField(current, g, game);
        current[0] = g_top + g_margin + g_cover;
        // 画分割线
        drawSeparator(current, g);
        // 画第一张海报
        if (0 < posters.length) drawPoster(current, g, posters[0]);
        // 画简介
        drawIntroduction(current, g, introduction);
        // 画其它海报
        if (1 < posters.length) {
            for (int i = 1; i < posters.length; i++) drawPoster(current, g, posters[i]);
        }
        drawIcon(g);
        
        ImageIO.write(buffer, g_format, new File(String.format("%s/output/%s.%s", dir, game.c_name_zh_cn, g_format)));
    }
    
    private static void drawCover(int[] current, Graphics g, String url) {
        Image cover = getImage(url);
        // background
        drawBackground(g, cover);
        // watermark
        drawWatermark(g, "VC电玩");
        
        // shadow
        int offset = 6;
        g.setColor(new Color(0, 0, 0, 80));
        g.fillRect(g_margin + offset, current[0] + g_margin + offset, g_cover, g_cover);
        // cover
        g.setColor(g_fg);
        g.drawImage(cover, g_margin, current[0] + g_margin, g_cover, g_cover, null);
        // mask
        ((Graphics2D) g).setPaint(new GradientPaint(
                new Point(g_margin, current[0] + g_margin),
                new Color(255, 255, 255, 150),
                new Point(g_margin + g_cover / 2, current[0] + g_margin + g_cover),
                new Color(255, 255, 255, 0)));
        g.fillRect(g_margin, current[0] + g_margin, g_cover, g_cover);
        g.setColor(new Color(255, 255, 255, 50));
        g.drawLine(g_margin, current[0] + g_margin, g_margin + g_cover - 1, current[0] + g_margin);
        g.drawLine(g_margin, current[0] + g_margin, g_margin, current[0] + g_margin + g_cover - 1);
        g.setColor(new Color(0, 0, 0, 100));
        g.drawLine(g_margin, current[0] + g_margin + g_cover - 1, g_margin + g_cover - 1, current[0] + g_margin + g_cover - 1);
        g.drawLine(g_margin + g_cover - 1, current[0] + g_margin, g_margin + g_cover - 1, current[0] + g_margin + g_cover - 1);
        
        g.setColor(g_fg);
    }
    
    private static void drawBackground(Graphics g, Image cover) {
        int bg_width = g_width;
        int bg_height = g_width / cover.getWidth(null) * cover.getHeight(null);
        // background
        g.drawImage(cover, 0, 0, bg_width, bg_height, null);
        ((Graphics2D) g).setPaint(new GradientPaint(
                new Point(0, 0),
                new Color(g_bg.getRed(), g_bg.getGreen(), g_bg.getBlue(), 100),
                new Point(0, g_top + g_margin + g_cover + g_margin * 4),
                new Color(g_bg.getRed(), g_bg.getGreen(), g_bg.getBlue(), 255)));
        g.fillRect(0, 0, bg_width, bg_height);
    }
    
    private static void drawField(int[] current, Graphics g, BeanGame game) {
        Map<String, String> fields = new LinkedHashMap<String, String>();
        fields.put("中文名称：", game.c_name_zh_cn);
        fields.put("英文名称：", game.c_name_en);
        fields.put("支持语言：", game.c_language);
        fields.put("游戏大小：", game.c_size);
        fields.put("发售时间：", game.t_sale);
        fields.put("发行厂商：", game.c_vendor);
        
        int base_x         = g_cover + g_margin * 2;
        int count          = fields.size();
        int label_len     = g.getFont().getSize() * 6;
        int field_len   = g_width - base_x - label_len - g_margin;
        int interval_y     = g_cover / count;
        int offset_y    = (interval_y - g.getFont().getSize()) / 3;
        
        current[0] += g_margin + g.getFont().getSize() + offset_y;
        fields.forEach((label, field)->{
            drawShadowString(g, label,    base_x,             current[0], label_len);
            drawShadowString(g, field,     base_x + label_len, current[0], field_len);
            current[0] += interval_y;
        });
    }
    
    private static void drawSeparator(int[] current, Graphics g) {
        current[0] += g_margin;
        g.setColor(g_bg.darker());
        g.drawLine(g_margin + 1, current[0] + 1, g_width - g_margin + 1, current[0] + 1);
        g.setColor(g_fg);
        g.drawLine(g_margin, current[0], g_width - g_margin, current[0]);
    }
    
    private static void drawShadowString(Graphics g, String s, int x, int y, int width) {
        g.setColor(g_bg.darker());
        drawString(g, s, x + 2, y + 2, width);
        g.setColor(g_fg);
        drawString(g, s, x, y, width);
    }
    
    private static void drawPoster(int[] current, Graphics g, String poster) {
        Image img = getImage(poster);
        current[0] += g_margin;
        // image
        g.drawImage(img, g_margin, current[0], g_width - g_margin * 2, g_poster, null);
        // mask
        g.setColor(new Color(255, 255, 255, 50));
        g.drawLine(g_margin, current[0], g_width - g_margin - 1, current[0]);
        g.drawLine(g_margin, current[0], g_margin, current[0] + g_poster - 1);
        g.setColor(new Color(0, 0, 0, 100));
        g.drawLine(g_margin, current[0] + g_poster - 1, g_width - g_margin - 1, current[0] + g_poster - 1);
        g.drawLine(g_width - g_margin - 1, current[0], g_width - g_margin - 1, current[0] + g_poster - 1);
        g.setColor(g_fg);
        
        current[0] += g_poster;
    }
    
    private static void drawIntroduction(int[] current, Graphics g, String[] introduction) {
        current[0] += g_margin;
        current[0] += g.getFont().getSize();
        for (String s : introduction) {
            drawShadowString(g, s, g_margin, current[0], g_width - g_margin * 2);
            current[0] += g.getFont().getSize() * 2;
        }
        current[0] -= g.getFont().getSize();
    }
    
    private static void drawWatermark(Graphics g, String watermark) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append(watermark + "  ");
        String[] wms = convertArticle(sb.toString(), g_width);
        g.setFont(g.getFont().deriveFont(45.0f));
        g.setColor(new Color(255, 255, 255, 12));
        int current = 0;
        for (String wm : wms) {
            g.drawString(wm, 0, current);
            current += g.getFont().getSize() * 5 / 4;
            if (current > 5000) break;
        }
        g.setFont(g_font);
    }
    
    private static void drawIcon(Graphics g) {
        Image icon = getIcon();
        Composite c = ((Graphics2D) g).getComposite();
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        // shadow
        g.drawImage(icon, g_width - g_margin - g_iconsize, g_margin, g_iconsize, g_iconsize, null);
        // icon
        g.drawImage(icon, g_width - g_margin - g_iconsize, g_margin, g_iconsize, g_iconsize, null);
        ((Graphics2D) g).setComposite(c);
    }
    
    private static String[] convertArticle(String article, int width) {
        Graphics g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();
        g.setFont(g_font);
        
        int from = 0;
        String indent = "        ";
        article = article.replace("|", "\n" + indent);
        article = indent + article;
        List<String> result = new LinkedList<String>();
        for (int i = 0; i < article.length(); i++) {
            String s = article.substring(from, i);
            if (article.charAt(i) == '\n') {
                result.add(article.substring(from, i));
                from = i + 1;
            } else if (g.getFontMetrics().stringWidth(s) >= width) {
                result.add(article.substring(from, i - 1));
                from = i - 1;
            }
        }
        result.add(article.substring(from));
        return result.toArray(new String[result.size()]);
    }
    
    private static void drawString(Graphics g, String s, int x, int y, int width) {
        if (g.getFontMetrics().stringWidth(s) > width) {
            width -= g.getFontMetrics().stringWidth("...");
            while (g.getFontMetrics().stringWidth(s) > width) s = s.substring(0, s.length() - 1);
            s += "...";
        }
        g.drawString(s, x, y);
    }
    
    private static BufferedImage getIcon() {
        return getImage(g_icon);
    }
    
    private static BufferedImage cache_icon_shadow = null;
    private static Image getIconShadow() {
        if (null == cache_icon_shadow) {
            BufferedImage icon = getIcon();
            BufferedImage shadow = new BufferedImage(icon.getWidth(), icon.getHeight(), BufferedImage.TYPE_INT_ARGB);
            shadow.getGraphics().drawImage(icon, 0, 0, null);
            for (int x = 0; x < shadow.getWidth(); x++) {
                for (int y = 0; y < shadow.getHeight(); y++) {
                    int argb = shadow.getRGB(x, y);
                    int a = (argb & 0xFF000000) >> 24;
                    if (0 != a) shadow.setRGB(x, y, Color.black.getRGB());
                }
            }
            cache_icon_shadow = shadow;
        }
        return cache_icon_shadow;
    }
    
    private static final Map<String, BufferedImage> cache = new HashMap<String, BufferedImage>();
    private static BufferedImage getImage(String url) {
        try {
            if (!cache.containsKey(url)) cache.put(url, ImageIO.read(new URL(url)));
            return cache.get(url);
        } catch (IOException e) {e.printStackTrace();}
        return null;
    }

}
