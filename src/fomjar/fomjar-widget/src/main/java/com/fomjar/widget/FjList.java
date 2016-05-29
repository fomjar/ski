package com.fomjar.widget;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

public class FjList<E> extends JComponent {

    private static final long serialVersionUID = -3914498112193542403L;
    
    public FjList() {
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
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
    
    public int getCellCount() {
        return getCells().size();
    }
    
    public FjListCell<E> getCell(int index) {
        return getCells().get(index);
    }
    
    @SuppressWarnings("unchecked")
    public List<FjListCell<E>> getCells() {
        List<FjListCell<E>> cells = new ArrayList<FjListCell<E>>(getComponentCount() - 1);
        for (Component c : getComponents()) {
            if (c instanceof FjListCell<?>) cells.add((FjListCell<E>) c);
        }
        return cells;
    }

}