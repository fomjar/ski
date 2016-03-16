package fomjar.server.msg;

import java.nio.charset.Charset;

import org.w3c.dom.Document;

import fomjar.server.FjMessage;
import net.sf.json.JSONObject;

public class FjHttpMessage implements FjMessage {
    
    private String content;
    
    public FjHttpMessage() {this(null);}
    
    public FjHttpMessage(String content) {
        if (null == content) content = "";
        this.content = content;
    }
    
    public int contentLength() {return content.getBytes(Charset.forName("utf-8")).length;}
    
    public String contentType() {
        if (content.startsWith("<html>") || content.startsWith("<HTML>")) return "text/html";
        if (content.startsWith("<")) return "text/xml";
        if (content.startsWith("{")) return "application/json";
        return "text/plain";
    }
    
    public String content() {return content;}
    
    public JSONObject contentToJson() {return new FjJsonMessage(content()).json();}
    
    public Document   contentToXml() {return new FjXmlMessage(content()).xml();}

    @Override
    public String toString() {return content();}
}
