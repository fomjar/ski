package fomjar.server.msg;

public class FjHttpResponse extends FjHttpMessage {
    
    private int code;
    
    public FjHttpResponse() {this(200, null);}
    
    public FjHttpResponse(String content) {this(200, content);}

    public FjHttpResponse(int code, String content) {
        super(content);
        this.code = code;
    }
    
    public int code() {return code;}
    
    @Override
    public String toString() {
        return String.format("HTTP/1.1 %d OK\r\n"
                + "Server: fjserver/0.1\r\n"
                + "Content-Type: %s\r\n"
                + "Content-Length: %d\r\n"
                + "\r\n"
                + "%s",
                code(),
                contentType(),
                contentLength(),
                null == content() ? "" : content());
    }
}
