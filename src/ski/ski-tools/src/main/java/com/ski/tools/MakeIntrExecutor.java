package com.ski.tools;

import java.awt.Color;
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
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import com.ski.common.CommonService;
import com.ski.common.bean.BeanGame;

public class MakeIntrExecutor implements ToolExecutor {
	
	private static final int 	COVER_WIDTH 	= 160;
	private static final int 	COVER_HEIGHT 	= 160;
	
	private static final Font	FONT			= new Font("黑体", Font.PLAIN, 20);
	private static final Color  BACKGROUND		= new Color(2, 0, 20);
	private static final Color  FOREGROUND		= Color.white;
//	private static final Color  BACKGROUND		= Color.white;
//	private static final Color  FOREGROUND		= Color.black;
	private static final int 	TOP				= 150;
	private static final int 	MARGIN			= 30;
	
	private static final int 	HEAD_HEIGHT 	= TOP + MARGIN + COVER_HEIGHT;
	private static final int 	POSTER_HEIGHT	= 400;
	
	private static int width = 0;
	
	public MakeIntrExecutor() {
		CommonService.setWsiHost("ski.craftvoid.com");
	}

	@Override
	public void execute(String[] args) {
		width 	= Integer.parseInt(args[0]);
        String  dir  	= args.length > 1 ? args[1] : ".";
		
		System.out.print(String.format("%-40s", "fetching game data..."));
		CommonService.updateGame();
		System.out.println(" done!");
		
		CommonService.getGameAll().values().parallelStream().forEach(game->makeIntr(game, width, dir));
	}
	
	private static void makeIntr(BeanGame game, int width, String dir) {
		System.out.print(String.format("make introduction for [%-40s]", game.c_name_zh_cn));
		
		String[] posters = game.c_url_poster.split(" ");
		if (1 == posters.length && 0 == posters[0].length()) posters = new String[] {};
		String[] introduction = convertArticle(game.c_introduction, width - MARGIN * 2);
		int height = HEAD_HEIGHT
				+ MARGIN	// separator
				+ (0 == posters.length ? 0 : posters.length * (MARGIN + POSTER_HEIGHT))
				+ (MARGIN + introduction.length * FONT.getSize() * 2)
				+ MARGIN;
		
		// 初始化缓存区
		BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = buffer.getGraphics();
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
		
		// 初始设定
		g.setFont(FONT);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(FOREGROUND);
		
		int[] current = new int[] {0};
		// 画封面
		drawCover(current, g, game.c_url_cover);
		// 画字段
		drawField(current, g, game);
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
		
		try {
			ImageIO.write(buffer, "png", new File(dir + "/output/" + game.c_name_zh_cn + ".png"));
			System.out.println(" done!");
		} catch (IOException e) {
			System.out.println(" fail!");
			e.printStackTrace();
		}
	}
	
	private static void drawCover(int[] current, Graphics g, String url) {
		Image cover = downloadImage(url);
		int bg_width = width;
		int bg_height = width / cover.getWidth(null) * cover.getHeight(null);
		// background
		g.drawImage(cover, 0, 0, bg_width, bg_height, null);
		((Graphics2D) g).setPaint(new GradientPaint(
				new Point(0, 0),
				new Color(BACKGROUND.getRed(), BACKGROUND.getGreen(), BACKGROUND.getBlue(), 100),
				new Point(0, HEAD_HEIGHT + MARGIN * 4),
				new Color(BACKGROUND.getRed(), BACKGROUND.getGreen(), BACKGROUND.getBlue(), 255)));
		g.fillRect(0, 0, bg_width, bg_height);
		// 画水印
		drawWatermark(g, "VC电玩");
		// cover
		g.setColor(FOREGROUND);
		g.drawImage(cover, MARGIN, current[0] + TOP + MARGIN, COVER_WIDTH, COVER_HEIGHT, null);
		// mask
		((Graphics2D) g).setPaint(new GradientPaint(
				new Point(MARGIN, current[0] + TOP + MARGIN),
				new Color(255, 255, 255, 150),
				new Point(MARGIN + COVER_WIDTH / 2, current[0] + TOP + MARGIN + COVER_HEIGHT),
				new Color(255, 255, 255, 0)));
		g.fillRect(MARGIN, current[0] + TOP + MARGIN, COVER_WIDTH, COVER_HEIGHT);
		g.setColor(new Color(255, 255, 255, 100));
		g.drawLine(MARGIN, current[0] + TOP + MARGIN, MARGIN + COVER_WIDTH - 1, current[0] + TOP + MARGIN);
		g.drawLine(MARGIN, current[0] + TOP + MARGIN, MARGIN, current[0] + TOP + MARGIN + COVER_HEIGHT - 1);
		g.setColor(new Color(0, 0, 0, 100));
		g.drawLine(MARGIN, current[0] + TOP + MARGIN + COVER_HEIGHT - 1, MARGIN + COVER_WIDTH - 1, current[0] + TOP + MARGIN + COVER_HEIGHT - 1);
		g.drawLine(MARGIN + COVER_WIDTH - 1, current[0] + TOP + MARGIN, MARGIN + COVER_WIDTH - 1, current[0] + TOP + MARGIN + COVER_HEIGHT - 1);
		
		g.setColor(FOREGROUND);
	}
	
	private static void drawField(int[] current, Graphics g, BeanGame game) {
		int base_x 		= COVER_WIDTH + MARGIN * 2 + 10;
		int count  		= 5;
		int label_len 	= g.getFont().getSize() * 6;
		int field_len   = width - base_x - label_len - MARGIN;
		int interval_y 	= COVER_HEIGHT / count;
		int offset_y    = (interval_y - g.getFont().getSize()) / 2;
		
		current[0] += TOP + MARGIN + g.getFont().getSize() + offset_y;
		String label = "中文名称：";
		drawShadowString(g, label, 				base_x, 			current[0], field_len);
		drawShadowString(g, game.c_name_zh_cn, 	base_x + label_len, current[0], field_len);
		current[0] += interval_y;
		label = "英文名称：";
		drawShadowString(g, label, 				base_x, 			current[0], field_len);
		drawShadowString(g, game.c_name_en, 		base_x + label_len, current[0], field_len);
		current[0] += interval_y;
		label = "游戏大小：";
		drawShadowString(g, label, 				base_x, 			current[0], field_len);
		drawShadowString(g, game.c_size,	 		base_x + label_len, current[0], field_len);
		current[0] += interval_y;
		label = "发售时间：";
		drawShadowString(g, label, 				base_x, 			current[0], field_len);
		drawShadowString(g, game.t_sale, 			base_x + label_len, current[0], field_len);
		current[0] += interval_y;
		label = "发行厂商：";
		drawShadowString(g, label, 				base_x, 			current[0], field_len);
		drawShadowString(g, game.c_vendor, 		base_x + label_len, current[0], field_len);
		
		current[0] = TOP + MARGIN + COVER_HEIGHT;
	}
	
	private static void drawSeparator(int[] current, Graphics g) {
		current[0] += MARGIN;
		g.setColor(BACKGROUND.darker());
		g.drawLine(MARGIN + 1, current[0] + 1, width - MARGIN + 1, current[0] + 1);
		g.setColor(FOREGROUND);
		g.drawLine(MARGIN, current[0], width - MARGIN, current[0]);
	}
	
	private static void drawShadowString(Graphics g, String s, int x, int y, int width) {
		g.setColor(BACKGROUND.darker());
		drawString(g, s, x + 2, y + 2, width);
		g.setColor(FOREGROUND);
		drawString(g, s, x, y, width);
	}
	
	private static void drawPoster(int[] current, Graphics g, String poster) {
		Image img = downloadImage(poster);
		current[0] += MARGIN;
		g.drawImage(img, MARGIN, current[0], width - MARGIN * 2, POSTER_HEIGHT, null);
		current[0] += POSTER_HEIGHT;
	}
	
	private static void drawIntroduction(int[] current, Graphics g, String[] introduction) {
		current[0] += MARGIN;
		current[0] += g.getFont().getSize();
		for (String s : introduction) {
			drawShadowString(g, s, MARGIN, current[0], width - MARGIN * 2);
			current[0] += g.getFont().getSize() * 2;
		}
		current[0] -= g.getFont().getSize();
	}
	
	private static void drawWatermark(Graphics g, String watermark) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1000; i++) sb.append(watermark + "  ");
		String[] wms = convertArticle(sb.toString(), width);
		g.setFont(g.getFont().deriveFont(45.0f));
		g.setColor(new Color(255, 255, 255, 8));
//		int current = TOP + MARGIN + COVER_HEIGHT;
		int current = 0;
		for (String wm : wms) {
			g.drawString(wm, 0, current);
			current += g.getFont().getSize() * 5 / 4;
			if (current > 5000) break;
		}
		g.setFont(FONT);
	}
	
	private static String[] convertArticle(String article, int width) {
		Graphics g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();
		g.setFont(FONT);
		
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
	
	private static Image downloadImage(String url) {
		try {return ImageIO.read(new URL(url));}
		catch (IOException e) {e.printStackTrace();}
		return null;
	}

}
