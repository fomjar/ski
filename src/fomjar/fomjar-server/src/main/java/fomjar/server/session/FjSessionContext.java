package fomjar.server.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fomjar.server.FjMessage;
import fomjar.server.FjServer;
import fomjar.server.msg.FjDscpMessage;

/**
 * <p>
 * 会话控制块（Session Control Block），用于记录会话状态
 * </p>
 * <p>
 * 系统缓存清单：
 * <table border="1px">
 * <tr><td>键</td><td>值</td><td>类型</td></tr>
 * <tr><td>sid</td><td>会话ID</td><td>{@link String}</td></tr>
 * <tr><td>ssn</td><td>当前会话序列号</td><td>{@link int}</td></tr>
 * <tr><td>server</td><td>服务器名称</td><td>{@link String}</td></tr>
 * <tr><td>msg.ssn</td><td>对应ssn的消息</td><td>{@link FjMessage}</td></tr>
 * <tr><td>time.open</td><td>会话打开时间（单位毫秒）</td><td>{@link long}</td></tr>
 * <tr><td>time.close</td><td>会话关闭时间（单位毫秒）</td><td>{@link long}</td></tr>
 * <tr><td>business.type</td><td>会话的业务类型，取值为某些指令</td><td>{@link int}</td></tr>
 * </table>
 * </p>
 * 
 * @author fomjar
 */
public class FjSessionContext {
    
    private Map<String, Object> data;
    
    FjSessionContext(String sid) {
        data = new HashMap<String, Object>();
        data.put("sid", sid);
        data.put("ssn", -1);
    }
    
    /** 获取会话的所有缓存 */
    public Map<String, Object> getAll()    {return data;}
    /** 根据索引获取一个缓存对象 */
    public Object  get        (String key) {return data.get(key);}
    /** 根据索引获取一个缓存字符串 */
    public String  getString  (String key) {return (String)  data.get(key);}
    /** 根据索引获取一个缓存boolean值 */
    public boolean getBoolean (String key) {return (boolean) data.get(key);}
    /** 根据索引获取一个缓存字节 */
    public byte    getByte    (String key) {return (byte)    data.get(key);}
    /** 根据索引获取一个缓存字符 */
    public char    getChar    (String key) {return (char)    data.get(key);}
    /** 根据索引获取一个缓存短整数 */
    public short   getShort   (String key) {return (short)   data.get(key);}
    /** 根据索引获取一个缓存整数 */
    public int     getInteger (String key) {return (int)     data.get(key);}
    /** 根据索引获取一个缓存长整数 */
    public long    getLong    (String key) {return (long)    data.get(key);}
    /** 根据索引获取一个缓存浮点数 */
    public float   getFloat   (String key) {return (float)   data.get(key);}
    /** 根据索引获取一个缓存双精度浮点数 */
    public double  getDouble  (String key) {return (double)  data.get(key);}
    /** 判断给定索引的缓存是否存在*/
    public boolean has        (String key) {return data.containsKey(key);}
    /** 删除给定索引的缓存 */
    public Object  remove     (String key) {return data.remove(key);}
    /** 存入一个键值对到缓存中 */
    public FjSessionContext   put        (String key, Object value) {data.put(key, value); return this;}

    
    /** @return 该会话的会话ID */
    public String  sid()    {return getString("sid");}
    /**
     * 随着同一个会话中涉及的消息数增多，序列号也会逐渐增加。正常情况下，序列号初始为-1，每收到一个消息，序列号+1
     * 
     * @return 该会话的序列号
     */
    public int     ssn()    {return getInteger("ssn");}
    public String  server() {return getString("server");}
    
    void prepare(FjServer server, FjMessage msg) {
        put("server", server.name());
        
        put("ssn", ssn() + 1);
        put("msg." + ssn(), msg);
    }
    
    /** 根据会话序列号获取其对应的消息 */
    public FjDscpMessage msg(int ssn) {return (FjDscpMessage) get("msg." + ssn);}
    /** 获取会话中缓存的所有收到的消息，按照会话序列号排序返回 */
    public List<FjDscpMessage> msgs() {
        return data.entrySet().stream()
                .filter ((entry)->         {return entry.getKey().startsWith("msg.");})
                .sorted ((entry1, entry2)->{return entry1.getKey().compareTo(entry2.getKey());})
                .map    ((entry)->         {return (FjDscpMessage) entry.getValue();})
                .collect(Collectors.toList());
    }
}