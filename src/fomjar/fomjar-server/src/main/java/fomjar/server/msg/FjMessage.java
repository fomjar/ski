package fomjar.server.msg;


public abstract class FjMessage {
	
	public static FjMessage create(final String data) {
		if (data.startsWith("GET")
				|| data.startsWith("POST")
				|| data.startsWith("HEAD")) return new FjHttpRequest(data);
		if (data.startsWith("HTTP/")) return new FjHttpResponse(data);
		if (data.startsWith("{")) {
			FjJsonMessage jmsg = new FjJsonMessage(data);
			if (jmsg.json().containsKey("fs")
					&& jmsg.json().containsKey("ts")
					&& jmsg.json().containsKey("sid")
					&& jmsg.json().containsKey("ssn")) {
				if (jmsg.json().containsKey("cmd")
						&& jmsg.json().containsKey("arg")) return new FjDSCPRequest(data);
				else if (jmsg.json().containsKey("code")
						&& jmsg.json().containsKey("desc")) return new FjDSCPResponse(data);
				else return new FjDSCPMessage(data);
			} else {
				return jmsg;
			}
		}
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
