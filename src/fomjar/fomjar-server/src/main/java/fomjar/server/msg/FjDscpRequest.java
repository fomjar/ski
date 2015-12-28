package fomjar.server.msg;

import net.sf.json.JSONObject;

public class FjDscpRequest extends FjDscpMessage {
	
	public FjDscpRequest()            {this(null);}
	public FjDscpRequest(Object json) {super(json);}

	public int        cmd() {return json().getInt("cmd");}
	public JSONObject arg() {return (JSONObject) json().get("arg");}
	
	@Override
	public boolean isValid() {
		if (!super.isValid()) return false;
		if (!json().containsKey("cmd")) return false;
		if (!json().containsKey("arg")) return false;
		return true;
	}

}
