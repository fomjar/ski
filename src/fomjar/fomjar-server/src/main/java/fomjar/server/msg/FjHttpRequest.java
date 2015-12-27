package fomjar.server.msg;

import java.util.HashMap;
import java.util.Map;

public class FjHttpRequest extends FjHttpMessage {
	
	private static final String TEMPLATE = "%s %s HTTP/1.1\r\n"
			+ "Host: %s\r\n"
			+ "Connection: Keep-Alive\r\n"
			+ "User-Agent: fomjar\r\n"
			+ "Content-Type: %s\r\n"
			+ "Content-Length: %d\r\n"
			+ "Accept: */*\r\n"
			+ "\r\n"
			+ "%s";
	
	public FjHttpRequest(String method, String url, String bodyType, String body) {
		super(String.format(TEMPLATE,
				method,
				url.substring(url.indexOf("/", url.indexOf("//") + 2)),
				url.substring(url.indexOf("//") + 2, url.indexOf("/", url.indexOf("//") + 2)),
				bodyType,
				null == body ? 0 : body.length(),
				null == body ? "" : body));
	}
	
	public FjHttpRequest(String http) {
		super(http);
	}
	
	public String titleMethod() {
		return title().split(" ")[0];
	}
	
	public String titleUrl() {
		return title().split(" ")[1];
	}
	
	public String titleVersion() {
		return title().split(" ")[2];
	}
	
	public String titleParam(String key) {
		return titleParams().get(key);
	}
	
	public Map<String, String> titleParams() {
		Map<String, String> params = new HashMap<String, String>();
		if (titleUrl().contains("?")) {
			String paramString = titleUrl().substring(titleUrl().indexOf("?") + 1);
			for (String param : paramString.split("&")) {
				String k = param.split("=")[0];
				String v = param.split("=")[1];
				params.put(k, v);
			}
		}
		return params;
	}
}
