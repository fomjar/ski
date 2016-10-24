package fomjar.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class FjThreadFactory implements ThreadFactory {
    private static Map<String, AtomicInteger> poolNumber = new HashMap<String, AtomicInteger>();

    private ThreadGroup group;
    private AtomicInteger threadNumber = new AtomicInteger(1);
    private String namePrefix;

    public FjThreadFactory(String prefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();

        if (!poolNumber.containsKey(prefix)) poolNumber.put(prefix, new AtomicInteger(1));
        namePrefix = String.format("%s-pool-%d-thread-", prefix, poolNumber.get(prefix).getAndIncrement());
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
