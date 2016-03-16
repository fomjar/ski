package fomjar.util;

public interface FjTask extends Runnable {
    
    /**
     * 具体执行的任务内容
     */
    void perform();
    
}
