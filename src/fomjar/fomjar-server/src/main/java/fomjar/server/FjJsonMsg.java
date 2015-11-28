package fomjar.server;

import net.sf.json.JSONObject;

public class FjJsonMsg extends FjMsg {
	
	private JSONObject json;
	
	public FjJsonMsg(Object json) {
		this.json = JSONObject.fromObject(json);
	}
	
	public JSONObject json() {
		return json;
	}
	
	@Override
	public String toString() {
		return json().toString();
	}
	
}
