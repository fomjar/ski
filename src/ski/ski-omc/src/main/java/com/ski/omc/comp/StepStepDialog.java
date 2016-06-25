package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
        this.index = 1;
        this.progress = new JProgressBar(0, steps.length);
        this.progress.setValue(this.index);
        this.progress.setStringPainted(true);
        
        jlb_steps = new JLabel[steps.length];
        for (int i = 0; i < jlb_steps.length; i++) {
            jlb_steps[i] = new JLabel(steps[i].name);
            jlb_steps[i].setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        }
        this.jta_desc = new JTextArea();
        this.jta_desc.setLineWrap(true);
        this.jta_desc.setEditable(false);
        this.jsp_desc = new JScrollPane(jta_desc);
        this.jsp_desc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.jsp_desc.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel panel_steps = new JPanel();
        panel_steps.setLayout(new BoxLayout(panel_steps, BoxLayout.Y_AXIS));
        panel_steps.add(Box.createVerticalGlue());
        for (JLabel label : jlb_steps) panel_steps.add(label);
        panel_steps.add(Box.createVerticalGlue());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(panel_steps, BorderLayout.WEST);
        panel.add(jsp_desc, BorderLayout.CENTER);
        
        ((JComponent) getContentPane()).setBorder(BorderFactory.createLineBorder(Color.darkGray));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(progress, BorderLayout.SOUTH);
        
        getContentPane().addMouseMotionListener(new MouseMotionAdapter() {
            private int x;
            private int y;
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(getX() + (e.getX() - x), getY() + (e.getY() - y));
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                x = e.getX();
                y = e.getY();
            }
        });
        
        setTitle("正在操作");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setSize(new Dimension(600, 300));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        update();
    }
    
    public void toPrevStep() {
        index--;
        if (index < 1) index = 1;
        update();
    }
    
    public void toNextStep() {
        index++;
        if (index > steps.length) index = steps.length;
        update();
    }
    
    private void update() {
        for (JLabel step : jlb_steps) step.setFont(step.getFont().deriveFont(Font.PLAIN));
        jlb_steps[index - 1].setFont(jlb_steps[index - 1].getFont().deriveFont(Font.BOLD));
        if (0 < jta_desc.getText().length()) appendDescription("");
        appendDescription(steps[index - 1].desc);
        progress.setValue(index);
        progress.setString(String.format("%d / %d", index, steps.length));
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
