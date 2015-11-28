package fomjar.server;

import java.util.HashMap;
import java.util.Map;

public class FjConnectionCache {
	
	private Map<String, FjMsg> map;
	
	public FjConnectionCache() {
		map = new HashMap<String, FjMsg>();
	}
	
	public void put(String sid, FjMsg msg) {
		map.put(sid, msg);
	}
	
	public FjMsg get(String sid) {
		return map.get(sid);
	}
	
	public FjMsg remove(String sid) {
		return map.remove(sid);
	}
	
	public boolean has(FjMsg msg) {
		return map.containsValue(msg);
	}

}
