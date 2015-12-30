package fomjar.server.msg;

import java.util.Random;

public class FjDscpMessage extends FjJsonMessage {
	
	public FjDscpMessage(Object json) {super(json);}
	
	public String fs()  {return json().getString("fs");}
	public String ts()  {return json().getString("ts");}
	public String sid() {return json().getString("sid");}
	public int    ssn() {return json().getInt("ssn");}

	public boolean isValid() {
		if (!json().containsKey("fs"))  return false;
		if (!json().containsKey("ts"))  return false;
		if (!json().containsKey("sid")) return false;
		if (!json().containsKey("ssn")) return false;
		return true;
	}
	
	private static final Random random = new Random();
	public static String newSid(String serverName) {
		return Integer.toHexString(serverName.hashCode())
				+ Integer.toHexString(Long.toHexString(System.currentTimeMillis()).hashCode())
				+ Integer.toHexString(String.valueOf(random.nextInt()).hashCode());
	}
	
}
