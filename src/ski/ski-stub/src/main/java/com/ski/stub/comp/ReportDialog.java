package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class ReportDialog extends JDialog {

    private static final long serialVersionUID = 5478304934566261533L;
    
    private JEditorPane jep;
    
    public ReportDialog() {
        setTitle("报告");
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        jep = new JEditorPane();
        jep.setEditable(false);
        jep.setContentType("text/html");
        HTMLEditorKit ek = new HTMLEditorKit();
        jep.setEditorKit(ek);
        StyleSheet ss = ek.getStyleSheet();
        ss.addRule("table {width:100%; border-spacing: 0; font-family: '微软雅黑', 'Hiragino Sans GB'}");
        ss.addRule("td {border: 1px solid black; text-align: center; background-color: #EEEEEE}");
        ss.addRule("h1 {color: #884444}");
        ss.addRule("h2 {text-align: left; padding-left: 8px}");
        ss.addRule(".category {background-color: #444488; color: #EEEEEE}");
        jep.setDocument(ek.createDefaultDocument());
        JScrollPane jsp = new JScrollPane(jep);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("另存为图片"));
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(jsp, BorderLayout.CENTER);
        
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            BufferedImage buffer = new BufferedImage(jep.getWidth(), jep.getHeight(), BufferedImage.TYPE_INT_RGB);
            jep.paint(buffer.getGraphics());
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(String.format("%s%s.png", getTitle(), new SimpleDateFormat("-yyyyMMddHHmmss").format(new Date(System.currentTimeMillis())))));
            if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(ReportDialog.this)) {
                File file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".png")) file = new File(file.getAbsolutePath() + ".png");
                try {
                    ImageIO.write(buffer, "png", file);
                    JOptionPane.showConfirmDialog(null, "保存成功: " + file.getAbsolutePath(), "信息", JOptionPane.DEFAULT_OPTION);
                } catch (Exception e1) {
                    JOptionPane.showConfirmDialog(null, "保存失败: " + e1.getMessage(), "错误", JOptionPane.DEFAULT_OPTION);
                    e1.printStackTrace();
                }
            }
        });
    }
    
    public void setReport(String report) {
        jep.setText(report);
        jep.setSelectionStart(0);
        jep.setSelectionEnd(0);
    }
}
