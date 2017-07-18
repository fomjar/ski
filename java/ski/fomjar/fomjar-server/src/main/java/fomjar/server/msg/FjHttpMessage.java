package fomjar.server.msg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

import fomjar.server.FjMessage;
import net.sf.json.JSONObject;

public abstract class FjHttpMessage implements FjMessage {

    private Map<String, String>    attr;
    private Map<String, Map<String, String>>    setcookie;
    private String    contentType;
    private byte[]    content;

    public FjHttpMessage() {this(null, null);}

    public FjHttpMessage(String contentType, Object content) {
        this.contentType    = null == contentType ? "text/plain" : contentType;
        try {this.content   = null == content ? new byte[] {} : content instanceof byte[] ? (byte[])content : content.toString().getBytes("utf-8");}
        catch (UnsupportedEncodingException e) {e.printStackTrace();}
        this.attr           = new HashMap<>();
        this.setcookie      = new HashMap<>();
    }
    
    public void contentCatch(SocketChannel conn, ByteBuffer buf, long timeout) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(content());
        
        int protocal_length = 0;
        if (attr().containsKey("Content-Length"))
            protocal_length = Integer.parseInt(attr().get("Content-Length"));
        
        long begin = System.currentTimeMillis();
        buf.clear();
        while (baos.size() < protocal_length && System.currentTimeMillis() - begin < timeout) {
            if (conn.read(buf) > 0) {
                buf.flip();
                baos.write(buf.array(), buf.position(), buf.limit());
                buf.clear();
                begin = System.currentTimeMillis();
            } else {
                try {Thread.sleep(100L);}
                catch (InterruptedException e) {e.printStackTrace();}
            }
        }
        content(baos.toByteArray());
    }

    protected abstract String head();

    public Map<String, String> attr()     {return attr;};
    public int      contentLength() {return null == content() ? 0 : content().length;}
    public String   contentType()   {return attr().containsKey("Content-Type") ? attr.get("Content-Type") : contentType;}
    public byte[]   content()       {return content;}

    public void         content(Object content) {
        if (content instanceof byte[]) {
            this.content = (byte[]) content;
        } else {
            try {this.content = content.toString().getBytes("utf-8");}
            catch (UnsupportedEncodingException e) {e.printStackTrace();}
        }
    }
    public String       contentToString()       {
        try {return new String(content(), "utf-8");}
        catch (UnsupportedEncodingException e) {e.printStackTrace();}
        return null;
    }
    public JSONObject   contentToJson()         {
            try {return new FjJsonMessage(new String(content(), "utf-8")).json();}
            catch (UnsupportedEncodingException e) {e.printStackTrace();}
            return null;
    }
    public Document     contentToXml()          {
        try {return new FjXmlMessage(new String(content(), "utf-8")).xml();}
        catch (UnsupportedEncodingException e) {e.printStackTrace();}
        return null;
    }

    public Map<String, String> cookie() {
        Map<String, String> cookie = new HashMap<>();

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

    public void setcookie(String key, String val, String domain, String path, String expires) {
        if (setcookie.containsKey(key)) setcookie.remove(key);

        Map<String, String> cookie = new LinkedHashMap<>();
        try {val = URLEncoder.encode(val, "utf-8");}
        catch (UnsupportedEncodingException e) {e.printStackTrace();}

        cookie.put(key, val);
        if (null != domain)     cookie.put("domain",    domain);
        if (null != path)       cookie.put("path",      path);
        if (null != expires)    cookie.put("expires",   expires);
        setcookie.put(key, cookie);
    }

    public void setcookie(String key, String val) {
        setcookie(key, val, null, "/", null);
    }

    @Override
    public String toString() {
        attr().put("Content-Type",      contentType());
        attr().put("Content-Length",    String.valueOf(contentLength()));

        StringBuilder sb = new StringBuilder();
        sb.append(head() + "\r\n");
        attr().entrySet().forEach(entry->{
            if (entry.getKey().equals("Content-Type")
                    && (entry.getValue().startsWith("text") || entry.getValue().startsWith("application")))
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
        return sb.toString();
    }
}
