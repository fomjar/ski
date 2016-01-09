package fomjar.server.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjMessage;

/**
 * 会话控制块，用于记录会话状态
 * 
 * @author fomja
 */
public class FjSCB {
	
	private Map<String, Object> data;
	
	FjSCB(String sid) {
		data = new HashMap<String, Object>();
		data.put("end", false);
		data.put("sid", sid);
		data.put("ssn", -1);
	}
	
	public Map<String, Object> getAll()    {return data;}
	public Object  get        (String key) {return data.get(key);}
	public String  getString  (String key) {return (String)  data.get(key);}
	public boolean getBoolean (String key) {return (boolean) data.get(key);}
	public byte    getByte    (String key) {return (byte)    data.get(key);}
	public char    getChar    (String key) {return (char)    data.get(key);}
	public short   getShort   (String key) {return (short)   data.get(key);}
	public int     getInteger (String key) {return (int)     data.get(key);}
	public long    getLong    (String key) {return (long)    data.get(key);}
	public float   getFloat   (String key) {return (float)   data.get(key);}
	public double  getDouble  (String key) {return (double)  data.get(key);}
	public boolean has        (String key) {return data.containsKey(key);}
	public FjSCB   put(String key, Object value) {data.put(key, value); return this;}

	public  String  sid()     {return getString("sid");}
	public  int     ssn()     {return getInteger("ssn");}
	public  void    end()     {data.put("end", true);}
	
	boolean isEnd()   {return getBoolean("end");}
	void next(FjMessage msg) {
		put("ssn", ssn() + 1);
		data.put("msg." + ssn(), msg);
	}
	
	public FjDscpMessage msg(int ssn) {return (FjDscpMessage) get("msg." + ssn);}
	public List<FjDscpMessage> msgs() {
		return data.entrySet().stream()
				.filter ((entry)->         {return entry.getKey().startsWith("msg.");})
				.sorted ((entry1, entry2)->{return entry1.getKey().compareTo(entry2.getKey());})
				.map    ((entry)->         {return (FjDscpMessage) entry.getValue();})
				.collect(Collectors.toList());
	}
}