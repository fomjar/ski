package fomjar.server.msg;

import java.util.HashMap;
import java.util.Map;

public class FjHttpResponse extends FjHttpMessage {

    private static final Map<Integer, String> desc = new HashMap<Integer, String>();

    static {
        desc.put(200, "OK");
        desc.put(302, "Object moved");
    }

    public static FjHttpResponse parse(String data) {
        String[] head       = data.split("\r\n")[0].split(" ");
        String[] contents   = data.split("\r\n\r\n");
        String   attrs      = contents[0].substring(data.indexOf("\r\n") + 2);
        String   content    = contents.length > 1 ? contents[1] : null;
        FjHttpResponse response = new FjHttpResponse(head[0], Integer.parseInt(head[1]), null, content);
        for (String attr : attrs.split("\r\n")) {
            String[] kv = attr.split(":");
            String   k  = kv[0].trim();
            String   v  = attr.substring(kv[0].length() + 1).trim();
            response.attr().put(k, v);
        }
        return response;
    }

    private String  protocal;
    private int     code;

    public FjHttpResponse(String protocal, int code, String contentType, Object content) {
        super(contentType, content);
        this.protocal   = null == protocal ? "HTTP/1.1" : protocal;
        this.code       = code;
    }

    public String     protocal()         {return protocal;}
    public int         code()             {return code;}
    public void     code(int code)    {this.code = code;}

    @Override
    protected String head() {
        return String.format("%s %d %s", protocal(), code(), desc.get(code()));
    }
}
