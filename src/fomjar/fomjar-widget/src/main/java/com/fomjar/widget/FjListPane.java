package com.fomjar.widget;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class FjListPane<E> extends JScrollPane {

    private static final long serialVersionUID = 3635223056300818407L;
    private FjSearchBar searchBar;
    private FjList<E> list;
    
    public FjListPane() {
        searchBar = new FjSearchBar();
        searchBar.setVisible(false);
        list = new FjList<E>();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(list, BorderLayout.CENTER);
        setViewportView(panel);
        
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        getVerticalScrollBar().setUnitIncrement(20);
    }
    
    public void enableSearchBar() {searchBar.setVisible(true);}
    
    public FjSearchBar getSearchBar() {return searchBar;}
    
    public FjList<E> getList() {return list;}

}
