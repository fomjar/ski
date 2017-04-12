package fomjar.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fomjar.server.msg.FjDscpMessage;
import fomjar.util.FjLoopTask;

public class FjServer extends FjLoopTask {

    private static final Logger logger = Logger.getLogger(FjServer.class);
    private String name;
    private FjMessageQueue mq;
    private Map<String, FjServerTask> tasks;
    private Map<String, FjServerTask> dscps;

    public FjServer(String name, FjMessageQueue mq) {
        this.name = name;
        this.mq = mq;
        tasks = new LinkedHashMap<String, FjServerTask>();
        dscps = new HashMap<String, FjServerTask>();
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
    
    public void onDscpSession(String sid, FjServerTask task) {
        if (null == sid || null == task) throw new NullPointerException();
        dscps.put(sid, task);
        
        try {task.initialize(this);}
        catch (Exception e) {logger.error("initialize task failed", e);}
    }

    @Override
    public void perform() {
        FjMessageWrapper wrapper = null;
        while (null == (wrapper = mq.poll()));
        
        boolean is_dscp_session = false;
        
        if (wrapper.message() instanceof FjDscpMessage) {
            FjDscpMessage dmsg = (FjDscpMessage) wrapper.message();
            if (dscps.containsKey(dmsg.sid())) {
                is_dscp_session = true;
                FjServerTask task = dscps.remove(dmsg.sid());
                
                try {task.onMessage(this, wrapper);}
                catch (Exception e) {logger.error("error occurs on session message: " + wrapper.message(), e);}
                
                try {task.destroy(this);}
                catch (Exception e) {logger.error("destroy task failed", e);}
            }
        }

        if (!is_dscp_session) {
            synchronized (tasks) {
                for (FjServerTask task : tasks.values()) {
                    try {task.onMessage(this, wrapper);}
                    catch (Exception e) {logger.error("error occurs on message: " + wrapper.message(), e);}
                }
            }
        }

        try {
            SocketChannel conn = (SocketChannel) wrapper.attachment("conn");
            if (null != conn) conn.close();
        } catch (IOException e) {logger.warn("error occurs when close connection for message: " + wrapper.message());}
    }

    public static interface FjServerTask {
        void initialize (FjServer server);
        void destroy    (FjServer server);
        void onMessage  (FjServer server, FjMessageWrapper wrapper);
    }

}
