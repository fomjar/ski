package fomjar.server.msg;

import org.w3c.dom.Document;

import net.sf.json.JSONObject;

public class FjHttpMessage implements FjMessage {
	
	private String content;
	
	public FjHttpMessage() {this(null);}
	
	public FjHttpMessage(String content) {
		if (null == content) content = "";
		this.content = content;
	}
	
	public int contentLength() {return content.length();}
	
	public String contentType() {
		if (content.startsWith("<")) return "text/xml";
		if (content.startsWith("{")) return "application/json";
		return "text/plain";
	}
	
	public String content() {return content;}
	
	public JSONObject contentToJson() {return new FjJsonMessage(content()).json();}
	
	public Document   contentToXml() {return new FjXmlMessage(content()).xml();}

	@Override
	public String toString() {return content();}
}
