package com.ski.vcg.omc.comp;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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

public class ReportDialog extends JDialog {

    private static final long serialVersionUID = 5478304934566261533L;

    private JEditorPane jep;

    public ReportDialog() {
        super(MainFrame.getInstance(), "报告");
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        jep = new JEditorPane();
        jep.setEditable(false);
        jep.setContentType("text/html");
        JScrollPane jsp = new JScrollPane(jep);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("复制为图片"));
        toolbar.add(new JButton("保存为图片"));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(jsp, BorderLayout.CENTER);

        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            BufferedImage img = getReportAsImage();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(img), null);
        });
        ((JButton) toolbar.getComponent(1)).addActionListener(e->{
            BufferedImage img = getReportAsImage();

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(String.format("%s%s.png", getTitle(), new SimpleDateFormat("-yyyyMMddHHmmss").format(new Date()))));
            if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(ReportDialog.this)) {
                File file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".png")) file = new File(file.getAbsolutePath() + ".png");
                try {
                    ImageIO.write(img, "png", file);
                    JOptionPane.showMessageDialog(ReportDialog.this, "保存成功: " + file.getAbsolutePath(), "信息", JOptionPane.PLAIN_MESSAGE);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(ReportDialog.this, "保存失败: " + e1.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
            }
        });
    }

    private BufferedImage getReportAsImage() {
        BufferedImage buffer = new BufferedImage(jep.getWidth(), jep.getHeight(), BufferedImage.TYPE_INT_RGB);
        jep.paint(buffer.getGraphics());
        return buffer;
    }

    public void setReport(String report) {
        jep.setText(report);
        jep.setSelectionStart(0);
        jep.setSelectionEnd(0);
    }

    public static class ImageSelection implements Transferable {

        private Image image;

        public ImageSelection(Image image) {this.image = image;}

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {throw new UnsupportedFlavorException(flavor);}
            return image;
        }
    }

}
