package com.ski.frs.web.mon;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;

import fomjar.server.FjServerToolkit;
import fomjar.util.FjLoopTask;
import net.sf.json.JSONObject;

public class PictureMonitor extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(PictureMonitor.class);
    private static final int BUF_LEN = 1024 * 8;
    
    private byte[] buffer;
    
    public PictureMonitor() {
        setDelay(1000L * 20);
        buffer = new byte[BUF_LEN];
    }

    @Override
    public void perform() {
        logger.debug("monitor begin");
        scanDirectory(new File(FjServerToolkit.getServerConfig("web.pic.src")));
        logger.debug("monitor end");
    }
    
    public void open() {
        if (isRun()) {
            logger.warn("monitor already opened");
            return;
        }
        
        new Thread(this, "monitor-picture").start();
    }
    
    private void scanDirectory(File dir) {
        logger.error(dir.getPath());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) scanDirectory(file);
            else if (file.isFile()) scanFile(file);
            else logger.error("unknown file: " + file);
        }
        if (0 == dir.listFiles().length) dir.delete();
    }
    
    private void scanFile(File file) {
        String name = file.getName();
        if (!name.toLowerCase().endsWith(".jpg")) return;
        
        logger.info("scan file: " + file.getPath());
        if (name.startsWith("20")) updatePic(file, ISIS.FIELD_PIC_SIZE_LARGE);
        else if (name.startsWith("F")) updatePic(file, ISIS.FIELD_PIC_SIZE_SMALL);
        else if (name.startsWith("P")) updatePic(file, ISIS.FIELD_PIC_SIZE_MIDDLE);
        else logger.error("unknown picture file: " + file);
        
        checkWait();
    }
    
    private void updatePic(File file, int size) {
        String[] fields = file.getName().split("\\.")[0].split("_");
        JSONObject args = new JSONObject();
        args.put("did",  fields[1]);
        args.put("name", file.getName());
        String time = fields[0].substring(fields[0].indexOf("20"));
        args.put("time", time.substring(0, time.length() - 3));
        args.put("size", size);
        args.put("type", ISIS.FIELD_PIC_TYPE_MAN);
        File xml = new File(file.getPath().replace(".jpg", ".xml"));
        if (xml.isFile()) {
            try {args.put("desc1", readVector(xml));}
            catch (IOException e) {
                logger.error("read vector failed: " + xml, e);
                return;
            }
            if (!moveToDst(xml)) return;
        }
        FjServerToolkit.dscpRequest("web", ISIS.INST_UPDATE_PIC, args);
        moveToDst(file);
    }
    
    private String readFileAsString(File file) throws IOException {
        return new String(readFile(file), "utf-8");
    }
    
    private String readVector(File file) throws IOException {
        String xml = readFileAsString(file);
        String vector = xml.substring(xml.indexOf("<data>") + 6, xml.indexOf("</data>"));
        if (vector.contains("\r\n")) vector = vector.replace("\r\n", "");
        if (vector.contains("\r")) vector = vector.replace("\r", "");
        if (vector.contains("\n")) vector = vector.replace("\n", "");
        while (vector.contains("  ")) vector = vector.replace("  ", " ");
        vector = vector.trim();
        return vector;
    }
    
    private static boolean moveToDst(File src) {
        File dst = new File("document"
                + FjServerToolkit.getServerConfig("web.pic.dst")
                + "/"
                + src.getName());
        File par = dst.getParentFile();
        if (!par.isDirectory()) {
            if (!par.mkdirs()) {
                logger.error("create dst dir failed: " + par);
                return false;
            }
        }
        if (!src.renameTo(dst)) {
            logger.error("move file to dst failed: " + src);
            return false;
        } else {
            logger.info("move file to dst success: " + src);
            return true;
        }
    }
    
    private byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = 0;
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        while (-1 < (len = bis.read(buffer))) baos.write(buffer, 0, len);
        bis.close();
        return baos.toByteArray();
    }
    
    private static void checkWait() {
        while (FjServerToolkit.getAnySender().mq().size() >= Integer.parseInt(FjServerToolkit.getServerConfig("web.pic.que"))) {
            try {Thread.sleep(100L);}
            catch (InterruptedException e) {logger.warn("check and wait mq failed", e);}
        }
    }

}
