package com.ski.stub;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DefaultComboBoxModel;

public class TabRecordAccount extends TabPaneBase {

    private static final long serialVersionUID = 4478338967110972515L;
    
    public TabRecordAccount() {
        addField(CommonUI.createPanelLabelCombo("G       ID  (整数)", new String[] {}));
        addField(CommonUI.createPanelLabelField("GA      ID  (自动生成)"));
        addField(CommonUI.createPanelLabelField("用  户  名  (字符串)"));
        addField(CommonUI.createPanelLabelField("密  码  A   (字符串)"));
        addField(CommonUI.createPanelLabelField("密  码  B   (字符串)"));
        addField(CommonUI.createPanelLabelCombo("当前  密码  (字符串)", new String[] {}));
        addField(CommonUI.createPanelLabelField("出生  日期  (yyyy/mm/dd)"));
        
        DefaultComboBoxModel<String> current = (DefaultComboBoxModel<String>) getFieldToCombo(5).getModel();
        KeyListener listener = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                current.removeAllElements();
                current.addElement(getFieldToField(3).getText());
                current.addElement(getFieldToField(4).getText());
            }
        };
        getField(3).addKeyListener(listener);
        getField(4).addKeyListener(listener);
    }

}
