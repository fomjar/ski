package com.fomjar.blog.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ConfigService {
    
    private static final Log logger = LogFactory.getLog(ConfigService.class);
    
    public static final String PATH_ROOT = "./document";
    
    public static final String PATH_CONFIG  = "./config";
    public static final String PATH_ARTICLE = PATH_ROOT + "/article";
    
    public Monitor mon_comm;
    public Monitor mon_auth;
    public Monitor mon_article_list;
    
    public ConfigService() {
        try {
            new Thread(mon_comm         = new MonitorLoad(PATH_CONFIG  + "/comm.json"),  "monitor-load-comm").start();
            new Thread(mon_auth         = new MonitorSave(PATH_CONFIG  + "/auth.json"),  "monitor-load-auth").start();
            new Thread(mon_article_list = new MonitorSave(PATH_ARTICLE + "/list.json"),  "monitor-save-article-list").start();
        } catch (IOException e) {logger.error("start monitor thread failed", e);}
    }
    
    @SuppressWarnings({"unchecked"})
    public abstract static class Monitor implements Runnable {
        
        protected Map<String, Object> config;
        protected File          file;
        private boolean         mod_mem;
        private long            mod_fs;
        private ObjectMapper    mapper;
        private boolean         isRun;
        private long            interval;
        
        public Monitor(String path) {
            this.config     = new HashMap<>();
            this.file       = new File(path);
            this.mod_mem    = false;
            this.mod_fs     = 0;
            this.mapper     = new ObjectMapper();
            this.isRun      = false;
            this.interval   = 1000L * 10;
        }
        
        @Override
        public void run() {
            isRun = true;
            while (isRun) {
                try {Thread.sleep(interval);}
                catch (InterruptedException e) {logger.error("monitor sleep interrupted", e);}
                try {monitor();}
                catch (Exception e) {logger.error("monitor failed", e);}
            }
        }
        public abstract void monitor() throws IOException;
        
        public void save() throws IOException {
            if (0 == config.size()) return;
            
            if (mod_mem) {
                File parent = file.getParentFile();
                if (!parent.isDirectory()) {
                    if (!parent.mkdirs()) {
                        throw new IOException("make directories failed: " + parent.getPath());
                    }
                }
                FileOutputStream fos = new FileOutputStream(file.getPath() + ".tmp", false);
                try {mapper.writeValue(fos, this.config);}
                catch (IOException e) {throw e;}
                finally {fos.close();}
                
                Files.move(new File(file.getPath() + ".tmp").toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                mod_mem = false;
            }
        }
        
        public void load() throws IOException {
            if (!file.isFile()) return;
            
            if (file.lastModified() > mod_fs) {
                FileInputStream fis = new FileInputStream(file);
                try {this.config = mapper.readValue(fis, Map.class);}
                catch (IOException e) {throw e;}
                finally {fis.close();}
                mod_fs = file.lastModified();
            }
        }
        
        public Map<String, Object> config() {
            mod_mem = true;
            return config;
        }
        
        public boolean isRun() {return isRun;}
        
        public void setInterval(long interval) {this.interval = interval;}
        
        public void close() {isRun = false;}
    }
    
    private static class MonitorLoad extends Monitor {
        
        public MonitorLoad(String path) throws IOException {
            super(path);
            load();
        }

        @Override
        public void monitor() throws IOException {
            load();
        }
    }
    
    private static class MonitorSave extends Monitor {
        
        public MonitorSave(String path) throws IOException {
            super(path);
            load();
        }

        @Override
        public void monitor() throws IOException {
            save();
        }
    }

}
