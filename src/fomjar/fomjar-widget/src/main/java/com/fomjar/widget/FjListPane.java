package com.fomjar.widget;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class FjListPane<E> extends JScrollPane {

    private static final long serialVersionUID = 3635223056300818407L;
    private FjList<E> list;
    
    public FjListPane() {
        list = new FjList<E>();
        setViewportView(list);
        
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    }
    
    public FjList<E> getList() {return list;}

}
