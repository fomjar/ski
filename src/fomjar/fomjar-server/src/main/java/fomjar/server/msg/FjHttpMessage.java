package fomjar.server.msg;

import java.nio.charset.Charset;

import org.w3c.dom.Document;

import fomjar.server.FjMessage;
import net.sf.json.JSONObject;

public class FjHttpMessage implements FjMessage {
    
    public static final String CT_TEXT  = "text/plain";
    public static final String CT_JSON  = "application/json";
    public static final String CT_HTML  = "text/html";
    public static final String CT_XML   = "text/xml";
    public static final String CT_CSS   = "text/css";
    public static final String CT_JS    = "application/x-javascript";
    
    private String contentType;
    private String content;
    
    public FjHttpMessage() {this(null, null);}
    
    public FjHttpMessage(String contentType, String content) {
        this.contentType = contentType;
        this.content = content;
    }
    
    public int contentLength() {
        if (null == content) return 0;
        
        return content.getBytes(Charset.forName("utf-8")).length;
    }
    
    public String contentType() {
        if (null == contentType) return "text/plain";
        return contentType;
    }
    
    public String       content()       {return content;}
    
    public JSONObject   contentToJson() {return new FjJsonMessage(content()).json();}
    
    public Document     contentToXml()  {return new FjXmlMessage(content()).xml();}

    @Override
    public String toString() {return content();}
}
