package fomjar.server.msg;

public class FjHttpResponse extends FjHttpMessage {
    
    private int code;
    
    public FjHttpResponse() {this(200, null, null);}
    
    public FjHttpResponse(String contentType, String content) {this(200, contentType, content);}

    public FjHttpResponse(int code, String contentType, String content) {
        super(contentType, content);
        this.code = code;
    }
    
    public int code() {return code;}
    
    @Override
    public String toString() {
        return String.format("HTTP/1.1 %d OK\r\n"
                + "Server: fjserver/0.1\r\n"
                + "Content-Type: %s;charset=UTF-8\r\n"
                + "Content-Length: %d\r\n"
                + "\r\n"
                + "%s",
                code(),
                contentType(),
                contentLength(),
                null == content() ? "" : content());
    }
}
