package fomjar.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
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
    
    public static FjConfigMonitor getConfigGuard() {return config_monitor;}
    
    public static class FjAddress {
        
        public String server;
        public String host;
        public int    port;
        
        @Override
        public String toString() {return "[" + server + ":" + host + ":" + port + "]";}
    }
    
    public static class FjSlb {
        
        private Properties address;
        
        public FjSlb(Properties address) {setAddresses(address);}
        
        public void setAddresses(Properties address) {this.address = address;}
        
        public List<FjAddress> getAddresses(String namePrefix) {
            if (null == address || null == namePrefix) return null;
            List<FjAddress> items = new LinkedList<FjAddress>();
            address.forEach((k, v)->{
                if (((String) k).toLowerCase().startsWith(namePrefix.toLowerCase())) {
                    FjAddress item = new FjAddress();
                    item.server = namePrefix;
                    item.host = ((String) v).split(",")[0].trim();
                    item.port = Integer.parseInt(((String) v).split(",")[1].trim());
                    items.add(item);
                }
            });
            return items;
        }
        
        public FjAddress getAddress(String server) {
            List<FjAddress> addresses = getAddresses(server);
            if (null == addresses || 0 == addresses.size()) return null;
            int i = new Random().nextInt() % addresses.size();
            return addresses.get(i);
        }
    }
    
    public static class FjConfigMonitor extends FjLoopTask {
        
        public FjConfigMonitor() {
            long inteval = 10;
            setDelay(inteval * 1000);
            setInterval(inteval * 1000);
        }
        
        @Override
        public void perform() {
            try {PropertyConfigurator.configure("conf/log4j.conf");}
            catch (Exception e) {logger.error("load config failed", e);}
            
            server = loadOneConfig("conf/server.conf");
            
            if (null == slb) slb = new FjSlb(loadOneConfig("conf/address.conf"));
            else slb.setAddresses(loadOneConfig("conf/address.conf"));
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
        new Thread(sender,   name + "-fjsender").start();
        new Thread(server,   name + "-fjserver").start();
        new Thread(receiver, name + "-fjreceiver").start();
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
        sender.close();
        server.close();
        receiver.close();
        logger.error("server: " + name + " stopped");
        return server;
    }
    
    public static FjServer getServer(String name) {return null == g_server ? null : g_server.get(name);}
    
    public static FjSender getSender(String name) {return null == g_sender ? null : g_sender.get(name);}
    
    public static FjMessage createMessage(String data) {
        if (data.startsWith("GET")
                || data.startsWith("POST")
                || data.startsWith("HEAD")) {
            String[] title = data.split("\r\n")[0].split(" ");
            String content = null;
            if (data.contains("\r\n\r\n") && 1 < data.split("\r\n\r\n").length) content = data.split("\r\n\r\n")[1];
            return new FjHttpRequest(title[0], title[1], content);
        }
        if (data.startsWith("HTTP/")) {
            int code = Integer.parseInt(data.split("\r\n")[0].split(" ")[1]);
            String content = data.contains("\r\n\r\n") ? data.split("\r\n\r\n")[1] : null;
            return new FjHttpResponse(code, content);
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
