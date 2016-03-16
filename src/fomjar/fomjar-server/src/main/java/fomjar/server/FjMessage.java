package fomjar.server;

/**
 * 消息抽象。所有以fomjar-server库为基础的服务器间通讯消息均以字符串
 * 格式传输数据，各类应用层协议必须转化内容为字符串形式传输。子类需要
 * 实现自己的{@link #toString()}方法，服务器将直接读取此值。
 * 
 * @author fomjar
 */
public interface FjMessage {
    
    /**
     * 消息内容
     * 
     * @return 消息内容的字符串形式
     */
    String toString();
    
}
