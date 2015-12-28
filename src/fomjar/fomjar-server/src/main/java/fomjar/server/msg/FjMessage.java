package fomjar.server.msg;


public abstract class FjMessage {
	
	public static FjMessage create(final String data) {
		if (data.startsWith("GET")
				|| data.startsWith("POST")
				|| data.startsWith("HEAD")) {
			String[] title = data.split("\r\n")[0].split(" ");
			String content = data.contains("\r\n\r\n") ? data.split("\r\n\r\n")[1] : null;
			return new FjHttpRequest(title[0], title[1], content);
		}
		if (data.startsWith("HTTP/")) {
			int code = Integer.parseInt(data.split("\r\n")[0].split(" ")[1]);
			String content = data.contains("\r\n\r\n") ? data.split("\r\n\r\n")[1] : null;
			return new FjHttpResponse(code, content);
		}
		if (data.startsWith("{")) {
			FjJsonMessage jmsg = new FjJsonMessage(data);
			if (jmsg.json().containsKey("fs")
					&& jmsg.json().containsKey("ts")
					&& jmsg.json().containsKey("sid")
					&& jmsg.json().containsKey("ssn")) {
				if (jmsg.json().containsKey("cmd")
						&& jmsg.json().containsKey("arg")) return new FjDscpRequest(data);
				else if (jmsg.json().containsKey("code")
						&& jmsg.json().containsKey("desc")) return new FjDscpResponse(data);
				else return new FjDscpMessage(data);
			} else {
				return jmsg;
			}
		}
		if (data.startsWith("<")) return new FjXmlMessage(data);
		return new FjMessage() {@Override public String toString() {return data;}};
	}
	
	public FjMessage() {}
	
	@Override
	public abstract String toString();
	
}
