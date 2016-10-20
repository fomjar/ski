package com.ski.vcg.omc.comp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fomjar.widget.FjListCell;

public class DetailPane extends JComponent {
    
    private static final long serialVersionUID = 5954712374553011298L;
    
    private JLabel title;
    private JPanel toolbar;
    private JLabel indicator;
    
    public DetailPane(String title, JComponent content, JButton... buttons) {
        this.title = new JLabel(title);
        this.title.setFont(this.title.getFont().deriveFont(Font.BOLD));
        this.title.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        
        toolbar = new JPanel();
        toolbar.setOpaque(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        addToolBarButton(buttons);
        
        indicator = new JLabel("-");
        indicator.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        
        FjListCell<Object> panel_title = new FjListCell<Object>();
        panel_title.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 12));
        panel_title.setLayout(new BorderLayout());
        panel_title.add(indicator, BorderLayout.WEST);
        panel_title.add(this.title, BorderLayout.CENTER);
        panel_title.add(toolbar, BorderLayout.EAST);
        
        panel_title.addActionListener(e->{
            if ("+".equals(indicator.getText())) {
                indicator.setText("-");
                content.setVisible(true);
            } else if ("-".equals(indicator.getText())) {
                indicator.setText("+");
                content.setVisible(false);
            }
        });
        
        setLayout(new BorderLayout());
        add(panel_title, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }
    
    public JLabel  getTitle() {return title;}
    public void    setTitle(String title) {this.title.setText(title);}
    
    public JPanel  getToolBar() {return toolbar;}
    
    public JButton getToolBarButton(int i) {return (JButton) toolbar.getComponent(i * 2);}
    
    public JLabel  getIndicator() {return indicator;}
    
    public void addToolBarButton(JButton... buttons) {
        if (null == buttons || 0 == buttons.length) return;
        
        for (JButton button : buttons) {
            if (0 < toolbar.getComponentCount()) toolbar.add(Box.createHorizontalStrut(4));
            toolbar.add(button);
        }
    }
    
    public static JButton createToolBarButton(String name, ActionListener action) {
        JButton button = new JButton(name);
        button.addActionListener(action);
        return button;
    }

}
