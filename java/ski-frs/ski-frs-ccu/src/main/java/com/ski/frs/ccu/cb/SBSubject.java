package com.ski.frs.ccu.cb;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import net.sf.json.JSONArray;

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
 * <tr><td>pids     </td><td>图片</td></tr>
 * </table>
 */
@SuppressWarnings("unchecked")
public class SBSubject extends StoreBlock {

    private static final long serialVersionUID = 1L;
    
    public Map<String, Object> setSubject(Map<String, Object> sub) {
        String sid = null;
        if (!sub.containsKey("sid")) sub.put("sid", sid = "subject-" + UUID.randomUUID().toString().replace("-", ""));
        else sid = sub.get("sid").toString();
        if (!sub.containsKey("time")) sub.put("time", System.currentTimeMillis());
        if (!sub.containsKey("items")) sub.put("items", new HashMap<String, Map<String, Object>>());
        return (Map<String, Object>) data().put(sid, sub);
    }
    
    public void setSubjectItem(String sid, Map<String, Object> item) {
        if (!data().containsKey(sid)) return;
        Map<String, Object> sub = (Map<String, Object>) data().get(sid);
        
        item.put("sid", sid);
        String siid = null;
        if (!item.containsKey("siid")) item.put("siid", siid = "subject-item-" + UUID.randomUUID().toString().replace("-", ""));
        else siid = item.get("siid").toString();
        if (!item.containsKey("time")) item.put("time", System.currentTimeMillis());
        if (!item.containsKey("pids")) item.put("pids", new LinkedList<Map<String, Object>>());
        Map<String, Object> items = (Map<String, Object>) sub.get("items");
        items.put(siid, item);
    }
    
    public List<Map<String, Object>> delSubject(String... sid) {
        List<Map<String, Object>> list = new LinkedList<>();
        for (String s : sid) {
            Map<String, Object> sub = (Map<String, Object>) data().remove(s);
            if (null != sub) list.add(sub);
        }
        return list;
    }
    
    public List<Map<String, Object>> delSubjectItem(String sid, String... siid) {
        if (!data().containsKey(sid)) return null;
        Map<String, Object> sub = (Map<String, Object>) data().get(sid);
        List<Map<String, Object>> list = new LinkedList<>();
        Map<String, Map<String, Object>> items = (Map<String, Map<String, Object>>) sub.get("items");
        for (String s : siid) {
            Map<String, Object> item = items.remove(s);
            if (null != item) list.add(item);
        }
        return list;
    }
    
    public Map<String, Object> modSubject(Map<String, Object> sub) {
        if (!sub.containsKey("sid")) return null;
        String sid = (String) sub.get("sid");
        if (!data().containsKey(sid)) return null;
        Map<String, Object> sub_old = (Map<String, Object>) data().get(sid);
        sub_old.putAll(sub);
        sub.putAll(sub_old);
        return (Map<String, Object>) data().put(sid, sub);
    }
    
    public List<Map<String, Object>> getSubject(String... sid) {
        if (null != sid && 0 < sid.length) {
            return data().entrySet().parallelStream()
                    .filter(e->{for (String s : sid) if (e.getKey().equals(s)) return true; return false;})
                    .map(e->(Map<String, Object>) e.getValue())
                    .collect(Collectors.toList());
        } else {
            return data().entrySet().parallelStream()
                    .map(e->{
                        Map<String, Object> sub = new HashMap<>((Map<String, Object>) e.getValue());
                        sub.put("items", ((Map<String, Object>) sub.get("items")).size());
                        return sub;
                    })
                    .collect(Collectors.toList());
        }
    }
    
    public List<Map<String, Object>> getSubjectItem(Map<String, Object> args) {
        return data().values().parallelStream()
                .map(sub->(Map<String, Object>) sub)
                .filter(sub->{
                    if (!args.containsKey("sid")) return true;
                    
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
                .map(sub->(Map<String, Object>) sub.get("items"))
                .flatMap(items->items.values().stream())
                .map(item->(Map<String, Object>) item)
                .filter(item->{
                    if (!args.containsKey("name")) return true;
                    
                    String name = args.get("name").toString();
                    if (item.get("name").toString().contains(name)) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.containsKey("gender")) return true;
                    
                    int gender = Integer.parseInt(args.get("gender").toString());
                    if (Integer.parseInt(item.get("gender").toString()) == gender) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.containsKey("birth")) return true;
                    
                    String birth = args.get("birth").toString();
                    if (item.get("birth").toString().contains(birth)) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.containsKey("idno")) return true;
                    
                    String idno = args.get("idno").toString();
                    if (item.get("idno").toString().contains(idno)) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.containsKey("phone")) return true;
                    
                    String phone = args.get("phone").toString();
                    if (item.get("phone").toString().contains(phone)) return true;
                    return false;
                })
                .filter(item->{
                    if (!args.containsKey("addr")) return true;
                    
                    String addr = args.get("addr").toString();
                    if (item.get("addr").toString().contains(addr)) return true;
                    return false;
                })
                .map(item->{
                    item.put("sname", ((Map<String, Object>) data().get(item.get("sid"))).get("name"));
                    return item;
                })
                .collect(Collectors.toList());
    }

}
