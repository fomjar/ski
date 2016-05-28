package com.fomjar.widget;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

public class FjSearchBar extends JComponent {

    private static final long serialVersionUID = 1371918084585677391L;
    private JComboBox<String>   types;
    private FjTextField         field;
    private List<FjSearchListener> listeners;
    
    public FjSearchBar() {
        this(null, null);
    }
    
    public FjSearchBar(String[] types, String tipText) {
        this.types = new JComboBox<String>();
        setSearchTypes(types);
        this.field = new FjTextField();
        setSearchTips(tipText);
        this.listeners = new LinkedList<FjSearchListener>();
        
        setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        setLayout(new BorderLayout());
        add(this.types, BorderLayout.WEST);
        add(this.field, BorderLayout.CENTER);
        
        field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String type = FjSearchBar.this.types.isVisible() ? FjSearchBar.this.types.getSelectedItem().toString() : null;
                String[] words = field.getText().split(" ");
                listeners.forEach(listener->listener.searchPerformed(type, words));
            }
        });
    }
    
    public void setSearchTypes(String[] types) {
        if (null == types || 0 == types.length) this.types.setVisible(false);
        else {
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) this.types.getModel();
            model.removeAllElements();
            for (String type : types) model.addElement(type);
            this.types.setVisible(true);
        }
    }
    
    public void setSearchTips(String text) {
        this.field.setTipText(text);
    }
    
    public void addSearchListener(FjSearchListener listener) {
        listeners.add(listener);
    }
    
    public static interface FjSearchListener {
        void searchPerformed(String type, String[] words);
    }
    
}
