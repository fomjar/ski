package fomjar.server.msg;

import java.util.HashMap;
import java.util.Map;


public class FjHttpRequest extends FjHttpMessage {
    
    private String method;
    private String url;
    
    public FjHttpRequest(String method, String url) {this(method, url, null);}
    
    public FjHttpRequest(String method, String url, String content) {
        super(content);
        this.method = method;
        this.url    = url;
    }
    
    public String method() {return method;}
    
    public String url() {return url;}
    
    public Map<String, String> urlParameters() {
        if (!url().contains("?")) return null;
        
        String paramLine = url().split("\\?")[1];
        String[] params = paramLine.split("&");
        Map<String, String> result = new HashMap<String, String>();
        for (String param : params) result.put(param.substring(0, param.indexOf("=")), param.substring(param.indexOf("=") + 1));
        return result;
    }

    @Override
    public String toString() {
        String requesturl = null;
        if (url().contains("//")) requesturl = url().substring(url().substring(url().indexOf("//") + 2).indexOf("/"));
        else requesturl = url();
        return String.format("%s %s HTTP/1.1\r\n"
                + "Content-Type: %s\r\n"
                + "Content-Length: %d\r\n"
                + "\r\n"
                + "%s",
                method(), requesturl,
                contentType(),
                contentLength(),
                null == content() ? "" : content());
    }
}
