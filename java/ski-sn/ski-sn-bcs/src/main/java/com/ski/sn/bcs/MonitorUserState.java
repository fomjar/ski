package com.ski.sn.bcs;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ski.sn.common.CommonDefinition;
import com.ski.sn.common.CommonService;

import fomjar.util.FjLoopTask;
import net.sf.json.JSONObject;

public class MonitorUserState extends FjLoopTask {
    
    private Map<Integer, Long> cache;
    
    public MonitorUserState() {
        cache = new ConcurrentHashMap<Integer, Long>();
        setInterval(1000 * 60);
    }
    
    public void start() {
        if (isRun()) return;
        
        new Thread(this, "monitor-userstate").start();
    }
    
    public void notify(int uid) {
        cache.put(uid, System.currentTimeMillis());
    }

    @Override
    public void perform() {
        long now = System.currentTimeMillis();
        Set<Integer> toremove = new HashSet<Integer>();
        cache.forEach((uid, time)->{
            if (now - time >= 1000 * 60) { // logout
                JSONObject args = new JSONObject();
                args.put("uid", uid);
                args.put("state", CommonDefinition.Field.USER_STATE_OFFLINE);
                toremove.add(uid);
                CommonService.requesta("cdb", CommonDefinition.ISIS.INST_UPDATE_USER_STATE, args);
            }
        });
        toremove.forEach(uid->cache.remove(uid));
    }

}
