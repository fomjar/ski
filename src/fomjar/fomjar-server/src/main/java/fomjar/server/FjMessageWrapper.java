package fomjar.server;

import java.util.HashMap;
import java.util.Map;

import fomjar.server.msg.FjMessage;

public class FjMessageWrapper {
	
	private FjMessage msg;
	private Map<String, Object> attachment;
	
	FjMessageWrapper(FjMessage msg) {
		this.msg = msg;
	}
	
	public FjMessage message() {
		return msg;
	}
	
	public FjMessageWrapper attach(String key, Object value) {
		if (null == key || null == value) throw new NullPointerException();
		
		if (null == attachment) attachment = new HashMap<String, Object>();
		attachment.put(key, value);
		return this;
	}
	
	public Object attachment(String key) {
		if (null == attachment) return null;
		
		return attachment.get(key);
	}
	
	public Map<String, Object> attachments() {
		return attachment;
	}
	
}