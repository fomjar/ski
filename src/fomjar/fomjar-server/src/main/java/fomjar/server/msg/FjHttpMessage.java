package fomjar.server.msg;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    
    private Map<String, String>	attr;
    private Map<String, Map<String, String>>	setcookie;
    private String	contentType;
    private String	content;
    
    public FjHttpMessage() {this(null, null);}
    
    public FjHttpMessage(String contentType, String content) {
    	this.contentType	= null == contentType ? CT_TEXT_PLAIN : contentType;
        this.content 		= null == content ? "" : content;
        this.attr 			= new HashMap<String, String>();
        this.setcookie		= new HashMap<String, Map<String, String>>();
    }
    
    protected abstract String head();
    
    public Map<String, String> attr() 	{return attr;};
    public int 			contentLength() {return content.getBytes(Charset.forName("utf-8")).length;}
    public String 		contentType() 	{return attr().containsKey("Content-Type") ? attr.get("Content-Type") : contentType;}
    public String       content()       {return content;}
    
    public void			content(String content) {this.content = content;}
    public JSONObject   contentToJson() 		{return new FjJsonMessage(content()).json();}
    public Document     contentToXml()  		{return new FjXmlMessage(content()).xml();}
    
    public Map<String, String> cookie() {
    	Map<String, String> cookie = new HashMap<String, String>();
    	
    	if (attr().containsKey("Cookie")) {
	    	String cookie_string = attr().get("Cookie");
	    	if (0 < cookie_string.length()) {
	    		for (String c : cookie_string.split(";")) {
	    			if (0 < c.trim().length() && c.contains("=")) {
		    			String k = c.split("=")[0].trim();
	    				
		    			String v = c.substring(c.indexOf("=") + 1).trim();
	    				try {v = URLDecoder.decode(v, "utf-8");}
	    				catch (UnsupportedEncodingException e) {e.printStackTrace();}
	    				
		    			cookie.put(k, v);
	    			}
	    		}
	    	}
    	}
    	return cookie;
    }
    
    public void setcookie(String key, String val) {
    	if (setcookie.containsKey(key)) setcookie.remove(key);
    	
    	Map<String, String> cookie = new HashMap<String, String>();
		try {val = URLEncoder.encode(val, "utf-8");}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		
    	cookie.put(key, val);
    	setcookie.put(key, cookie);
    }

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
    	setcookie.values().forEach(cookie->{
    		sb.append(String.format("Set-Cookie: %s\r\n",
    				cookie.entrySet()
    						.stream()
    						.map(entry->String.format("%s=%s", entry.getKey(), entry.getValue()))
    						.collect(Collectors.joining("; "))
    		));
    	});
    	sb.append("\r\n");
    	sb.append(content());
    	return sb.toString();
    }
}
