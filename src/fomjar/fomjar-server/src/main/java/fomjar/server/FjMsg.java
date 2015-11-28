package fomjar.server;

import net.sf.json.JSONException;

public abstract class FjMsg {
	
	public static FjMsg create(final Object data) {
		FjMsg msg = null;
		if (null == msg) {
			try {msg = new FjJsonMsg(data);}
			catch (JSONException e) {}
		}
		if (null == msg) {
			msg = new FjMsg() {
				@Override
				public String toString() {
					return null == data ? null : data.toString();
				}
			};
		}
		return msg;
	}
	
	public FjMsg() {
	}
	
	@Override
	public abstract String toString();
	
}
