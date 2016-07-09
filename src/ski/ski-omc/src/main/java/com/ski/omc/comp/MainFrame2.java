package com.ski.omc.comp;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.fomjar.widget.FjListPane;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;

public class MainFrame2 extends JFrame {

    private static final long serialVersionUID = 8460467197557992622L;
    
    private static MainFrame2 instance = null;
    public static MainFrame2 getInstance() {
        if (null == instance) instance = new MainFrame2();
        return instance;
    }
    
    private FjListPane<BeanChannelAccount>  users;
    private JPanel                          detail;
    
    public MainFrame2() {
        setTitle(String.format("SKI-OMC-%s [%s]", CommonDefinition.VERSION, CommonService.getWsiUrl()));
        setSize(1280, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

}
