package com.ski.bcs.monitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.ski.common.bean.BeanChannelAccount;

import fomjar.server.FjServerToolkit;
import fomjar.util.FjLoopTask;

public class CacheMonitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(CacheMonitor.class);
    
    public Map<String, Cache> cache_rent_end_fail_for_pass;
    
    public CacheMonitor() {
        cache_rent_end_fail_for_pass = new HashMap<String, Cache>();
    }
    
    public boolean isCacheRentEndFailForPass(BeanChannelAccount user) {
        synchronized (cache_rent_end_fail_for_pass) {
            return cache_rent_end_fail_for_pass.containsKey(user.c_user);
        }
    }
    
    public void putCacheRentEndFailForPass(BeanChannelAccount user) {
        synchronized (cache_rent_end_fail_for_pass) {
            cache_rent_end_fail_for_pass.put(user.c_user, new Cache(user.c_user));
        }
    }
    
    public void start() {
        if (isRun()) {
            logger.warn("monitor-cache has already started");
            return;
        }
        new Thread(this, "monitor-cache").start();
    }
    
    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("bcs.monitor.cache.interval"));
        setInterval(second * 1000);
    }
    
    @Override
    public void perform() {
        resetInterval();
        
        synchronized (cache_rent_end_fail_for_pass) {
            List<String> toRemove = cache_rent_end_fail_for_pass.entrySet()
                    .stream()
                    .filter(entry->{
                        long timeout = Long.parseLong(FjServerToolkit.getServerConfig("bcs.monitor.cache.timeout")) * 1000L;
                        return System.currentTimeMillis() - entry.getValue().time >= timeout;
                    })
                    .map(entry->entry.getKey())
                    .collect(Collectors.toList());
            toRemove.forEach(user->cache_rent_end_fail_for_pass.remove(user));
        }
    }
    
    public static class Cache {
        
        String     user;
        long     time;
        
        public Cache(String user) {
            this.user = user;
            this.time = System.currentTimeMillis();
        }
    }
    

}
