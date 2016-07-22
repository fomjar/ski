package com.fomjar.widget;

/**
 * 组件的守护接口，可以开启和关闭守护线程
 * 
 * @author fomja
 */
public interface FjDaemon {

    void openDaemon();
    
    void closeDaemon();
    
}
