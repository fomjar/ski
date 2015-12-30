package fomjar.server.msg;

public class FjDscpResponse extends FjDscpMessage {
	
	public FjDscpResponse() {this(null);}
	
	public FjDscpResponse(Object json) {
		super(json);
		if (!json().containsKey("code")) json().put("code", -1);
		if (!json().containsKey("desc")) json().put("desc", null);
	}

	public int    code() {return json().getInt("code");}
	public Object desc() {return json().get("desc");}

}
