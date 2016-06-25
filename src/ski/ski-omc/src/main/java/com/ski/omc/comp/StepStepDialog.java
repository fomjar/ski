package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import com.ski.omc.MainFrame;

public class StepStepDialog extends JDialog {

    private static final long serialVersionUID = 4665927096547901460L;
    private Step[]      steps;
    private int         index;
    private JProgressBar progress;
    private JLabel[]    jlb_steps;
    private JScrollPane jsp_desc;
    private JTextArea   jta_desc;
    
    public StepStepDialog(Step[] steps) {
        super(MainFrame.getInstance());
        
        this.steps = steps;
        this.index = 0;
        this.progress = new JProgressBar(0, steps.length - 1);
        this.progress.setValue(this.index);
        
        jlb_steps = new JLabel[steps.length];
        for (int i = 0; i < jlb_steps.length; i++) {
            jlb_steps[i] = new JLabel(steps[i].name);
        }
        this.jta_desc = new JTextArea();
        this.jta_desc.setLineWrap(true);
        this.jta_desc.setEditable(false);
        this.jsp_desc = new JScrollPane(jta_desc);
        this.jsp_desc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.jsp_desc.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel panel_steps = new JPanel();
        panel_steps.setLayout(new GridLayout(jlb_steps.length, 1));
        for (JLabel label : jlb_steps) panel_steps.add(label);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(panel_steps, BorderLayout.WEST);
        panel.add(jsp_desc, BorderLayout.CENTER);
        
        ((JComponent) getContentPane()).setBorder(BorderFactory.createLineBorder(Color.darkGray));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(progress, BorderLayout.SOUTH);
        
        setTitle("正在操作");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setSize(new Dimension(600, 100));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        update();
    }
    
    public void toPrevStep() {
        index--;
        update();
    }
    
    public void toNextStep() {
        index++;
        update();
    }
    
    private void update() {
        for (JLabel step : jlb_steps) step.setFont(step.getFont().deriveFont(Font.PLAIN));
        jlb_steps[index].setFont(jlb_steps[index].getFont().deriveFont(Font.BOLD));
        appendDescription(steps[index].desc);
        progress.setValue(index);
    }
    
    public void appendDescription(String desc) {
        jta_desc.append(desc + "\n");
        jta_desc.setSelectionStart(jta_desc.getText().length());
        jta_desc.setSelectionEnd(jta_desc.getText().length());
    }
    
    public static class Step {
        public String name;
        public String desc;
        public Step(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }

}
