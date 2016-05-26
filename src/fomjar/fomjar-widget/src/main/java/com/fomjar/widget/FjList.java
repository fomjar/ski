package com.fomjar.widget;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

public class FjList<E> extends JComponent {

    private static final long serialVersionUID = -3914498112193542403L;
    
    public FjList() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalGlue());
    }
    
    public void removeAllCell() {
        super.removeAll();
        add(Box.createVerticalGlue());
        revalidate();
    }
    
    public void addCell(FjListCell<E> cell) {
        remove(getComponentCount() - 1);
        add(cell);
        add(Box.createVerticalGlue());
        revalidate();
    }

}
