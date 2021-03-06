package fomjar.util;

import org.apache.log4j.Logger;

/**
 * 循环任务基础支持，支持精确的启动延时和间隔定时
 *
 * @author fomjar
 */
public abstract class FjLoopTask implements FjTask {

    private static final Logger logger = Logger.getLogger(FjLoopTask.class);

    private long delay;
    private long interval;
    private boolean isRun;
    private Thread thread;

    /**
     * 初始化一个循环任务，其启动延时和间隔定时均为0
     */
    public FjLoopTask() {this(0L, 0L);}

    /**
     * 根据给定的间隔定时初始化一个循环任务
     *
     * @param interval 间隔定时（单位毫秒）
     */
    public FjLoopTask(long interval) {this(0L, interval);}

    /**
     * 根据给定的启动延时和间隔定时初始化一个循环任务
     *
     * @param delay    启动延时（单位毫秒）
     * @param interval 间隔定时（单位毫秒）
     */
    public FjLoopTask(long delay, long interval) {
        this.delay = delay;
        this.interval = interval;
        isRun = false;
    }

    /**
     * @return milliseconds 启动延时（单位毫秒）
     */
    public long getDelay() {return delay;}

    /**
     * @param delay 启动延时（单位毫秒）
     */
    public void setDelay(long delay) {this.delay = delay;}

    /**
     * @return 间隔定时（单位毫秒）
     */
    public long getInterval() {return interval;}

    /**
     * @param millisecond 间隔定时（单位毫秒）
     */
    public void setInterval(long millisecond) {this.interval = millisecond;}

    /**
     * @return 循环任务的运行状态
     */
    public boolean isRun() {return isRun;}

    /**
     * 关闭并推出此循环任务
     */
    public void close() {
        // must sleep here, or fail, don't know why
        try {Thread.sleep(10L);}
        catch (InterruptedException e) {e.printStackTrace();}
        isRun = false;
        if (null != thread) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public void run() {
        isRun = true;
        thread = Thread.currentThread();

        try {Thread.sleep(getDelay());}
        catch (InterruptedException e) {e.printStackTrace();}

        while(isRun) {
            long start = System.currentTimeMillis();

            try {perform();}
            catch (Exception e) {logger.error("perform failed", e);}

            long end = System.currentTimeMillis();
            long delta = end - start;
            if (delta >= getInterval()) Thread.yield();
            else {
                try {Thread.sleep(getInterval() - delta);}
                catch (InterruptedException e) {e.printStackTrace();}
            }
        }
    }
}
