package fomjar.server.msg;

import java.util.Random;

public class FjDscpMessage extends FjJsonMessage {
	
	public FjDscpMessage() {this(null);}
	
	public FjDscpMessage(Object json) {
		super(json);
		if (!json().containsKey("fs"))  json().put("fs",  null);
		if (!json().containsKey("ts"))  json().put("ts",  null);
		if (!json().containsKey("sid")) json().put("sid", newSid());
		if (!json().containsKey("cmd")) json().put("cmd", -1);
		if (!json().containsKey("arg")) json().put("arg", null);
	}
	
	public String fs()  {return json().getString("fs");}
	public String ts()  {return json().getString("ts");}
	public String sid() {return json().getString("sid");}
	public int    cmd() {return json().getInt("cmd");}
	public Object arg() {return json().get("arg");}
	
	private static final Random random = new Random();
	private static String newSid() {
		return Integer.toHexString(Long.toHexString(System.currentTimeMillis()).hashCode())
				+ Integer.toHexString(String.valueOf(random.nextInt()).hashCode());
	}
	
}
