package fomjar.server;

import java.util.HashMap;
import java.util.Map;

public class FjMessageWrapper {
    
    private FjMessage msg;
    private Map<String, Object> attachment;
    
    public FjMessageWrapper(FjMessage msg) {
        if (null == msg) throw new NullPointerException();
        this.msg = msg;
    }
    
    public FjMessage message() {return msg;}
    
    /**
     * 系统级附件：<br/>
     * <table border=1>
     *   <tr>
     *     <td>key</td>
     *     <td>value</td>
     *     <td>function</td>
     *   </tr>
     *   <tr>
     *     <td>conn</td>
     *     <td>{@link java.nio.channels.SocketChannel}</td>
     *     <td>指定消息发送通道</td>
     *   </tr>
     *   <tr>
     *     <td>observer</td>
     *     <td>{@link fomjar.server.FjSender.FjSenderObserver}</td>
     *     <td>发送过程的观察者</td>
     *   </tr>
     * <table>
     * 
     * @param key
     * @param value
     * @return
     */
    public FjMessageWrapper attach(String key, Object value) {
        if (null == key) throw new NullPointerException();
        
        if (null == attachment) attachment = new HashMap<String, Object>();
        attachment.put(key, value);
        return this;
    }
    
    public Object attachment(String key) {return null == attachment ? null : attachment.get(key);}
    
    public Map<String, Object> attachments() {return attachment;}
    
}