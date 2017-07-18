package com.ski.frs.ccu.tools;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ski.frs.ccu.StoreBlockService;
import com.ski.frs.isis.ISIS;

import net.sf.json.JSONObject;

public class SBMaker {
    
    private String  did;
    private int     count;
    private int     fv;
    
    public SBMaker() {
        did = null;
        count = 10000000;
        fv = 88;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public void setDid(String did) {
        this.did = did;
    }
    
    public void setFv(int fv) {
        this.fv = fv;
    }
    
    public void make() {
        if (null == did) throw new IllegalArgumentException("did must be not null");
        
        print("make begin");
        print("count: " + count);
        StoreBlockService sbs = StoreBlockService.getInstance();
        Random r = new Random();
        print("==========make begin==========");
        {
            JSONObject dev = new JSONObject();
            dev.put("did", did);
            dev.put("path", "path-" + randomString());
            sbs.sb_dev.setDevice(dev);
        }
        
        int g = 20;
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int i = 0; i < g; i++) {
        		pool.submit(()->{
	        		for (int j = 0; j < this.count / g; j++) {
                    JSONObject args = new JSONObject();
                    args.put("did",     did);
                    args.put("name",    "name-" + randomString());
                    args.put("type",    ISIS.FIELD_TYPE_MAN_FACE);
                    args.put("size",    ISIS.FIELD_PIC_SIZE_SMALL);
                    args.put("path",    "path-" + randomString());
                    args.put("name",    "name-" + randomString());
                    args.put("dpath",   "dpath-" + randomString());
                    args.put("gender",  r.nextInt() % 3);
                    args.put("age",     r.nextInt() % 9);
                    args.put("hat",     r.nextInt() % 9);
                    args.put("glass",   r.nextInt() % 9);
                    args.put("mask",    r.nextInt() % 9);
                    args.put("cloth",   r.nextInt() % 9);
                    args.put("nation",  r.nextInt() % 9);
                    double[] fvs = new double[fv];
                    for (int k = 0; k < fvs.length; k++) {
                        fvs[k] = r.nextDouble();
                        fvs[k] = fvs[k] - Math.floor(fvs[k]);
                    }
                    args.put("fv",      fvs);
                    
                    sbs.dispatch(ISIS.INST_SET_PIC, args);
                    if (j % (this.count / g / g) == 0) {
                        print("progress: " + j * g);
                    }
	        		}
        		});
        }
        pool.shutdown();
        try {pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);}
        catch (InterruptedException e) {e.printStackTrace();}
        print("==========make end==========");
        print("==========save begin==========");
        try {
            print("save path: " + sbs.sb_pic.path());
            sbs.sb_pic.save();
            print("==========save end==========");
        } catch (IOException e) {
            print("==========save failed==========");
            e.printStackTrace();
        }
    }
    
    private static String randomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static void print(String text) {
        System.out.println(String.format("[%s] %s", sdf.format(new Date()), text));
    }

}
