package fomjar.server.msg;

import fomjar.server.FjMessage;

/**
 * 字符串消息
 * 
 * @author fomjar
 */
public class FjStringMessage implements FjMessage {
    
    private String string;
    
    /**
     * 根据给定字符串初始化一个字符串消息
     * 
     * @param string 给定字符串
     */
    public FjStringMessage(String string) {
        if (null == string) string = "";
        this.string = string;
    }

    @Override
    public String toString() {return string;}

}
