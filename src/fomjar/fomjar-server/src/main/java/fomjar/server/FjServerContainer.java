package fomjar.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import fomjar.util.FjLoopTask;

public class FjServerContainer extends FjLoopTask {
    
    private static final Logger logger = Logger.getLogger(FjServerContainer.class);
    
    private static FjServerContainer instance = null;
    public synchronized static FjServerContainer getInstance() {
        if (null == instance) instance = new FjServerContainer();
        return instance;
    }
    
    private Map<String, Set<String>>     servers;
    private Map<String, Long>             modified;
    
    private FjServerContainer() {
        servers = new HashMap<String, Set<String>>();
        modified = new HashMap<String, Long>();
        
        Long time = 10 * 1000L;
        setInterval(time);
    }
    
    public void register(String server, String taskpath) {
        synchronized (servers) {
            if (!servers.containsKey(server)) servers.put(server, new LinkedHashSet<String>());
            servers.get(server).add(taskpath);
            
            if (!isRun()) new Thread(this, "fjserver-container").start();
        }
    }
    
    public void deregister(String server, String taskpath) {
        synchronized (servers) {
            if (!servers.containsKey(server)) return;
            servers.get(server).remove(taskpath);
            
            if (isRun()) {
                for (Set<String> paths : servers.values()) {
                    if (!paths.isEmpty()) return;
                }
                close();
            }
        }
    }
    
    @Override
    public void perform() {
        synchronized (servers) {
            servers.forEach((s, p)->{
                FjServer server = FjServerToolkit.getServer(s);
                p
                        .stream()
                        .map(path->new File(path))
                        .filter(file->file.isDirectory())
                        .forEach(dir->{
                            for (File file : dir.listFiles()) {
                                if (!file.isFile()) continue;
                                if (!file.getName().toLowerCase().endsWith(".jar")
                                        && !file.getName().toLowerCase().endsWith(".zip")) continue;
                                
                                if (!modified.containsKey(file.getName())) modified.put(file.getName(), 0L);
                                if (modified.get(file.getName()) >= file.lastModified()) return;
                                
                                try {
                                    ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
                                    ZipEntry ze = null;
                                    List<String> classes = new LinkedList<String>();
                                    while (null != (ze = zis.getNextEntry())) {
                                        if (ze.isDirectory()) continue;
                                        
                                        if (ze.getName().toLowerCase().endsWith(".class"))
                                            classes.add(ze.getName().replace("/", ".").substring(0, ze.getName().length() - 6));
                                    }
                                    
                                    URLClassLoader loader = new URLClassLoader(new URL[] {file.toURI().toURL()});
                                    for (String classname : classes) {
                                        Class<?> clazz = loader.loadClass(classname);
                                        if (!FjServer.FjServerTask.class.isAssignableFrom(clazz)) continue;
                                        
                                        FjServer.FjServerTask task = (FjServer.FjServerTask) clazz.newInstance();
                                        server.addServerTask(task);
                                        logger.info("update task success: " + task.getClass().getName());
                                    }
                                    loader.close();
                                    zis.close();
                                    modified.put(file.getName(), file.lastModified());
                                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | IOException e) {
                                    logger.error("load task failed for file: " + file.getName(), e);
                                }
                            }
                        });
            });
        }
    }
    
}
