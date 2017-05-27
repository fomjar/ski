package com.ski.frs.ccu;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.ski.frs.ccu.cb.SBDevice;
import com.ski.frs.ccu.cb.SBPicture;
import com.ski.frs.ccu.cb.SBSubject;

import fomjar.util.FjLoopTask;
import fomjar.util.FjThreadFactory;

public class StoreBlockService {
    
    private static final Logger logger = Logger.getLogger(StoreBlockService.class);
    
    private static ExecutorService pool = null;
    private static StoreBlockService instance = null;
    public static synchronized StoreBlockService getInstance() {
        if (null == instance) {
            pool = Executors.newCachedThreadPool(new FjThreadFactory("sbs"));
            instance = new StoreBlockService();
        }
        return instance;
    }
    
    private FjLoopTask  monitor;
    private SBDevice    sb_dev;
    private SBPicture   sb_pic;
    private SBSubject   sb_sub;
    
    private StoreBlockService() {
        monitor = new Monitor();
        sb_dev = new SBDevice();
        sb_pic = new SBPicture();
        sb_sub = new SBSubject();
    }
    
    public void open() {
        if (monitor.isRun()) return;
        new Thread(monitor, "sbs-monitor").start();
        if (sb_dev.file().isFile()) {
            pool.submit(()->{
                try {
                    logger.info("load sb dev begin");
                    sb_dev = (SBDevice) sb_dev.load();
                    logger.info("load sb dev success");
                } catch (ClassNotFoundException | IOException e) {logger.error("load sb dev failed", e);}
            });
        }
        if (sb_pic.file().isFile()) {
            pool.submit(()->{
                try {
                    logger.info("load sb pic begin");
                    sb_pic = (SBPicture) sb_pic.load();
                    logger.info("load sb pic success");
                } catch (ClassNotFoundException | IOException e) {logger.error("load sb pic failed", e);}
            });
        }
        if (sb_sub.file().isFile()) {
            pool.submit(()->{
                try {
                    logger.info("load sb sub begin");
                    sb_sub = (SBSubject) sb_sub.load();
                    logger.info("load sb sub success");
                } catch (ClassNotFoundException | IOException e) {logger.error("load sb sub failed", e);}
            });
        }
    }
    
    public void close() {monitor.close();}
    
    private class Monitor extends FjLoopTask {
        
        private static final long TIMEOUT = 1000L * 60 * 20;
        
        public Monitor() {
            setDelay(TIMEOUT);
            setInterval(TIMEOUT);
        }
        @Override
        public void perform() {
            pool.submit(()->{
                try {
                    logger.info("save sb dev begin");
                    sb_dev.save();
                    logger.info("save sb dev success, file size: " + sb_dev.file().length());
                } catch (IOException e) {logger.error("save sb dev failed", e);}
            });
            pool.submit(()->{
                try {
                    logger.info("save sb pic begin");
                    sb_pic.save();
                    logger.info("save sb pic success, file size: " + sb_pic.file().length());
                } catch (IOException e) {logger.error("save sb pic failed", e);}
            });
            pool.submit(()->{
                try {
                    logger.info("save sb sub begin");
                    sb_sub.save();
                    logger.info("save sb sub success, file size: " + sb_sub.file().length());
                } catch (IOException e) {logger.error("save sb sub failed", e);}
            });
        }
        
    }

}
