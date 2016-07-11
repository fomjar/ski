package com.fomjar.widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

public class FjListCell<E> extends JComponent {
    
    private static final long serialVersionUID = -5413153652935337627L;
    
    private   static final Color color_default      = new Color(230, 230, 230);
    private   static final Color color_over         = new Color(240, 240, 255);
    private   static final Color color_press        = new Color(200, 200, 230);
    private   static final Color color_bright   = Color.white;
    private   static final Color color_shadow   = Color.lightGray;
    protected static final Color color_major    = Color.darkGray;
    protected static final Color color_minor    = Color.gray;
    
    private Color   c_default;
    private Color   c_over;
    private Color   c_press;
    private boolean is_over;
    private boolean is_press;
    private E       data;
    private List<ActionListener> listeners;
    
    public FjListCell() {
        this(null);
    }
    
    public FjListCell(E data) {
        c_default   = color_default;
        c_over      = color_over;
        c_press     = color_press;
        
        listeners   = new LinkedList<ActionListener>();
        is_over     = false;
        is_press    = false;
        setData(data);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4,12,4,12));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                is_press = true;
                repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                is_press = false;
                repaint();
                if (is_over) listeners.forEach(listener->listener.actionPerformed(new ActionEvent(e, e.getID(), null, e.getModifiers())));
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                is_over = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                is_over = false;
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (is_press)       g.setColor(c_press);
        else if (is_over)   g.setColor(c_over);
        else                g.setColor(c_default);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        if (!is_press)  g.setColor(color_bright);
        else            g.setColor(color_shadow);
        g.drawLine(0, 0, getWidth(), 0);
        if (!is_press)  g.setColor(color_shadow);
        else            g.setColor(color_bright);
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        
        super.paintComponent(g);
    }
    
    public void setData(E data) {this.data = data;}

    public E getData() {return data;}
    
    public void addActionListener(ActionListener listener) {listeners.add(listener);}
    
    public void passthroughMouseEvent(Component top) {
        top.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e)     {FjListCell.this.dispatchEvent(e);}
            @Override
            public void mousePressed(MouseEvent e)      {FjListCell.this.dispatchEvent(e);}
            @Override
            public void mouseExited(MouseEvent e)       {FjListCell.this.dispatchEvent(e);}
            @Override
            public void mouseEntered(MouseEvent e)      {FjListCell.this.dispatchEvent(e);}
            @Override
            public void mouseClicked(MouseEvent e)      {FjListCell.this.dispatchEvent(e);}
        });
        if (top instanceof Container) {
            for (Component c : ((Container) top).getComponents()) passthroughMouseEvent(c);
        }
    }
    
    @Override
    public void setForeground(Color color) {
        super.setForeground(color);
        
        for (Component c : getComponents()) c.setForeground(color);
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
        for (Component c : getComponents()) c.setFont(font);
    }
    
    public void setColorDefault(Color color)    {c_default = color;}
    public void setColorOver(Color color)       {c_over = color;}
    public void setColorPress(Color color)      {c_press = color;}

}