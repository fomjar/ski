package com.fomjar.widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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

public abstract class FjListCell<E> extends JComponent {
    
    private static final long serialVersionUID = -5413153652935337627L;
    
    private   static final Color color_default      = new Color(230, 230, 230);
    private   static final Color color_over         = new Color(240, 240, 255);
    private   static final Color color_press        = new Color(200, 200, 230);
    private   static final Color color_actionmark   = new Color(255, 100, 100);
    private   static final Color color_bright   = Color.white;
    private   static final Color color_shadow   = Color.lightGray;
    protected static final Color color_major    = Color.darkGray;
    protected static final Color color_minor    = Color.gray;
    
    private boolean is_over;
    private boolean is_press;
    private boolean actionmark;
    private E       data;
    private List<ActionListener> listeners;
    
    public FjListCell(E data) {
        listeners   = new LinkedList<ActionListener>();
        is_over     = false;
        is_press    = false;
        actionmark  = true;
        setData(data);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
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
                if (is_over) listeners.forEach(listener->listener.actionPerformed(new ActionEvent(e.getSource(), e.getID(), null, e.getModifiers())));
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
        if (is_press)       g.setColor(color_press);
        else if (is_over)   g.setColor(color_over);
        else                g.setColor(color_default);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        if (!is_press)  g.setColor(color_bright);
        else            g.setColor(color_shadow);
        g.drawLine(0, 0, getWidth(), 0);
        if (!is_press)  g.setColor(color_shadow);
        else            g.setColor(color_bright);
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        
        super.paintComponent(g);
        
        if (actionmark && !listeners.isEmpty()) {
            g.setColor(color_actionmark);
            g.fillRect(0, 1, 3, getHeight() - 2);
        }
    }
    
    public void setData(E data) {this.data = data;}

    public E getData() {return data;}
    
    public void addActionListener(ActionListener listener) {listeners.add(listener);}
    
    public void setActionMark(boolean actionmark) {this.actionmark = actionmark;}

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

}