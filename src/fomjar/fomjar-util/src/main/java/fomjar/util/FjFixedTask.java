package fomjar.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class FjFixedTask implements FjTask {
    
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    
    private long time;
    
    public FjFixedTask() {
        this(Long.MAX_VALUE);
    }
    
    public FjFixedTask(long time) {
        this.time = time;
    }
    
    public long getFixedTime() {
        return time;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        
        Thread curr = Thread.currentThread();
        pool.submit(()->{
            try {
                perform();
                
                try {onPerformDone(System.currentTimeMillis() - start);}
                catch (Exception e) {e.printStackTrace();}
                
                curr.interrupt();
            } catch (Exception e) {e.printStackTrace();}
        });
        
        try {
            Thread.sleep(getFixedTime());
            
            try {onTimeOut();}
            catch (Exception e1) {e1.printStackTrace();}
        } catch (InterruptedException e) {
            try {onInTime(System.currentTimeMillis() - start);}
            catch (Exception e2) {e2.printStackTrace();}
        }
    }
    
    protected void onInTime(long timecost) {}
    
    protected void onTimeOut() {}
    
    protected void onPerformDone(long timecost) {}
    
}
