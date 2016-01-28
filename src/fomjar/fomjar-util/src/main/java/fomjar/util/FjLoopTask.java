package fomjar.util;

public abstract class FjLoopTask implements Runnable {

    private long delay;
    private long interval;
    private boolean isRun;
    
    public FjLoopTask() {
        this(0L, 0L);
    }
    
    public FjLoopTask(long interval) {
        this(0L, interval);
    }
    
    public FjLoopTask(long delay, long interval) {
        this.delay = delay;
        this.interval = interval;
        isRun = false;
    }
    
    /**
     * @return milliseconds
     */
    public long getDelay() {
        return delay;
    }
    
    /**
     * @param delay in milliseconds
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }
    
    /**
     * @return millisecond
     */
    public long getInterval() {
        return interval;
    }

    public void setInterval(long millisecond) {
        this.interval = millisecond;
    }

    public boolean isRun() {
        return isRun;
    }
    
    public void close()     {
        isRun = false;
    }

    @Override
    public void run() {
        isRun = true;
        try {Thread.sleep(getDelay());}
        catch (InterruptedException e) {e.printStackTrace();}
        while(isRun) {
            long start = System.currentTimeMillis();
            try {perform();}
            catch (Exception e) {e.printStackTrace();}
            long end = System.currentTimeMillis();
            long delta = end - start;
            if (delta >= getInterval()) Thread.yield();
            else {
                try {Thread.sleep(getInterval() - delta);}
                catch (InterruptedException e) {e.printStackTrace();}
            }
        }
    }
    
    public abstract void perform();
    
}
