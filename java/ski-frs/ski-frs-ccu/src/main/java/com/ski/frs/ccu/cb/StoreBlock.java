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

import fomjar.server.FjServerToolkit;

public abstract class StoreBlock implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String path() {
        return FjServerToolkit.getServerConfig("ccu.sb") + getClass().getName().toLowerCase() + ".sb";
    }

    public StoreBlock load() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(path());
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object o = ois.readObject();
        ois.close();
        return (StoreBlock) o;
    }
    
    public boolean save() throws IOException {
        String path_temp = path() + ".tmp";
        FileOutputStream fos = new FileOutputStream(path_temp);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();
        oos.close();
        File dst = new File(path());
        if (dst.isFile()) if (!dst.delete()) return false;
        return new File(path_temp).renameTo(dst);
    }
    
    public File file() {return new File(path());}
    
}
