package com.fomjar.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
        this.listeners = new LinkedList<>();

        this.types.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, INSETS));
        this.types.setPreferredSize(new Dimension(120, field.getHeight()));
        setBorder(BorderFactory.createEmptyBorder(INSETS, INSETS, INSETS, INSETS));
        setLayout(new BorderLayout());
        add(this.types, BorderLayout.WEST);
        add(this.field, BorderLayout.CENTER);

        this.types.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {doSearch();}
        });

        this.field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {doSearch();}
        });

        this.field.addKeyListener(new KeyAdapter() {
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
        this.field.setDefaultTips(text);
    }

    public void addSearchListener(FjSearchListener listener) {
        listeners.add(listener);
    }

    public void doSearch() {
        try {
            String type = FjSearchBar.this.types.isVisible() ? FjSearchBar.this.types.getSelectedItem().toString() : null;
            String[] words0 = field.getText().split(" ");
            List<String> words = new ArrayList<String>(words0.length);
            for (String word : words0) if (null != word && 0 < word.length()) words.add(word);
            listeners.forEach(listener->listener.searchPerformed(type, words.toArray(new String[] {})));
        } catch (Exception e) {e.printStackTrace();}
    }

    public static interface FjSearchListener {
        void searchPerformed(String type, String[] words);
    }

    public static abstract class FjSearchAdapterForFjList<E> implements FjSearchListener {

        private final FjList<E> list;

        public FjSearchAdapterForFjList(FjList<E> list) {
            this.list = list;
        }

        @Override
        public void searchPerformed(String type, String[] words) {
            list.getCells().forEach(cell->{
                if (isMatch(type, words, cell.getData())) cell.setVisible(true);
                else cell.setVisible(false);
            });
            list.repaint();
        }

        public abstract boolean isMatch(String type, String[] words, E celldata);

    }
}
