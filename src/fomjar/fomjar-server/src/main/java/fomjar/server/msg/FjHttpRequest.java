package fomjar.server.msg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;


public class FjHttpRequest extends FjHttpMessage {
    
    private String method;
    private String url;
    
    public FjHttpRequest(String method, String url) {this(method, url, null, null);}
    
    public FjHttpRequest(String method, String url, String contentType, String content) {
        super(contentType, content);
        this.method = method;
        this.url    = url;
    }
    
    public String method() {return method;}
    
    public String url() {return url;}
    
    public Map<String, String> urlArgs() {
        if (!url().contains("?")) return null;
        
        String paramLine = url().split("\\?")[1];
        String[] params = paramLine.split("&");
        Map<String, String> result = new HashMap<String, String>();
        for (String param : params) {
            if (param.contains("=")) result.put(param.substring(0, param.indexOf("=")), param.substring(param.indexOf("=") + 1));
            else result.put(param, null);
        }
        return result;
    }

    @Override
    public String toString() {
        String requesturl = null;
        if (url().contains("//")) requesturl = url().substring(url().substring(url().indexOf("//") + 2).indexOf("/"));
        else requesturl = url();
        return String.format("%s %s HTTP/1.1\r\n"
                + "Content-Type: %s;charset=UTF-8\r\n"
                + "Content-Length: %d\r\n"
                + "\r\n"
                + "%s",
                method(), requesturl,
                contentType(),
                contentLength(),
                null == content() ? "" : content());
    }
    
    private ByteBuffer buf = null;
    
    public FjJsonMessage toJsonMessage(SocketChannel conn) {
        Map<String, String> urlargs = urlArgs();
        JSONObject args = contentToJson();
        
        if ("POST".equals(method()) && args.isEmpty()) {
            if (null == buf) buf = ByteBuffer.allocate(1024 * 1024);
            long timeout = 1000L * 3;
            long start = System.currentTimeMillis();
            while (args.isEmpty() && System.currentTimeMillis() - start < timeout) {
                try{Thread.sleep(50L);}
                catch (InterruptedException e) {e.printStackTrace();}
                buf.clear();
                try {conn.read(buf);}
                catch (IOException e) {e.printStackTrace();}
                buf.flip();
                if (!buf.hasRemaining()) continue;
                
                args = JSONObject.fromObject(Charset.forName("utf-8").decode(buf).toString());
                break;
            }
        }
        
        if (null != urlargs) args.putAll(urlargs);
        return new FjJsonMessage(args);
    }
    
    public FjXmlMessage toXmlMessage(SocketChannel conn) {
        String xml = content();
        if (null != xml && 0 < xml.length()) return new FjXmlMessage(xml);
        
        if ("POST".equals(method())) {
            if (null == buf) buf = ByteBuffer.allocate(1024 * 1024);
            long timeout = 1000L * 3;
            long start = System.currentTimeMillis();
            while ((null == xml || 0 == xml.length()) && System.currentTimeMillis() - start < timeout) {
                try{Thread.sleep(50L);}
                catch (InterruptedException e) {e.printStackTrace();}
                buf.clear();
                try {conn.read(buf);}
                catch (IOException e) {e.printStackTrace();}
                buf.flip();
                if (!buf.hasRemaining()) continue;
                
                return new FjXmlMessage(Charset.forName("utf-8").decode(buf).toString());
            }
        }
        return null;
    }
}
