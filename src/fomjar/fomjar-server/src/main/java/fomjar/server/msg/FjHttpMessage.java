package fomjar.server.msg;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import fomjar.server.FjMessage;
import net.sf.json.JSONObject;

public abstract class FjHttpMessage implements FjMessage {
    
	public static final String CT_TEXT_CSS		= "text/css";
	public static final String CT_TEXT_HTML  	= "text/html";
    public static final String CT_TEXT_PLAIN  	= "text/plain";
    public static final String CT_TEXT_XML   	= "text/xml";
    public static final String CT_APPL_JSON  	= "application/json";
    public static final String CT_APPL_JS    	= "application/x-javascript";
    
    private Map<String, String> attr;
    private String contentType;
    private String content;
    
    public FjHttpMessage() {this(null, null);}
    
    public FjHttpMessage(String contentType, String content) {
    	this.contentType	= null == contentType ? CT_TEXT_PLAIN : contentType;
        this.content 		= null == content ? "" : content;
        this.attr 			= new HashMap<String, String>();
    }
    
    protected abstract String head();
    
    public Map<String, String> attr() 	{return attr;};
    public int 			contentLength() {return content.getBytes(Charset.forName("utf-8")).length;}
    public String 		contentType() 	{return attr().containsKey("Content-Type") ? attr.get("Content-Type") : contentType;}
    public String       content()       {return content;}
    
    public void			content(String content) {this.content = content;}
    public JSONObject   contentToJson() 		{return new FjJsonMessage(content()).json();}
    public Document     contentToXml()  		{return new FjXmlMessage(content()).xml();}

    @Override
    public String toString() {
    	attr().put("Content-Type", 		contentType());
    	attr().put("Content-Length", 	String.valueOf(contentLength()));
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append(head() + "\r\n");
    	attr().entrySet().forEach(entry->{
    		if (entry.getKey().equals("Content-Type"))
    			sb.append(String.format("%s: %s; charset=UTF-8\r\n", entry.getKey(), entry.getValue()));
    		else
        		sb.append(String.format("%s: %s\r\n", entry.getKey(), entry.getValue()));
    			
    	});
    	sb.append("\r\n");
    	sb.append(content());
    	return sb.toString();
    }
}
