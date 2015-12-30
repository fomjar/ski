package fomjar.server.msg;

public class FjDscpRequest extends FjDscpMessage {
	
	public FjDscpRequest() {this(null);}
	
	public FjDscpRequest(Object json) {
		super(json);
		if (!json().containsKey("cmd")) json().put("cmd", -1);
		if (!json().containsKey("arg")) json().put("arg", null);
	}

	public int    cmd() {return json().getInt("cmd");}
	public Object arg() {return json().get("arg");}
	
}
