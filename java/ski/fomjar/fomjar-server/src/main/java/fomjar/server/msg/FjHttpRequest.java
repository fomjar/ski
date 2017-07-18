package fomjar.server.msg;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

public class FjHttpRequest extends FjHttpMessage {
    
    private static final String[] methods = new String[] {
            "GET",
            "POST",
            "HEAD",
            "PUT",
            "DELETE",
            "CONNECT",
            "OPTIONS",
            "PATCH",
            "PROPFIND",
            "PROPPATCH",
            "MKCOL",
            "COPY",
            "MOVE",
            "LOCK",
            "UNLOCK",
            "TRACE"
    };
    
    public static boolean is(String data) {
        for (String m : methods) {
            if (data.startsWith(m)) return true;
        }
        return false;
    }

    public static FjHttpRequest parse(String data) {
        String[] head         = data.split("\r\n")[0].split(" ");
        String[] contents   = data.split("\r\n\r\n");
        String   attrs        = contents[0].substring(data.indexOf("\r\n") + 2);
        String   content     = contents.length > 1 ? contents[1] : null;
        FjHttpRequest request = new FjHttpRequest(head[0].trim(), head[1].trim(), null, content);
        for (String attr : attrs.split("\r\n")) {
            String[] kv = attr.split(":");
            String   k  = kv[0].trim();
            String   v  = attr.substring(kv[0].length() + 1).trim();
            request.attr().put(k, v);
        }
        return request;
    }

    private String method;
    private String url;

    public FjHttpRequest(String method, String url, String contentType, Object content) {
        super(contentType, content);
        this.method = method;
        try {this.url = URLDecoder.decode(url, "utf-8");}
        catch (UnsupportedEncodingException e) {
            this.url = url;
            e.printStackTrace();
        }
    }

    public String method()    {return method;}
    public String url()     {return url;}
    public String path()    {return url().contains("?") ? url().substring(0, url.indexOf("?")) : url();}

    public Map<String, String> urlArgs() {
        if (!url().contains("?")) return new HashMap<String, String>();

        String paramLine = url().split("\\?")[1];
        String[] params = paramLine.split("&");
        Map<String, String> result = new HashMap<String, String>();
        for (String param : params) {
            if (param.contains("=")) result.put(param.substring(0, param.indexOf("=")), param.substring(param.indexOf("=") + 1));
            else result.put(param, null);
        }
        return result;
    }

    public JSONObject argsToJson() {
        JSONObject json = contentToJson();
        json.putAll(urlArgs());
        return json;
    }

    @Override
    protected String head() {
        String requesturl = null;
        if (url().contains("//")) requesturl = url().substring(url().substring(url().indexOf("//") + 2).indexOf("/"));
        else requesturl = url();
        return String.format("%s %s HTTP/1.1", method(), requesturl);
    }
}
