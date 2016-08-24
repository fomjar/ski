package fomjar.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fomjar.util.FjLoopTask;

public class FjServer extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(FjServer.class);
    private String name;
    private FjMessageQueue mq;
    private Map<String, FjServerTask> tasks;
    
    public FjServer(String name, FjMessageQueue mq) {
        this.name = name;
        this.mq = mq;
        tasks = new LinkedHashMap<String, FjServerTask>();
    }
    
    public String name() {return name;}
    
    public FjMessageQueue mq() {return mq;}
    
    public FjServerTask addServerTask(FjServerTask task) {
        if (null == task) throw new NullPointerException();
        synchronized (tasks) {
            FjServerTask old = tasks.put(task.getClass().getName(), task);
            
            if (null != old) {
                try {old.destroy(this);}
                catch (Exception e) {logger.error("destroy task failed", e);}
            }
            try {task.initialize(this);}
            catch (Exception e) {logger.error("initialize task failed", e);}
            
            return old;
        }
    }

    @Override
    public void perform() {
        FjMessageWrapper wrapper = null;
        while (null == (wrapper = mq.poll()));
        
        synchronized (tasks) {
            for (FjServerTask task : tasks.values()) {
                try {task.onMessage(this, wrapper);}
                catch (Exception e) {logger.error("error occurs on message: " + wrapper.message(), e);}
            }
        }
        
        try {
            SocketChannel conn = (SocketChannel) wrapper.attachment("conn");
            if (null != conn) conn.close();
        } catch (IOException e) {logger.warn("error occurs when close connection for message: " + wrapper.message());}
    }
    
    public static interface FjServerTask {
        void initialize    (FjServer server);
        void destroy    (FjServer server);
        void onMessage    (FjServer server, FjMessageWrapper wrapper);
    }

}
