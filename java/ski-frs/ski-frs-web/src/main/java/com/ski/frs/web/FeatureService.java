package com.ski.frs.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class FeatureService {
    
    private static final Logger logger = Logger.getLogger(FeatureService.class);
    
    public static final int DEVICE_GPU = 0;
    public static final int DEVICE_CPU = 1;
    
    /** 成功 */
    public static final int SUCCESS             = 0;
    /** 加载分类器失败 */
    public static final int ERROR_LOAD_FILTER   = 1;
    /** 缓存为空 */
    public static final int ERROR_BUFFER_EMPTY  = 2;
    /** 未初始化 */
    public static final int ERROR_NO_INIT       = 3;
    /** 没有对应索引的GPU设备 */
    public static final int ERROR_NO_DEV        = 4;
    /** GPU设备已经用光，能力不足 */
    public static final int ERROR_DISABLE       = 5;
    /** 没有人脸 */
    public static final int ERROR_NO_FACE       = 6;
    
    private static FeatureService instance = null;
    public static FeatureService getDefault() {
        if (null == instance) instance = new FeatureService();
        return instance;
    }
    
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private FV fv;
    
    public FeatureService() {
        checkRespawn();
    }
    
    private void checkRespawn() {
        if (null != process && process.isAlive()) return;
        
        try {
            process = Runtime.getRuntime().exec("feature_extract.exe");
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            
            while (!read().startsWith("ready"));
        } catch (IOException e) {logger.error("respawn process failed", e);}
    }
    
    private String read() {
        try {return reader.readLine();}
        catch (IOException e) {logger.error("read line failed", e);}
        return null;
    }
    
    private void write(String s) {
        try {
            writer.write(s + "\r\n");
            writer.flush();
        } catch (IOException e) {logger.error("write line failed: " + s, e);}
    }
    
    public void close() {
        write("exit");
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {e.printStackTrace();}
    }

    private void do_fv() {
        String line = null;
        while (!(line = read()).startsWith("mark=")) logger.warn("illegal interface output: " + line);
        
        if (null == fv) {
            logger.error("null fv acceptor");
            return;
        }
        
        int mark = SUCCESS;
        String[] kvs = line.split(",");
        Map<String, Object> data = new HashMap<>();
        for (String kv : kvs) {
            if (SUCCESS != mark) break;
            
            String k = kv.split("=")[0].trim();
            String v = kv.substring(kv.indexOf("=") + 1).trim();
            switch (k) {
            case "mark":
                mark = Integer.parseInt(v);
                if (SUCCESS != mark) logger.error("illegal picture: " + line);
                data.put(k, mark);
                break;
            case "fv":
                String[] fvs = kvs[1].split("=")[1].split(" ");
                double[] fvd = new double[fvs.length];
                for (int i = 0; i < fvs.length; i++) fvd[i] = Double.parseDouble(fvs[i]);
                data.put(k, fvd);
                break;
            case "glass":
            case "mask":
            case "hat":
            case "gender":
            case "nation":
                data.put(k, Integer.parseInt(v));
                break;
            default:
                logger.error("illegal key: " + k);
                break;
            }
        }
        
        fv.fv(data.containsKey("mark")      ? (int) data.get("mark")    : -1,
                data.containsKey("fv")      ? (double[]) data.get("fv") : null,
                data.containsKey("glass")   ? (int) data.get("glass")   : -1,
                data.containsKey("mask")    ? (int) data.get("mask")    : -1,
                data.containsKey("hat")     ? (int) data.get("hat")     : -1,
                data.containsKey("gender")  ? (int) data.get("gender")  : -1,
                data.containsKey("nation")  ? (int) data.get("nation")  : -1);
    }
    
    public void fv_path(FV fv, String... paths) {
        synchronized(process) {
            checkRespawn();
            
            this.fv = fv;
            for (String path : paths) {
                write("path");
                write(path);
                do_fv();
            }
        }
    }
    
    public void fv_base64(FV fv, String... base64s) {
        synchronized(process) {
            checkRespawn();
            
            this.fv = fv;
            for (String base64 : base64s) {
                if (base64.startsWith("data:image")) base64 = base64.substring(base64.indexOf("base64,") + 7);
                write("base64");
                write(base64);
                do_fv();
            }
        }
    }
    
    public static interface FV {void fv(int mark, double[] fv, int glass, int mask, int hat, int gender, int nation);}

}
