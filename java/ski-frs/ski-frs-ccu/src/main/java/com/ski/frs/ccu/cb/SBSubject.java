package com.ski.frs.ccu.cb;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * <style>
 * table, th, td {
 *     border-collapse  :collapse;
 *     border           : 1px solid black;
 * }
 * th, td {padding : .5em 1em;}
 * </style>
 * 
 * 主体库
 * <table>
 * <tr><th>键</th><th>描述</th></tr>
 * <tr><td>sid  </td><td>主体库编号</td></tr>
 * <tr><td>name </td><td>名称</td></tr>
 * <tr><td>type </td><td>类型：0 - 人，1 - 汽车</td></tr>
 * <tr><td>time </td><td>创建时间</td></tr>
 * <tr><td>items</td><td>主体项</td></tr>
 * </table>
 * 
 * 人像库：
 * <table>
 * <tr><th>键</th><th>描述</th></tr>
 * <tr><td>sid      </td><td>主体库编号</td></tr>
 * <tr><td>siid     </td><td>主体项编号</td></tr>
 * <tr><td>time     </td><td>创建时间</td></tr>
 * <tr><td>name     </td><td>姓名</td></tr>
 * <tr><td>gender   </td><td>性别：0 - 女，1 - 男</td></tr>
 * <tr><td>birth    </td><td>生日</td></tr>
 * <tr><td>idno     </td><td>身份证号</td></tr>
 * <tr><td>phone    </td><td>电话</td></tr>
 * <tr><td>addr     </td><td>地址</td></tr>
 * <tr><td>pics     </td><td>图片</td></tr>
 * </table>
 */
public class SBSubject extends StoreBlock {

    private static final long serialVersionUID = 1L;
    
    public JSONObject setSubject(JSONObject sub) {
        String sid = null;
        if (!sub.containsKey("sid")) sub.put("sid", sid = "subject-" + UUID.randomUUID().toString().replace("-", ""));
        else sid = sub.get("sid").toString();
        if (!sub.containsKey("time")) sub.put("time", System.currentTimeMillis());
        if (!sub.containsKey("items")) sub.put("items", new JSONObject());
        return (JSONObject) data().put(sid, sub);
    }
    
    public void setSubjectItem(String sid, JSONObject item) {
        if (!data().containsKey(sid)) return;
        JSONObject sub = (JSONObject) data().get(sid);
        
        item.put("sid", sid);
        String siid = null;
        if (!item.has("siid")) item.put("siid", siid = "subject-item-" + UUID.randomUUID().toString().replace("-", ""));
        else siid = item.getString("siid");
        if (!item.has("time")) item.put("time", System.currentTimeMillis());
        if (!item.has("pics")) item.put("pics", new JSONArray());
        JSONObject items = sub.getJSONObject("items");
        items.put(siid, item);
    }
    
    public List<JSONObject> delSubject(String... sid) {
        List<JSONObject> list = new LinkedList<>();
        for (String s : sid) {
            JSONObject sub = (JSONObject) data().remove(s);
            if (null != sub) list.add(sub);
        }
        return list;
    }
    
    public List<JSONObject> delSubjectItem(String sid, String... siid) {
        if (!data().containsKey(sid)) return null;
        
        JSONObject sub = (JSONObject) data().get(sid);
        List<JSONObject> list = new LinkedList<>();
        JSONObject items = sub.getJSONObject("items");
        for (String s : siid) {
            JSONObject item = (JSONObject) items.remove(s);
            if (null != item) list.add(item);
        }
        return list;
    }
    
    public JSONObject modSubject(JSONObject sub) {
        if (!sub.containsKey("sid")) return null;
        
        String sid = sub.getString("sid");
        if (!data().containsKey(sid)) return null;
        JSONObject sub_old = (JSONObject) data().get(sid);
        sub_old.putAll(sub);
        sub.putAll(sub_old);
        return (JSONObject) data().put(sid, sub);
    }
    
    public JSONObject modSubjectItem(JSONObject item) {
        if (!item.containsKey("sid") || !item.containsKey("siid")) return null;
        
        String sid = item.getString("sid");
        String siid = item.getString("siid");
        if (!data().containsKey(sid)) return null;
        JSONObject sub = (JSONObject) data().get(sid);
        if (!sub.containsKey(siid)) return null;
        JSONObject item_old = sub.getJSONObject(siid);
        item_old.putAll(item);
        item.putAll(item_old);
        return (JSONObject) sub.put(siid, item);
    }
    
    public List<JSONObject> getSubject(String... sid) {
        if (null != sid && 0 < sid.length) {
            return data().entrySet().parallelStream()
                    .filter(e->{for (String s : sid) if (e.getKey().equals(s)) return true; return false;})
                    .map(e->(JSONObject) e.getValue())
                    .collect(Collectors.toList());
        } else {
            return data().entrySet().parallelStream()
                    .map(e->{
                        JSONObject sub = JSONObject.fromObject((JSONObject) e.getValue());
                        sub.put("items", sub.getJSONObject("items").size());
                        return sub;
                    })
                    .collect(Collectors.toList());
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<JSONObject> getSubjectItem(JSONObject args) {
        return data().entrySet().parallelStream()
                .map(e->(JSONObject) e.getValue())
                .filter(sub->{
                    if (!args.has("sid")) return true;
                    
                    Object obj = args.get("sid");
                    List<String> sids = new LinkedList<>();
                    if (obj instanceof JSONArray) {
                        JSONArray array = (JSONArray) obj;
                        for (int i = 0; i < array.size(); i++) sids.add(array.getString(i));
                    } else sids.add(obj.toString());
                    for (String sid : sids) {
                        if (sid.equals(sub.get("sid"))) return true;
                    }
                    return false;
                })
                .flatMap(sub->new LinkedList<Object>(sub.getJSONObject("items").values()).stream())
                .map(item->(JSONObject) item)
                .filter(item->{
                    if (!args.has("name") || !item.has("name")) return true;
                    if (item.getString("name").contains(args.getString("name"))) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.has("gender") || !item.has("gender")) return true;
                    if (item.getInt("gender") == args.getInt("gender")) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.has("birth") || !item.has("birth")) return true;
                    if (item.getString("birth").contains(args.getString("birth"))) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.has("idno") || !item.has("idno")) return true;
                    if (item.getString("idno").contains(args.getString("idno"))) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.has("phone") || !item.has("phone")) return true;
                    if (item.getString("phone").contains(args.getString("phone"))) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.has("addr") || !item.has("addr")) return true;
                    if (item.getString("addr").contains(args.getString("addr"))) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.has("birth_min") || !item.has("birth")) return true;
                    
                    String birth = item.getString("birth");
                    int year = Integer.parseInt(birth.substring(0, 4));
                    if (args.getInt("birth_min") <= year) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.has("birth_max") || !item.has("birth")) return true;
                    
                    String birth = item.getString("birth");
                    int year = Integer.parseInt(birth.substring(0, 4));
                    if (args.getInt("birth_max") >= year) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.has("fv") || !args.has("min") || !args.has("max")) return true;
                    
                    JSONArray array = args.getJSONArray("fv");
                    double[] fva = new double[array.size()];
                    for (int i = 0; i < array.size(); i++) fva[i] = array.getDouble(i);
                    double min = args.getDouble("min");
                    double max = args.getDouble("max");
                    
                    JSONArray pics = item.getJSONArray("pics");
                    double tv_max = 0;
                    for (int i = 0; i < pics.size(); i++) {
                        JSONObject pic = pics.getJSONObject(i);
                        if (!pic.containsKey("fv")) continue;
                        
                        array = pic.getJSONArray("fv");
                        double[] fvp = new double[array.size()];
                        for (int j = 0; j < array.size(); j++) fvp[j] = array.getDouble(j);
                        
                        double tv = transvection(fva, fvp);
                        if (tv > tv_max) tv_max = tv;
                        item.put("tv", tv_max);
                        if (min <= tv && tv <= max) return true;
                    }
                    return false;
                })
                .sorted((item1, item2)->{
                    if (!item2.has("tv") && !item1.has("tv")) return 0;
                    if (!item2.has("tv")) return 1;
                    if (!item1.has("tv")) return -1;
                    return (int) (item2.getDouble("tv") * 100000 - item1.getDouble("tv") * 100000);
                })
                .collect(Collectors.toList());
    }
    private static double transvection(double[] v1, double[] v2) {
        double tv = 0;
        for (int i = 0; i < Math.min(v1.length, v2.length); i++) tv += v1[i] * v2[i];
        return tv;
    }

}
