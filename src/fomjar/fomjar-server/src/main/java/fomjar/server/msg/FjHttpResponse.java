package fomjar.server.msg;

import java.util.Date;

public class FjHttpResponse extends FjHttpMessage {
	
	private static final String TEMPLATE = "HTTP/1.1 200 OK\r\n"
			+ "Connection: keep-alive\r\n"
			+ "Date: %s\r\n"
			+ "Server: fomjar\r\n"
			+ "Content-Type: %s\r\n"
			+ "Content-Length: %d\r\n"
			+ "\r\n"
			+ "%s";
			
	
	public FjHttpResponse(String contentType, String content) {
		super(String.format(TEMPLATE, new Date().toString(), contentType, null == content ? 0 : content.length(), null == content ? "" : content));
	}

	public FjHttpResponse(String http) {
		super(http);
	}
	
	public String titleVersion() {
		return title().split(" ")[0];
	}
	
	public int titleReturnCode() {
		return Integer.parseInt(title().split(" ")[1]);
	}
	
	public String titleReturnDescription() {
		return title().split(" ")[2];
	}

}
