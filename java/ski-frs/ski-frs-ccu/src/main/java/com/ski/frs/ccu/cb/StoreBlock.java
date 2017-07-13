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

/**
 * Please remember to deal with data synchronization at business layer among:
 * 
 * <ul>
 * <li>data read or write: {@link #data()}</li>
 * <li>data read: {@link #load()}</li>
 * <li>data write: {@link #save()}</li>
 * </ul>
 * @author fomjar
 */
public abstract class StoreBlock implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Map<String, Object> data = new HashMap<>();
    
    public Map<String, Object> data() {return data;}
    
    public String path() {
        String base = FjServerToolkit.getServerConfig("ccu.sb");
        if (null == base || 0 == base.length()) base = "./";
        
        File dir = new File(base);
        if (!dir.isDirectory()) dir.mkdirs();
        return base + getClass().getName().toLowerCase() + ".sb";
    }

    public void load() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(path());
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object o = ois.readObject();
        ois.close();
        
        this.data = ((StoreBlock) o).data;
    }
    
    public void save() throws IOException {
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
    
    public File file() {return new File(path());}
    
}
