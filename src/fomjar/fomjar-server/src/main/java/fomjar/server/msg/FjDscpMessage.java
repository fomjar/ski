package fomjar.server.msg;

import java.util.UUID;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * <p>
 * DSCP(分布式服务器通讯协议，Distributed Server Communication Protocol)协议的实现。
 * DSCP主要定义了分布式组网下的服务器间的消息通讯协议基本格式，以简单、通用、扩展为原则，
 * 可用于互联网下的服务器之间的数据交互。
 * </p>
 * <p>
 * 实现基于JSON。
 * </p>
 * <p>
 * DSCP协议的五个关键字段：
 * <li>FS   -- 来源服务器</li>
 * <li>TS   -- 到达服务器</li>
 * <li>SID  -- 会话ID，字符串</li>
 * <li>INST -- 消息指令，整型</li>
 * <li>ARGS -- 指令参数，字符串、JSON对象、JSON数组</li>
 * </p>
 *
 * @author fomjar
 */
public class FjDscpMessage extends FjJsonMessage {

    /**
     * 初始化一个空的DSCP消息
     */
    public FjDscpMessage() {this(null);}

    /**
     * 以JSON数据初始化一个DSCP消息，JSON的数据类型请参见{@link FjJsonMessage}
     *
     * @param json 具体的JSON数据
     *
     * @see FjJsonMessage
     */
    public FjDscpMessage(Object json) {
        super(json);
        if (!json().containsKey("fs"))   json().put("fs",  null);
        if (!json().containsKey("ts"))   json().put("ts",  null);
        if (!json().containsKey("sid"))  json().put("sid", newSid());
        if (!json().containsKey("inst")) json().put("inst", -1);
        if (!json().containsKey("args")) json().put("args", null);
    }

    /** @return DSCP下的FS字段 */
    public String       fs()                {return json().getString("fs");}
    /** @return DSCP下的TS字段 */
    public String       ts()                {return json().getString("ts");}
    /** @return DSCP下的SID字段 */
    public String       sid()               {return json().getString("sid");}
    /** @return DSCP下的INST字段 */
    public int          inst()               {return json().getInt("inst");}
    /** @return DSCP下的ARGS字段 */
    public Object       args()               {return json().get("args");}
    /** @return ARGS字段转JSON格式 */
    public JSON         argsToJson()         {return (JSON) json().get("args");}
    /** @return ARGS字段转JSON对象格式 */
    public JSONObject   argsToJsonObject()   {return json().getJSONObject("args");}
    /** @return ARGS字段转JSON数组格式 */
    public JSONArray    argsToJsonArray()    {return json().getJSONArray("args");}

    /** @return 随机生成SID字段 */
    private static String newSid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
