package com.fomjar.widget;

import java.awt.BorderLayout;

import javax.swing.JLabel;

public class FjListCellString extends FjListCell<String> {

    private static final long serialVersionUID = 615786048674394192L;

    public FjListCellString(String text) {
        super(text);
        setLayout(new BorderLayout());
        add(new JLabel(text));
    }
}
