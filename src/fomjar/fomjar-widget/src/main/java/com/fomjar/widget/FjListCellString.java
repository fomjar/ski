package com.fomjar.widget;

import java.awt.BorderLayout;

import javax.swing.JLabel;

public class FjListCellString extends FjListCell<String> {

    private static final long serialVersionUID = 615786048674394192L;
    private JLabel major;
    private JLabel minor;

    public FjListCellString(String major) {
        this(major, null);
    }

    public FjListCellString(String major, String minor) {
        super(null != minor ? (major + minor) : major);
        setLayout(new BorderLayout());
        add(this.major = new JLabel(major), BorderLayout.CENTER);
        add(this.minor = new JLabel(minor), BorderLayout.EAST);
    }
    
    public void setMajor(String major) {this.major.setText(major);}
    public String getMajor() {return this.major.getText();}
    
    public void setMinor(String minor) {this.minor.setText(minor);}
    public String getMinor() {return this.minor.getText();}
}
