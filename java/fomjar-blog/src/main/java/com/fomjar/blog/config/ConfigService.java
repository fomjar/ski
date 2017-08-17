package com.fomjar.blog.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class ConfigService {
    
    private static final Log logger = LogFactory.getLog(ConfigService.class);
    
    public static final String PATH_DATA = "./data";
    
    public static final String PATH_USERS       = PATH_DATA + "/users";
    public static final String PATH_ARTICLES    = PATH_DATA + "/articles";
    
    public Monitor mon_comm;
    public Monitor mon_users;
    public Monitor mon_articles;
    
    public ConfigService() {
        try {
            new Thread(mon_users    = new MonitorLoad(PATH_USERS  + "/list.json"),  "monitor-users").start();
            new Thread(mon_articles = new MonitorSave(PATH_ARTICLES + "/list.json"),  "monitor-articles").start();
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
            
            this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        }
        
        @Override
        public void run() {
            isRun = true;
            logger.info("[MONITOR START] " + Thread.currentThread().getName());
            while (isRun) {
                try {Thread.sleep(interval);}
                catch (InterruptedException e) {logger.error("monitor sleep interrupted", e);}
                try {monitor();}
                catch (Exception e) {logger.error("monitor failed", e);}
            }
        }
        public abstract void monitor() throws IOException;
        
        public void mod_mem() {mod_mem = true;}
        
        public void save() throws IOException {
            if (0 == config.size()) return;
            
            if (mod_mem) {
                File parent = file.getParentFile();
                if (!parent.isDirectory()) {
                    if (!parent.mkdirs()) {
                        throw new IOException("make directories failed: " + parent.getPath());
                    }
                }
                
                File tmp = new File(file.getPath() + ".tmp");
                mapper.writeValue(tmp, this.config);
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                mod_mem = false;
                logger.info("[MONITOR SAVE] success: " + file.getPath());
            }
        }
        
        public void load() throws IOException {
            if (!file.isFile()) return;
            
            if (file.lastModified() > mod_fs) {
                this.config = mapper.readValue(file, Map.class);
                mod_fs = file.lastModified();
                logger.info("[MONITOR LOAD] success: " + file.getPath());
            }
        }
        
        public Map<String, Object> config() {return config;}
        
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
