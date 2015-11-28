package fomjar.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class FjToolkit {
	
	private static final Logger logger = Logger.getLogger(FjToolkit.class);
	private static Properties server = null;
	private static Properties address = null;
	
	public static void loadConfig() {
		PropertyConfigurator.configure("conf/log4j.conf");
		server  = loadOneConfig("conf/server.conf");
		address = loadOneConfig("conf/address.conf");
	}
	
	private static Properties loadOneConfig(String absolutePath) {
		if (null == absolutePath) return null;
		Properties p = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(absolutePath);
			p.load(fis);
			logger.info("load config success! config content: " + p);
		} catch (IOException e) {logger.error("load config failed from path: " + absolutePath, e);}
		finally {
			try {if (null != fis) fis.close();}
			catch (IOException e) {e.printStackTrace();}
		}
		return p;
	}
	
	public static String getServerConfig(String key) {
		if (null == server || null == key) return null;
		return server.getProperty(key);
	}
	
	public static List<FjAddress> getAddresses(String namePrefix) {
		if (null == address || null == namePrefix) return null;
		Iterator<Object> i = address.keySet().iterator();
		List<FjAddress> items = new LinkedList<FjAddress>();
		while (i.hasNext()) {
			String k = (String) i.next();
			if (k.toLowerCase().startsWith(namePrefix.toLowerCase())) {
				String v = address.getProperty(k);
				if (!v.contains(",")) continue;
				FjAddress item = new FjAddress();
				item.moduleCategory = namePrefix;
				item.host = v.split(",")[0].trim();
				item.port = Integer.parseInt(v.split(",")[1].trim());
				items.add(item);
			}
		}
		return items;
	}
	
	public static FjAddress getAddress(String moduleCategory) {
		List<FjAddress> addresses = getAddresses(moduleCategory);
		if (null == addresses || 0 == addresses.size()) return null;
		int i = new Random().nextInt() % addresses.size();
		return addresses.get(i);
	}
	
	public static class FjAddress {
		
		public String moduleCategory;
		public String host;
		public int port;
		
		@Override
		public String toString() {
			return "[" + moduleCategory + ":" + host + ":" + port + "]";
		}
	}
	
	private static ExecutorService g_pool = null;
	private static Map<String, FjServer>   g_server = null;
	private static Map<String, FjReceiver> g_receiver = null;
	private static Map<String, FjSender>   g_sender = null;
	
	/**
	 * call {@link #loadConfig()} first, and then ensure got the address of given server 'name'
	 * 
	 * @param name
	 * @return
	 */
	public static synchronized FjServer startServer(String name) {
		FjAddress address = getAddress(name);
		if (null == address) {
			logger.error("there is no address info for server name: " + name);
			return null;
		}
		FjMq mq = new FjMq();
		FjServer server = new FjServer(name, mq);
		FjReceiver receiver = new FjReceiver(mq, address.port);
		FjSender sender = new FjSender();
		if (null == g_pool) g_pool = Executors.newCachedThreadPool();
		g_pool.submit(sender);
		g_pool.submit(server);
		g_pool.submit(receiver);
		if (null == g_server) g_server = new HashMap<String, FjServer>();
		if (null == g_receiver) g_receiver = new HashMap<String, FjReceiver>();
		if (null == g_sender) g_sender = new HashMap<String, FjSender>();
		g_server.put(name, server);
		g_receiver.put(name, receiver);
		g_sender.put(name, sender);
		logger.error("server: " + name + " started on address: " + address);
		return server;
	}
	
	public static synchronized FjServer stopServer(String name) {
		if (null == g_server || !g_server.containsKey(name)) {
			logger.error("no server started with name: " + name);
			return null;
		}
		
		FjServer server = g_server.get(name);
		FjReceiver receiver = g_receiver.get(name);
		FjSender sender = g_sender.get(name);
		server.close();
		receiver.close();
		sender.close();
		logger.error("server: " + name + " stopped");
		return server;
	}
	
	public static FjServer getServer(String name) {
		if (null == g_server) return null;
		return g_server.get(name);
	}
	
	public static FjSender getSender(String name) {
		if (null == g_sender) return null;
		return g_sender.get(name);
	}
}
