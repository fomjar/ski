package fomjar.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public abstract class FjFixedTask implements FjTask {
    
    private static final Logger logger = Logger.getLogger(FjFixedTask.class);
    
    private static ExecutorService pool = null;
    
    private long time;
    
    public FjFixedTask() {this(Long.MAX_VALUE);}
    
    public FjFixedTask(long time) {this.time = time;}
    
    public long getFixedTime() {return time;}

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        
        Thread curr = Thread.currentThread();
        if (null == pool) pool = Executors.newCachedThreadPool();
        pool.submit(()->{
            try {
                perform();
                curr.interrupt();
                
                try {onPerformDone(System.currentTimeMillis() - start);}
                catch (Exception e) {logger.error("on perform done failed", e);}
            } catch (Exception e) {logger.error("perform failed", e);}
        });
        
        try {
            Thread.sleep(getFixedTime());
            
            try {onTimeOut();}
            catch (Exception e1) {logger.error("on timeout failed", e1);}
        } catch (InterruptedException e) {
            try {onInTime(System.currentTimeMillis() - start);}
            catch (Exception e2) {logger.error("on intime failed", e2);}
        }
    }
    
    protected void onInTime(long timecost) {}
    
    protected void onTimeOut() {}
    
    protected void onPerformDone(long timecost) {}
    
}
