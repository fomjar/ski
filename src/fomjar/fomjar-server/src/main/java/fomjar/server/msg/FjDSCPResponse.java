package fomjar.server.msg;

import net.sf.json.JSONObject;

public class FjDscpResponse extends FjDscpMessage {
	
	public FjDscpResponse()            {this(null);}
	public FjDscpResponse(Object json) {super(json);}

	public int        code() {return json().getInt("code");}
	public JSONObject desc() {return (JSONObject) json().get("desc");}

	@Override
	public boolean isValid() {
		if (!super.isValid()) return false;
		if (!json().containsKey("code")) return false;
		if (!json().containsKey("desc")) return false;
		return true;
	}

}
