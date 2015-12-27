package fomjar.server.msg;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

public class FjHttpMessage extends FjMessage {
	
	private static final String ln = "\r\n";
	
	private String title;
	private Map<String, String> head;
	private StringBuffer content;
	
	FjHttpMessage(String http) {
		String[] lines = http.split("\n");
		int phase = 0;
		for (String line : lines) {
			line = line.trim();
			switch (phase) {
			case 0:
				phase++;
				title = line;
				break;
			case 1:
				if (0 == line.length()) {
					phase++;
					break;
				}
				if (null == head) head = new HashMap<String, String>();
				String[] kvs = line.split(";");
				for (String kv : kvs) {
					kv = kv.trim();
					String k = kv.substring(0, kv.indexOf(":")).trim();
					String v = kv.substring(kv.indexOf(":") + 1).trim();
					head.put(k, v);
				}
				break;
			case 2:
				if (null == content) content = new StringBuffer();
				content.append(line + ln);
				break;
			default:
				break;
			}
		}
	}
	
	public String title() {
		return title;
	}
	
	public String head(String key) {
		if (null == head) return null;
		return head.get(key);
	}
	
	public Map<String, String> heads() {
		return head;
	}
	
	public String content() {
		if (null == content) return null;
		
		return content.toString();
	}
	
	public JSONObject contentToJson() {
		if (null == content()) return null;
		
		if (content().trim().startsWith("<")) return JSONObject.fromObject(new XMLSerializer().read(content()));
		else if (content().trim().startsWith("{")) return JSONObject.fromObject(content());
		else return null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(title + ln);
		for (Map.Entry<String, String> e : head.entrySet()) {
			sb.append(e.getKey() + ": " + e.getValue() + ln);
		}
		sb.append(ln);
		sb.append(content);
		return sb.toString();
	}
}
