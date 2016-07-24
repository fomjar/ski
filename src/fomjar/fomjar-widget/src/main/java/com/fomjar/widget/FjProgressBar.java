package com.fomjar.widget;

import javax.swing.JProgressBar;

public class FjProgressBar extends JProgressBar {

    private static final long serialVersionUID = 7597214081556427232L;
    private FjWidgetEngine engine;
    private int     multiple;
    private long    speed;
    private float   value_cur;
    private int     value_tar;
    
    public FjProgressBar() {
        engine      = new FjProgressBarEngine();
        multiple    = 100;
        speed       = 160L;
        value_cur   = 0.0f;
        value_tar   = 0;
    }
    
    public void setMultiple(int multiple) {this.multiple = multiple;}
    public int getMultiple() {return multiple;}
    
    @Override
    public void setMaximum(int n) {super.setMaximum(n * multiple);}
    
    @Override
    public void setMinimum(int n) {super.setMinimum(n * multiple);}
    
    @Override
    public void setValue(int n) {
        value_tar = n * multiple;
        if (!engine.isRun()) engine.open();
    }
    
    /**
     * complete time by each {@link #setValue(int)}
     * 
     * @param speed millisecond
     */
    public void setSpeed(long speed) {this.speed = speed;}
    public long getSpeed() {return speed;}

    private class FjProgressBarEngine extends FjWidgetEngine {
        @Override
        public void perform() {
            if (Math.abs(FjProgressBar.this.getValue() - value_tar) == 1) {
                value_cur = value_tar;
                close();
            } else if (FjProgressBar.this.getValue() != value_tar) {
                float total = value_tar - value_cur;
                float step  = total * getInterval() / speed;
                value_cur += step;
            }
            
            FjProgressBar.super.setValue((int) value_cur);
            FjProgressBar.this.repaint();
        }
    }

}
