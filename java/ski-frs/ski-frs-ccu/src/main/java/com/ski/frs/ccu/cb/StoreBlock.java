package com.ski.frs.ccu.cb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import fomjar.server.FjServerToolkit;

public abstract class StoreBlock implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private boolean ready = false;
    private Map<String, Object> data = new HashMap<>();
    
    public boolean ready() {return ready;}
    public Map<String, Object> data() {synchronized(data) {return data;}}
    
    public String path() {
        File dir = new File(FjServerToolkit.getServerConfig("ccu.sb"));
        if (!dir.isDirectory()) dir.mkdirs();
        return FjServerToolkit.getServerConfig("ccu.sb") + getClass().getName().toLowerCase() + ".sb";
    }

    public void load() throws IOException, ClassNotFoundException {
        this.ready = false;
        synchronized(data) {
            FileInputStream fis = new FileInputStream(path());
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object o = ois.readObject();
            ois.close();
            
            this.data = ((StoreBlock) o).data;
        }
        this.ready = true;
    }
    
    public void save() throws IOException {
        synchronized(data) {
            String path_temp = path() + ".tmp";
            FileOutputStream fos = new FileOutputStream(path_temp);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            oos.close();
            
            File dst = new File(path());
            if (dst.isFile()) if (!dst.delete()) throw new IOException("delete file failed: " + dst.getPath());
            if (!new File(path_temp).renameTo(dst)) throw new IOException("move file failed: " + dst.getPath());
        }
    }
    
    public File file() {return new File(path());}
    
}
