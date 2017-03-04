package com.ski.frs.web;

import java.util.HashMap;
import java.util.Map;

import fomjar.server.msg.FjHttpResponse;

public class CacheResponse {
    
    private static CacheResponse instance = null;
    
    public static synchronized CacheResponse getInstance() {
        if (null == instance) instance = new CacheResponse();
        return instance;
    }
    
    private Map<String, FjHttpResponse> map;
    
    private CacheResponse() {
        map = new HashMap<String, FjHttpResponse>();
    }
    
    public void cacheWait(String sid, FjHttpResponse response) {
        map.put(sid, response);
        synchronized (response) {
            try {response.wait(1000L * 8);}
            catch (InterruptedException e) {e.printStackTrace();}
        }
    }
    
    public FjHttpResponse cache(String sid) {
        return map.get(sid);
    }
    
    public void cacheNotify(String sid) {
        FjHttpResponse response = map.remove(sid);
        if (null != response) {
            synchronized (response) {response.notify();}
        }
    }

}
