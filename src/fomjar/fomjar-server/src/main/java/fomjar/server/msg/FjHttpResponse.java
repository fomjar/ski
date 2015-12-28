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

}
