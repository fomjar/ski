package fomjar.server;

import java.net.Socket;

import net.sf.json.JSONObject;

public class FjJsonMsg extends FjMsg {
	
	private JSONObject json;
	
	public FjJsonMsg(Socket conn, Object json) {
		super(conn);
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
