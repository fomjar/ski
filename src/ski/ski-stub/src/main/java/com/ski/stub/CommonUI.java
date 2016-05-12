package com.ski.stub;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CommonUI {
    
    private static final int    PADDING = 8;
    private static final Font   FONT    = new Font("黑体", Font.PLAIN, 12);
    
    public static Font getCommonFont() {return FONT;}
    
    public static JPanel createPanelLabelField(String label) {return createPanelLabelField(label, null);}
    
    public static JPanel createPanelLabelField(String label, String field) {
        JPanel      jpanel = createPanel();
        JLabel      jlabel = createLabel(label);
        JTextField  jfield = new JTextField(field);
        jfield.setFont(getCommonFont());
        
        jpanel.add(jlabel, BorderLayout.WEST);
        jpanel.add(jfield, BorderLayout.CENTER);
        
        return jpanel;
    }
    
    private static JPanel createPanel() {
        JPanel jpanel = new JPanel();
        jpanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        jpanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, PADDING));
        jpanel.setLayout(new BorderLayout());
        return jpanel;
    }
    
    private static JLabel createLabel(String label) {
        JLabel jlabel = new JLabel(label);
        jlabel.setFont(getCommonFont());
        jlabel.setBorder(BorderFactory.createEmptyBorder(0, PADDING, 0, PADDING));
        jlabel.setPreferredSize(new Dimension(240, 0));
        return jlabel;
    }
    
    public static JPanel createPanelLabelCombo(String label, String[] combo) {
        JPanel              jpanel = createPanel();
        JLabel              jlabel = createLabel(label);
        JComboBox<String>   jcombo = new JComboBox<String>(combo);
        jcombo.setFont(getCommonFont());
        jcombo.setEditable(false);
        
        jpanel.add(jlabel, BorderLayout.WEST);
        jpanel.add(jcombo, BorderLayout.CENTER);
        
        return jpanel;
    }

}
