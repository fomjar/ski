package com.fomjar.widget;

import javax.swing.JProgressBar;

public class FjProgressBar extends JProgressBar implements FjDaemon {

    private static final long serialVersionUID = 7597214081556427232L;
    private Helper  helper;
    private boolean daemon;
    private float   value_cur;
    private int     value_tar;
    
    public FjProgressBar() {
        helper      = new Helper();
        value_cur   = 0.0f;
        value_tar   = 0;
    }
    
    @Override
    public void setValue(int n) {
        if (daemon) value_tar = n;
        else super.setValue(n);
    }
    
    @Override
    public void openDaemon() {
        if (daemon) return;
        
        daemon = true;
        FjDaemonPool.pool().submit(helper);
    }

    @Override
    public void closeDaemon() {
        daemon = false;
    }

    private class Helper implements Runnable {
        private static final long finish    = 200L;
        private static final long interval  = 20L;
        @Override
        public void run() {
            while (daemon) {
                if (Math.abs(FjProgressBar.this.getValue() - value_tar) == 1) {
                    value_cur = value_tar;
                } else if (FjProgressBar.this.getValue() != value_tar) {
                    float total = value_tar - value_cur;
                    float step  = total * interval / finish;
                    value_cur += step;
                }
                FjProgressBar.super.setValue((int) value_cur);
                FjProgressBar.this.repaint();
                try {Thread.sleep(interval);}
                catch (InterruptedException e) {e.printStackTrace();}
            }
        }
    }

}
