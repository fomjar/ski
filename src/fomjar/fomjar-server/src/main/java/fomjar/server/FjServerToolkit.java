package fomjar.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjJsonMessage;
import fomjar.server.msg.FjStringMessage;
import fomjar.server.msg.FjXmlMessage;
import fomjar.util.FjLoopTask;

public class FjServerToolkit {
    
    private static final Logger logger = Logger.getLogger(FjServerToolkit.class);
    private static Properties server = null;
    private static FjSlb slb = null;
    private static FjConfigMonitor config_monitor = null;
    
    public static void startConfigMonitor() {
        if (null == config_monitor) config_monitor = new FjConfigMonitor();
        
        config_monitor.perform();
        if (!config_monitor.isRun()) new Thread(config_monitor, "fjconfig-monitor").start();
    }
    
    private static Properties loadOneConfig(String absolutePath) {
        if (null == absolutePath) return null;
        Properties p = new Properties();
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(absolutePath), "utf-8");
            p.load(isr);
        } catch (IOException e) {logger.error("load config failed from path: " + absolutePath, e);}
        finally {
            try {if (null != isr) isr.close();}
            catch (IOException e) {}
        }
        return p;
    }
    
    public static String getServerConfig(String key) {
        if (null == server || null == key) return null;
        return server.getProperty(key);
    }
    
    public static FjSlb getSlb() {return slb;}
    
    public static FjConfigMonitor getConfigMonitor() {return config_monitor;}
    
    public static class FjAddress {
        
        public String server;
        public String host;
        public int    port;
        
        @Override
        public boolean equals(Object obj) {
            if (null == obj) return false;
            if (!(obj instanceof FjAddress)) return false;
            
            if (this == obj) return true;
            
            FjAddress addr1 = (FjAddress) obj;
            return server.equals(addr1.server)
                    && host.equals(addr1.host)
                    && port == addr1.port;
        }
        
        @Override
        public String toString() {return server + ":" + host + ":" + port;}
    }
    
    public static class FjSlb {
        
        private Properties address;
        private Map<String, Integer> cache;
        
        public FjSlb() {cache = new HashMap<String, Integer>();}
        
        public void setAddresses(Properties address) {this.address = address;}
        
        public List<FjAddress> getAddresses(String server) {
            if (null == address || null == server) return null;
            List<FjAddress> items = new LinkedList<FjAddress>();
            address.forEach((k, v)->{
                if (k.toString().toLowerCase().startsWith(server.toLowerCase())) {
                    FjAddress item = new FjAddress();
                    item.server = server;
                    item.host = v.toString().split(":")[0].trim();
                    item.port = Integer.parseInt(v.toString().split(":")[1].trim());
                    items.add(item);
                }
            });
            return items;
        }
        
        public FjAddress getAddress(String server) {
            List<FjAddress> addresses = getAddresses(server);
            if (null == addresses || addresses.isEmpty()) return null;
            
            if (!cache.containsKey(server)) cache.put(server, -1);
            int last_index = cache.get(server);
            int this_index = last_index + 1;
            if (this_index >= addresses.size()) this_index = 0;
            cache.put(server, this_index);
            
            return addresses.get(this_index);
        }
    }
    
    public static class FjConfigMonitor extends FjLoopTask {
        
        private Runnable task;
        
        public FjConfigMonitor() {
            long inteval = 10;
            setDelay(inteval * 1000);
            setInterval(inteval * 1000);
        }
        
        @Override
        public void perform() {
            try {PropertyConfigurator.configure("conf/log4j.conf");}
            catch (Exception e) {logger.error("load config failed", e);}
            
            if (null == slb) slb = new FjSlb();
            slb.setAddresses(loadOneConfig("conf/address.conf"));
            
            server = loadOneConfig("conf/server.conf");
            
            if (null != task) task.run();
        }
        
        public void onLoad(Runnable task) {
            this.task = task;
        }
    }
    
    private static Map<String, FjServer>   g_server = null;
    private static Map<String, FjReceiver> g_receiver = null;
    private static Map<String, FjSender>   g_sender = null;
    
    /**
     * call {@link #startConfigMonitor()} first, and then ensure got the address of given server 'name'
     * 
     * @param name
     * @return
     */
    public static synchronized FjServer startServer(String name) {
        FjAddress address = getSlb().getAddress(name);
        if (null == address) {
            logger.error("there is no address info for server name: " + name);
            return null;
        }
        FjMessageQueue mq = new FjMessageQueue();
        FjServer server = new FjServer(name, mq);
        FjReceiver receiver = new FjReceiver(mq, address.port);
        FjSender sender = new FjSender();
        new Thread(sender,   "fjsender-" + name).start();
        new Thread(server,   "fjserver-" + name).start();
        new Thread(receiver, "fjreceiver-" + name).start();
        if (null == g_server)   g_server = new HashMap<String, FjServer>();
        if (null == g_sender)   g_sender = new HashMap<String, FjSender>();
        if (null == g_receiver) g_receiver = new HashMap<String, FjReceiver>();
        g_sender.put(name, sender);
        g_server.put(name, server);
        g_receiver.put(name, receiver);
        logger.error("server: " + name + " started on address: " + address);
        return server;
    }
    
    public static synchronized FjServer stopServer(String name) {
        if (null == g_server || !g_server.containsKey(name)) {
            logger.error("no server started with name: " + name);
            return null;
        }
        
        FjSender sender     = g_sender.get(name);
        FjServer server     = g_server.get(name);
        FjReceiver receiver = g_receiver.get(name);
        if (null != sender) sender.close();
        if (null != server) server.close();
        if (null != receiver) receiver.close();
        logger.error("server: " + name + " stopped");
        return server;
    }
    
    public static FjServer getServer(String name) {return null == g_server ? null : g_server.get(name);}
    
    public static FjSender getSender(String name) {return null == g_sender ? null : g_sender.get(name);}
    
    public static FjSender getAnySender() {
        if (null == g_sender) return null;
        int index = Math.abs(new Random().nextInt()) % g_sender.size();
        Iterator<FjSender> iterator = g_sender.values().iterator();
        for (int i = 0; i < index; i++) iterator.next();
        return iterator.next();
    }
    
    public static FjMessage createMessage(String data) {
        if (data.startsWith("GET")
                || data.startsWith("POST")
                || data.startsWith("HEAD")) {
            return FjHttpRequest.parse(data);
        }
        if (data.startsWith("HTTP/")) {
            return FjHttpResponse.parse(data);
        }
        if (data.startsWith("{")) {
            FjJsonMessage jmsg = new FjJsonMessage(data);
            if (jmsg.json().containsKey("fs")
                    && jmsg.json().containsKey("ts")
                    && jmsg.json().containsKey("sid")
                    && jmsg.json().containsKey("inst")
                    && jmsg.json().containsKey("args"))
                 return new FjDscpMessage(data);
            else return jmsg;
        }
        if (data.startsWith("<")) return new FjXmlMessage(data);
        return new FjStringMessage(data);
    }

}