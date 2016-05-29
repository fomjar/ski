package com.fomjar.widget;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

public class FjSearchBar extends JComponent {

    private static final long serialVersionUID = 1371918084585677391L;
    private static final int INSETS = 6;
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
        
        this.types.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, INSETS));
        setBorder(BorderFactory.createEmptyBorder(INSETS, INSETS, INSETS, INSETS));
        setLayout(new BorderLayout());
        add(this.types, BorderLayout.WEST);
        add(this.field, BorderLayout.CENTER);
        
        field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {doSearch();}
        });
        
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {doSearch();}
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
    
    private void doSearch() {
        try {
            String type = FjSearchBar.this.types.isVisible() ? FjSearchBar.this.types.getSelectedItem().toString() : null;
            String[] words0 = field.getText().split(" ");
            List<String> words = new ArrayList<String>(words0.length);
            for (String word : words0) if (null != word && 0 < word.length()) words.add(word);
            listeners.forEach(listener->listener.searchPerformed(type, words.toArray(new String[] {})));
        } catch (Exception e) {e.printStackTrace();}
    }
    
}
