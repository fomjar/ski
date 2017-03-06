package fomjar.server;

import java.util.HashMap;
import java.util.Map;

public class FjMessageWrapper {

    private FjMessage msg;
    private Map<String, Object> attachment;

    public FjMessageWrapper(FjMessage msg) {
        if (null == msg) throw new NullPointerException();

        this.msg = msg;

        attachment = new HashMap<String, Object>();
        attach("timestamp", System.currentTimeMillis());
    }

    public FjMessage message() {return msg;}

    /**
     *
     * @param key
     * @param value
     * @return
     */
    public FjMessageWrapper attach(String key, Object value) {
        if (null == key) throw new NullPointerException();

        attachment.put(key, value);
        return this;
    }

    /**
     * 系统级附件：<br/>
     * <table border=1>
     *   <tr>
     *     <td>key</td>
     *     <td>value</td>
     *     <td>function</td>
     *   </tr>
     *   <tr>
     *     <td>timestamp</td>
     *     <td>long</td>
     *     <td>生成时间戳(单位：毫秒)</td>
     *   </tr>
     *   <tr>
     *     <td>conn</td>
     *     <td>{@link java.nio.channels.SocketChannel}</td>
     *     <td>指定消息发送通道</td>
     *   </tr>
     *   <tr>
     *     <td>raw</td>
     *     <td>{@link String}</td>
     *     <td>接收到的原始字符串</td>
     *   </tr>
     *   <tr>
     *     <td>done</td>
     *     <td>{@link Runnable}</td>
     *     <td>发送完成之后的回调</td>
     *   </tr>
     * <table>
     *
     * @param key
     * @return
     */
    public Object attachment(String key) {return null == attachment ? null : attachment.get(key);}

    public Map<String, Object> attachments() {return attachment;}

}