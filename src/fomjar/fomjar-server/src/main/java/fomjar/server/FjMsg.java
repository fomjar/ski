package fomjar.server;

import java.net.Socket;

import net.sf.json.JSONException;

public abstract class FjMsg {
	
	public static FjMsg create(Socket conn, final Object data) {
		FjMsg msg = null;
		if (null == msg) {
			try {msg = new FjJsonMsg(conn, data);}
			catch (JSONException e) {}
		}
		if (null == msg) {
			msg = new FjMsg(conn) {
				@Override
				public String toString() {
					return data.toString();
				}
			};
		}
		return msg;
	}
	
	private Socket conn;
	private boolean isSending;
	
	public FjMsg(Socket conn) {
		this.conn = conn;
		isSending = false;
	}
	
	public Socket conn() {
		return conn;
	}
	
	void markSending() {
		isSending = true;
	}
	
	boolean isSending() {
		return isSending;
	}
	
	@Override
	public abstract String toString();
	
}
