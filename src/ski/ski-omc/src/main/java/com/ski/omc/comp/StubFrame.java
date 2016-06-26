package com.ski.omc.comp;

import javax.swing.JFrame;

public class StubFrame extends JFrame {
    
    private static StubFrame instance = null;
    public static StubFrame getInstance() {
        if (null == instance) instance = new StubFrame();
        return instance;
    }

    private static final long serialVersionUID = -6552398861110953211L;
    
    private StubFrame() {
        
    }

}
