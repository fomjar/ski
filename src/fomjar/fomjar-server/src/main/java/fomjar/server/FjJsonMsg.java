package fomjar.server;

import net.sf.json.JSONObject;

public class FjJsonMsg extends FjMsg {
	
	private JSONObject json;
	
	public FjJsonMsg() {
		this(null);
	}
	
	public FjJsonMsg(Object json) {
		if (null == json) this.json = new JSONObject();
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