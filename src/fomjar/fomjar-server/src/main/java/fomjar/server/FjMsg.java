package fomjar.server;

public abstract class FjMsg {
	
	public static FjMsg create(final String data) {
		if (data.startsWith("GET")
				|| data.startsWith("POST")
				|| data.startsWith("HEAD")) return new FjHttpRequest(data);
		if (data.startsWith("HTTP/")) return new FjHttpResponse(data);
		if (data.startsWith("{")) return new FjJsonMsg(data);
		return new FjMsg() {
			@Override
			public String toString() {
				return data;
			}
		};
	}
	
	public FjMsg() {
	}
	
	@Override
	public abstract String toString();
	
}
