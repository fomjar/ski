package fomjar.server;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

public class FjHttpRequest extends FjHttpMsg {

	public FjHttpRequest(String http) {
		super(http);
	}
	
	public String titleMethod() {
		return title().split(" ")[0];
	}
	
	public String titleUrl() {
		return title().substring(title().indexOf(" ") + 1, title().indexOf(" HTTP"));
	}
	
	public String titleParam(String key) {
		return titleParams().get(key);
	}
	
	public Map<String, String> titleParams() {
		Map<String, String> params = new HashMap<String, String>();
		if (titleUrl().contains("?")) {
			String paramString = titleUrl().substring(titleUrl().indexOf("?") + 1);
			for (String param : paramString.split("&")) {
				String k = param.split("=")[0];
				String v = param.split("=")[1];
				params.put(k, v);
			}
		}
		return params;
	}
	
	public JSONObject bodyToJson() {
		if (body().trim().startsWith("<")) return JSONObject.fromObject(new XMLSerializer().read(body()));
		else if (body().trim().startsWith("{")) return JSONObject.fromObject(body());
		else return null;
	}
}
