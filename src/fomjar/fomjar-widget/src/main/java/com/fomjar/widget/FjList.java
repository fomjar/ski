package com.fomjar.widget;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class FjList<E> extends JComponent {

    private static final long serialVersionUID = -3914498112193542403L;
    
    private JPanel cells;
    private boolean isSelectable;
    private FjListCell<E> selectedCell;
    
    public FjList() {
        cells = new JPanel();
        cells.setOpaque(false);
        cells.setLayout(new BoxLayout(cells, BoxLayout.Y_AXIS));
        isSelectable = false;
        setOpaque(false);
        setLayout(new BorderLayout());
        add(cells, BorderLayout.NORTH);
    }
    
    public void removeAllCell() {
        cells.removeAll();
        revalidate();
    }
    
    public void addCell(FjListCell<E> cell) {
        cell.setList(this);
        cells.add(cell);
        revalidate();
    }
    
    public int getCellCount() {
        return cells.getComponentCount();
    }
    
    public FjListCell<E> getCell(int index) {
        return getCells().get(index);
    }
    
    @SuppressWarnings("unchecked")
    public List<FjListCell<E>> getCells() {
        if (0 == getCellCount()) return new LinkedList<FjListCell<E>>();
        
        List<FjListCell<E>> list = new ArrayList<FjListCell<E>>(getCellCount() - 1);
        for (Component c : cells.getComponents()) {list.add((FjListCell<E>) c);}
        return list;
    }
    
    public void setSelectable(boolean isSelectable) {this.isSelectable = isSelectable;}
    public boolean isSelectable() {return this.isSelectable;}
    public FjListCell<E> getSelectedCell() {return selectedCell;}
    
    void notifySelect(FjListCell<E> cell) {
        if (!isSelectable) return;
        if (this != cell.getList()) return;
        
        for (FjListCell<E> c : getCells()) c.setSelected(false);
        cell.setSelected(true);
        selectedCell = cell;
    }

}