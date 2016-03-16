package fomjar.server.msg;

import fomjar.server.FjMessage;
import net.sf.json.JSONObject;

/**
 * JSON格式消息。数据访问参见{@link #json()}方法
 * 
 * @author fomjar
 */
public class FjJsonMessage implements FjMessage {
    
    private JSONObject json;
    
    /**
     * 初始化一个空的JSON消息
     */
    public FjJsonMessage() {this(null);}
    
    /**
     * <p>
     * 依据给定参数初始化一个JSON消息。
     * </p>
     * <p>
     * 如果入参为空，则初始化一个空的JSON消息；
     * 如果入参为{@link JSONObject}类型，则直接使用入参作为消息内容；
     * 如果其它类型的入参，则使用{@link JSONObject}工厂解析。
     * </p>
     * 
     * @param json 给定的json数据对象
     */
    public FjJsonMessage(Object json) {
        if (null == json) this.json = new JSONObject();
        else if (json instanceof JSONObject) this.json = (JSONObject) json;
        else this.json = JSONObject.fromObject(json);
    }
    
    /**
     * 以{@link JSONObject}数据类型来访问JSON消息数据，内容读写实时生效
     * 
     * @return {@link JSONObject}类型的数据
     */
    public JSONObject json() {return json;}
    
    /**
     * 实时生成JSON消息字符串
     */
    @Override
    public String toString() {return json().toString();}
    
}
