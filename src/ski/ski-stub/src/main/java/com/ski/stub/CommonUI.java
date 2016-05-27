package com.ski.stub;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;

public class CommonUI {
    
    public static final Font FONT = new Font("仿宋", Font.PLAIN, 14);

    public static final Color COLOR_MODIFYING = Color.blue;
    
    static {
        UIManager.getLookAndFeelDefaults().put("Label.font",        FONT);
        UIManager.getLookAndFeelDefaults().put("Table.font",        FONT);
        UIManager.getLookAndFeelDefaults().put("TableHeader.font",  FONT);
        UIManager.getLookAndFeelDefaults().put("TextField.font",    FONT);
        UIManager.getLookAndFeelDefaults().put("TextArea.font",     FONT);
        UIManager.getLookAndFeelDefaults().put("TitledBorder.font", FONT);
        UIManager.getLookAndFeelDefaults().put("CheckBox.font",     FONT);
        UIManager.getLookAndFeelDefaults().put("RadioButton.font",  FONT);
        UIManager.getLookAndFeelDefaults().put("ComboBox.font",     FONT);
        UIManager.getLookAndFeelDefaults().put("Button.font",       FONT);
    }

}
