package fomjar.server;

public abstract class FjMessage {
	
	public static FjMessage create(final String data) {
		if (data.startsWith("GET")
				|| data.startsWith("POST")
				|| data.startsWith("HEAD")) return new FjHttpRequest(data);
		if (data.startsWith("HTTP/")) return new FjHttpResponse(data);
		if (data.startsWith("{")) return new FjJsonMessage(data);
		return new FjMessage() {
			@Override
			public String toString() {
				return data;
			}
		};
	}
	
	public FjMessage() {
	}
	
	@Override
	public abstract String toString();
	
}
