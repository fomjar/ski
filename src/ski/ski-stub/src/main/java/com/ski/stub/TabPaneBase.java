package com.ski.stub;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class TabPaneBase extends JPanel {

    private static final long serialVersionUID = 5527656651436272715L;
    
    private JPanel  fieldpane;
    private JPanel  submitcontainer;
    private JButton submit;
    private JPanel  statuscontainer;
    private JLabel  status;
    
    public TabPaneBase() {
        fieldpane  = new JPanel() {
            private static final long serialVersionUID = -7571751943702009286L;
            @Override
            public Component add(Component comp) {
                if (4 <= getComponentCount()) { 
                    for (int i = 0; i < 4; i++) {
                        remove(getComponentCount() - 1);
                    }
                }
                
                Component c = super.add(comp);
                super.add(Box.createVerticalGlue());
                super.add(submitcontainer);
                super.add(Box.createVerticalStrut(4));
                super.add(statuscontainer);
                return c;
            }
        };
        submit = new JButton("提交");
        submit.setFont(CommonUI.getCommonFont());
        //submit.setContentAreaFilled(false);
        submit.addActionListener(action->{
            try {submit();}
            catch (Exception e) {
                e.printStackTrace();
                setStatus(e.getMessage());
            }
        });
        submitcontainer = new JPanel();
        submitcontainer.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        submitcontainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        submitcontainer.setPreferredSize(new Dimension(0, 32));
        submitcontainer.setLayout(new BorderLayout());
        submitcontainer.add(submit, BorderLayout.CENTER);
        
        status = new JLabel();
        status.setFont(CommonUI.getCommonFont());
        status.setHorizontalAlignment(SwingConstants.LEFT);
        status.setOpaque(true);
        status.setForeground(Color.gray);
        status.setBackground(Color.lightGray);
        status.getInsets().top      = 0;
        status.getInsets().left     = 0;
        status.getInsets().bottom   = 0;
        status.getInsets().right    = 0;
        statuscontainer = new JPanel();
        statuscontainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        statuscontainer.setBorder(BorderFactory.createLineBorder(Color.gray));
        statuscontainer.setLayout(new BorderLayout());
        statuscontainer.add(status, BorderLayout.CENTER);
        
        setLayout(new BorderLayout());
        add(fieldpane,  BorderLayout.CENTER);
        
        fieldpane.setLayout(new BoxLayout(fieldpane, BoxLayout.Y_AXIS));
        
        setStatus("没有任何操作");
    }
    
    public void addField(JComponent field) {fieldpane.add(field);}
    
    protected void update() {};
    
    protected void submit() {};
    
    public void enableSubmit() {submit.setEnabled(true);}
    
    public void disableSubmit() {submit.setEnabled(false);}
    
    public void setStatus(String text) {status.setText("当前：" + text);}
    
    public JComponent getField(int index) {return (JComponent) ((JPanel)fieldpane.getComponent(index)).getComponent(1);}
    
    public JTextField getFieldToField(int index) {return (JTextField) getField(index);}
    
    @SuppressWarnings("unchecked")
    public JComboBox<String> getFieldToCombo(int index) {return (JComboBox<String>) getField(index);}
    
}
