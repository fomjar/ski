package com.fomjar.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JTextField;

public class FjTextField extends JTextField {
    
    private static final long serialVersionUID = -1334812155892751398L;
    
    private String tipText;
    
    public FjTextField() {
        super();
    }
    
    public FjTextField(String text) {
        super(text);
    }

    public void setDefaultTips(String text) {
        this.tipText = text;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (null != tipText && 0 == getText().length()) {
            g.setColor(Color.gray);
            g.setFont(g.getFont().deriveFont(Font.ITALIC));
            int h = (int) g.getFontMetrics().getStringBounds(tipText, g).getHeight();
            g.drawString(tipText, 4, h);
        }
    }

}
