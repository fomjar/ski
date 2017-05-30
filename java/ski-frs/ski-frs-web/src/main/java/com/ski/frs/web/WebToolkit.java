package com.ski.frs.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

public class WebToolkit {
    
    private static final Logger logger = Logger.getLogger(WebToolkit.class);

    public static int getIntFromArgs(JSONObject args, String name) {
        if (!args.has(name)) return -1;

        Object obj = args.get(name);
        if (obj instanceof Integer) return (Integer) obj;
        else return Integer.parseInt(obj.toString(), 16);
    }
    
    public static List<File> collectFile(File dir) {
        List<File> list = new LinkedList<>();
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) list.addAll(collectFile(file));
                else if (file.isFile()) list.add(file);
                else logger.error("unknown file: " + file);
            }
        }
        return list;
    }
    
    public static void collectSomeFile(List<File> list, File dir, int count) {
        if (list.size() >= count) return;
        
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) list.addAll(collectFile(file));
                else if (file.isFile()) list.add(file);
                else logger.error("unknown file: " + file);
                
                if (list.size() >= count) break;
            }
        }
    }
    
    public static String regexField(String string, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) return matcher.group(1);
        else return null;
    }
    
    private static Object lock_move = new Object();
    public static boolean moveFile(File src, File dst) {
        if (!src.isFile()) {
            logger.error("move file failed, no such file, from: " + src.getPath() + ", to: " + dst.getPath());
            return false;
        }
        File dir = dst.getParentFile();
        if (!dir.isDirectory()) {
            synchronized(lock_move) {
                if (!dir.isDirectory()) {
                    if (!dir.mkdirs()) {
                        logger.error("move file failed, create dir failed, from: " + src.getPath() + ", to: " + dst.getPath());
                        return false;
                    }
                }
            }
        }
        if (!src.renameTo(dst)) {
            logger.error("move file failed, from: " + src.getPath() + ", to: " + dst.getPath());
            return false;
        }
        return true;
    }
    
    public static boolean deleteFile(File file) {
        if (!file.exists()) return true;
        if (file.isFile()) return file.delete();
        
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                if (!f.delete()) return false;
            } else if (f.isDirectory()) {
                if (!deleteFile(f)) return false;
            } else {
                logger.error("unknown file: " + f);
            }
        }
        return file.delete();
    }
    
    public static File writeFileBase64Image(String data, String path) throws IOException {
        File file = new File(path);
        File dir = file.getParentFile();
        if (!dir.isDirectory()) if (!dir.mkdirs()) throw new IOException("create directory failed: " + dir.getPath());
        
        if (data.startsWith("data:image")) data = data.substring(data.indexOf("base64,") + 7);
        byte[] data_bytes = Base64.getDecoder().decode(data);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data_bytes));
        ImageIO.write(img, "jpg", file);
        return file;
    }
}
