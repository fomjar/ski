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

import fomjar.util.FjLog;
import fomjar.util.FjLoopTask;

public class FjServerToolkit {
	
	private static final Logger logger = Logger.getLogger(FjServerToolkit.class);
	private static Properties    server = null;
	private static FjSlb         slb = null;
	private static FjConfigGuard guard = null;
	
	public static void startConfigGuard() {
		if (null == guard) {
			guard = new FjConfigGuard();
		}
		long inteval = 10;
		guard.setDelay(inteval * 1000);
		guard.setInterval(inteval * 1000L);
		guard.perform();
		if (!guard.isRun()) new Thread(guard, "fjserver-config-guard").start();
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
			catch (IOException e) {e.printStackTrace();}
		}
		return p;
	}
	
	public static String getServerConfig(String key) {
		if (null == server || null == key) return null;
		return server.getProperty(key);
	}
	
	public static FjSlb getSlb() {
		return slb;
	}
	
	public static FjConfigGuard getConfigGuard() {
		return guard;
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
	
	public static class FjSlb {
		
		private Properties address;
		
		public FjSlb(Properties address) {
			setAddresses(address);
		}
		
		public void setAddresses(Properties address) {
			this.address = address;
		}
		
		public List<FjAddress> getAddresses(String namePrefix) {
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
		
		public FjAddress getAddress(String moduleCategory) {
			List<FjAddress> addresses = getAddresses(moduleCategory);
			if (null == addresses || 0 == addresses.size()) return null;
			int i = new Random().nextInt() % addresses.size();
			return addresses.get(i);
		}
	}
	
	public static class FjConfigGuard extends FjLoopTask {
		@Override
		public void perform() {
			FjLog.loadLog();
			server = loadOneConfig("conf/server.conf");
			if (null == slb) slb = new FjSlb(loadOneConfig("conf/address.conf"));
			else slb.setAddresses(loadOneConfig("conf/address.conf"));
		}
	}
	
	private static Map<String, FjServer>   g_server = null;
	private static Map<String, FjReceiver> g_receiver = null;
	private static Map<String, FjSender>   g_sender = null;
	
	/**
	 * call {@link #startConfigGuard()} first, and then ensure got the address of given server 'name'
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
		FjMq mq = new FjMq();
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
		sender.close();
		server.close();
		receiver.close();
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
	
	private static final Random random = new Random();
	public static String newSid(String serverName) {
		return Integer.toHexString(serverName.hashCode())
				+ Integer.toHexString(Long.toHexString(System.currentTimeMillis()).hashCode())
				+ Integer.toHexString(String.valueOf(random.nextInt()).hashCode());
	}
	
	public static boolean isLegalMsg(FjMsg msg) {
		if (null == msg)                                  return false;
		if (!(msg instanceof FjJsonMsg))                  return false;
		if (!((FjJsonMsg) msg).json().containsKey("fs"))  return false;
		if (!((FjJsonMsg) msg).json().containsKey("ts"))  return false;
		if (!((FjJsonMsg) msg).json().containsKey("sid")) return false;
		return true;
	}
	
	public static boolean isLegalRequest(FjMsg msg) {
		if (!isLegalMsg(msg)) return false;
		if (!((FjJsonMsg) msg).json().containsKey("cmd")) return false;
		if (!((FjJsonMsg) msg).json().containsKey("arg")) return false;
		return true;
	}

	public static boolean isLegalResponse(FjMsg msg) {
		if (!isLegalMsg(msg)) return false;
		if (!((FjJsonMsg) msg).json().containsKey("code")) return false;
		if (!((FjJsonMsg) msg).json().containsKey("desc")) return false;
		return true;
	}
}
