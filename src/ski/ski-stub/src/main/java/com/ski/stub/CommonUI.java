package com.ski.stub;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

public class CommonUI {
    
    private static final int    PADDING = 8;
    private static final Font   FONT    = new Font("黑体", Font.PLAIN, 14);
    private static final int    HEIGHT  = 24;
    
    public static Font getCommonFont() {return FONT;}
    
    public static JPanel createPanelLabelField(String label) {return createPanelLabelField(label, null);}
    
    public static JPanel createPanelLabelField(String label, String field) {
        JPanel      jpanel = createPanel();
        JLabel      jlabel = createLabel(label);
        JTextField  jfield = new JTextField(field);
        jfield.setFont(getCommonFont());
        jfield.setPreferredSize(new Dimension(0, HEIGHT));
        jfield.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {jfield.selectAll();}
        });
        
        jpanel.add(jlabel, BorderLayout.WEST);
        jpanel.add(jfield, BorderLayout.CENTER);
        
        return jpanel;
    }
    
    private static JPanel createPanel() {
        JPanel jpanel = new JPanel();
        jpanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
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
        jcombo.setPreferredSize(new Dimension(0, HEIGHT));
        jcombo.setEditable(false);
        
        jpanel.add(jlabel, BorderLayout.WEST);
        jpanel.add(jcombo, BorderLayout.CENTER);
        
        return jpanel;
    }
    
    public static JTextField createPanelTitleField(String title, boolean editable) {
        JTextField jtf = new JTextField();
        jtf.setFont(getCommonFont());
        jtf.setBorder(BorderFactory.createTitledBorder(jtf.getBorder(), title));
        ((TitledBorder) jtf.getBorder()).setTitleFont(getCommonFont());
        jtf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        jtf.setEditable(editable);
        return jtf;
    }
    
    public static JScrollPane createPanelTitleArea(String title, boolean editable) {
        JTextArea jta = new JTextArea();
        jta.setFont(getCommonFont());
        jta.setLineWrap(true);
        jta.setEditable(editable);
        
        JScrollPane jsp = new JScrollPane(jta);
        jsp.setBorder(BorderFactory.createTitledBorder(jsp.getBorder(), title));
        ((TitledBorder) jsp.getBorder()).setTitleFont(getCommonFont());
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        return jsp;
    }
    
    public static JPanel createPanelRadioButton(String title, String[] radio) {
        JPanel panel = new JPanel();
        panel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        ((TitledBorder) panel.getBorder()).setTitleFont(getCommonFont());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        ButtonGroup group = new ButtonGroup();
        for (String s : radio) {
            JRadioButton b = new JRadioButton(s);
            b.setFont(getCommonFont());
            panel.add(b);
            
            group.add(b);
        }
        return panel;
    }

}
