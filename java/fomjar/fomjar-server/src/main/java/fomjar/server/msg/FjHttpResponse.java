package fomjar.server.msg;

import java.util.HashMap;
import java.util.Map;

public class FjHttpResponse extends FjHttpMessage {

    private static final Map<Integer, String> desc = new HashMap<Integer, String>();

    static {
        desc.put(100, "Continue");
        desc.put(101, "Switching Protocols");
        desc.put(102, "Processing");
        desc.put(200, "OK");
        desc.put(201, "Created");
        desc.put(202, "Accepted");
        desc.put(203, "Non-Authoritative Information");
        desc.put(204, "No Content");
        desc.put(205, "Reset Content");
        desc.put(206, "Partial Content");
        desc.put(207, "Multi-Status");
        desc.put(300, "Multiple Choices");
        desc.put(301, "Moved Permanently");
        desc.put(302, "Moved Temporarily");
        desc.put(303, "See Other");
        desc.put(304, "Not Modified");
        desc.put(305, "Use Proxy");
        desc.put(306, "Switch Proxy");
        desc.put(307, "Temporary Redirect");
        desc.put(400, "Bad Request");
        desc.put(401, "Unauthorized");
        desc.put(402, "Payment Required");
        desc.put(403, "Forbidden");
        desc.put(404, "Not Found");
        desc.put(405, "Method Not Allowed");
        desc.put(406, "Not Acceptable");
        desc.put(407, "Proxy Authentication Required");
        desc.put(408, "Request Timeout");
        desc.put(409, "Conflict");
        desc.put(401, "Gone");
        desc.put(411, "Length Required");
        desc.put(412, "Precondition Failed");
        desc.put(413, "Request Entity Too Large");
        desc.put(414, "Request-URI Too Long");
        desc.put(415, "Unsupported Media Type");
        desc.put(416, "Requested Range Not Satisfiable");
        desc.put(417, "Expectation Failed");
        desc.put(421, "There are too many connections from your internet address");
        desc.put(422, "Unprocessable Entity");
        desc.put(423, "Locked");
        desc.put(424, "Failed Dependency");
        desc.put(425, "Unordered Collection");
        desc.put(426, "Upgrade Required");
        desc.put(449, "Retry With");
        desc.put(500, "Internal Server Error");
        desc.put(501, "Not Implemented");
        desc.put(502, "Bad Gateway");
        desc.put(503, "Service Unavailable");
        desc.put(504, "Gateway Timeout");
        desc.put(505, "HTTP Version Not Supported");
        desc.put(506, "Variant Also Negotiates");
        desc.put(507, "Insufficient Storage");
        desc.put(509, "Bandwidth Limit Exceeded");
        desc.put(510, "Not Extended");
        desc.put(600, "Unparseable Response Headers");
    }
    
    public static boolean is(String data) {
        return data.startsWith("HTTP/");
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

    public String   protocal()      {return protocal;}
    public int      code()          {return code;}
    public void     code(int code)  {this.code = code;}

    @Override
    protected String head() {
        return String.format("%s %d %s", protocal(), code(), desc.get(code()));
    }
}
