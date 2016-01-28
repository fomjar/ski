package fomjar.server.session;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import fomjar.util.FjLoopTask;

public class FjSessionMonitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(FjSessionMonitor.class);
    
    private Map<String, FjSCB> scbs;
    private long timeoutToMark;
    private long timeoutToRemove;
    
    FjSessionMonitor(Map<String, FjSCB> scbs) {
        this.scbs = scbs;
        setTimeoutToMark  ( 2 * 60 * 60 * 1000L);
        setTimeoutToRemove(24 * 60 * 60 * 1000L);
        setInterval(10 * 1000L);
    }
    
    public void start() {
        if (isRun()) {
            logger.warn("fjsession-monitor has already started");
            return;
        }
        new Thread(this, "fjsession-monitor").start();
    }
    
    @Override
    public void perform() {
        synchronized (scbs) {
            scbs.entrySet()
                    .stream()
                    .filter((entry)->{return !entry.getValue().has("mark.timeout");})
                    .forEach((entry)->{
                        long time_open = entry.getValue().getLong("time.open");
                        if (System.currentTimeMillis() - time_open >= getTimeoutToMark()) {
                            logger.info("session timeout mark: " + entry.getKey());
                            entry.getValue().put("mark.timeout", true);
                        }
                    });
            List<String> sidToRemove = scbs.entrySet()
                    .stream()
                    .filter((entry)->{return System.currentTimeMillis() - entry.getValue().getLong("time.open") >= getTimeoutToRemove();})
                    .map((entry)->{
                        logger.info("session timeout remove: " + entry.getKey());
                        return entry.getKey();
                    })
                    .collect(Collectors.toList());
            if (null != sidToRemove && !sidToRemove.isEmpty())
                for (String sid : sidToRemove)
                    scbs.remove(sid);
        }
    }
    
    public void setTimeoutToMark(long timeInMillisecond)   {this.timeoutToMark = timeInMillisecond;}
    public long getTimeoutToMark() {return timeoutToMark;}
    
    public void setTimeoutToRemove(long timeInMillisecond) {this.timeoutToRemove = timeInMillisecond;}
    public long getTimeoutToRemove() {return timeoutToRemove;}

}
