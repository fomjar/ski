package fomjar.server.msg;

import net.sf.json.JSONObject;

public class FjJsonMessage extends FjMessage {
	
	private JSONObject json;
	
	public FjJsonMessage() {
		this(null);
	}
	
	public FjJsonMessage(Object json) {
		if (null == json) this.json = new JSONObject();
		else if (json instanceof JSONObject) this.json = (JSONObject) json;
		else this.json = JSONObject.fromObject(json);
	}
	
	public JSONObject json() {
		return json;
	}
	
	@Override
	public String toString() {
		return json().toString();
	}
	
}
