package com.fomjar.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class FjEditLabel extends JComponent {

    private static final long serialVersionUID = -8419870071367402091L;
    
    private boolean     isEditable;
    private JLabel      label;
    private JTextField  field;
    private List<EditListener> listeners;
    
    public FjEditLabel() {
        this(null, true);
    }
    
    public FjEditLabel(boolean isEditable) {
        this(null, isEditable);
    }
    
    public FjEditLabel(String text) {
        this(text, true);
    }
    
    public FjEditLabel(String text, boolean isEditable) {
        this.isEditable = isEditable;
        listeners = new LinkedList<EditListener>();
        label = new JLabel();
        field = new JTextField();
        
        label.setPreferredSize(new Dimension(10, 0));
        
        setText(text);
        setFont(label.getFont());
        setOpaque(false);
        setBackground(null);
        
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        // double click
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isEditable()) return;
                
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (2 == e.getClickCount()) {
                        removeAll();
                        add(field, BorderLayout.CENTER);
                        field.setPreferredSize(label.getPreferredSize());
                        revalidate();
                        repaint();
                        
                        field.setText(label.getText());
                        field.requestFocus();
                        field.selectAll();
                        
                        listeners.forEach(listener->listener.startEdit(label.getText()));
                    }
                }
            }
        });
        // enter
        field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String old_value = label.getText();
                String new_value = field.getText();
                label.setText(field.getText());
                removeAll();
                add(label, BorderLayout.CENTER);
                revalidate();
                repaint();
                
                listeners.forEach(listener->listener.finishEdit(old_value, new_value));
            }
        });
        // escape
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
                    field.setText(label.getText());
                    removeAll();
                    add(label, BorderLayout.CENTER);
                    revalidate();
                    repaint();
                    
                    listeners.forEach(listener->listener.cancelEdit(label.getText()));
                }
            }
        });
    }
    
    public void setText(String text) {label.setText(text);}
    
    public String getText() {return label.getText();}
    
    public boolean isEditable() {return isEditable;}
    
    public void setEditable(boolean isEditable) {this.isEditable = isEditable;}
    
    public void addEditListener(EditListener listener) {listeners.add(listener);}
    
    @Override
    public void setOpaque(boolean isOpaque) {
        super.setOpaque(isOpaque);
        label.setOpaque(isOpaque);
        field.setOpaque(isOpaque);
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        label.setFont(font);
        field.setFont(font);
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        label.setForeground(fg);
        field.setForeground(fg);
    }
    
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        label.setBackground(bg);
        field.setBackground(bg);
    }

    public static interface EditListener {
        void startEdit(String value);
        void finishEdit(String old_value, String new_value);
        void cancelEdit(String value);
    }
}