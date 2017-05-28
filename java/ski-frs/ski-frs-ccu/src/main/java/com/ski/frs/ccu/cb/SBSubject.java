package com.ski.frs.ccu.cb;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
 * <tr><td>siid     </td><td>主体项编号</td></tr>
 * <tr><td>time     </td><td>创建时间</td></tr>
 * <tr><td>name     </td><td>姓名</td></tr>
 * <tr><td>gender   </td><td>性别：0 - 女，1 - 男</td></tr>
 * <tr><td>birth    </td><td>生日</td></tr>
 * <tr><td>idno     </td><td>身份证号</td></tr>
 * <tr><td>phone    </td><td>电话</td></tr>
 * <tr><td>addr     </td><td>地址</td></tr>
 * </table>
 */
@SuppressWarnings("unchecked")
public class SBSubject extends StoreBlock {

    private static final long serialVersionUID = 1L;
    
    private Map<String, Map<String, Object>> data;
    
    public SBSubject() {data = new HashMap<>();}
    
    public Map<String, Object> setSubject(Map<String, Object> sub) {
        String sid = null;
        if (!sub.containsKey("sid")) sub.put("sid", sid = "subject-" + UUID.randomUUID().toString().replace("-", ""));
        else sid = sub.get("sid").toString();
        if (!sub.containsKey("items")) sub.put("items", new HashMap<String, Map<String, Object>>());
        return data.put(sid, sub);
    }
    
    public void setSubjectItem(String sid, Map<String, Object> item) {
        if (!data.containsKey(sid)) return;
        Map<String, Object> sub = data.get(sid);
        
        String siid = null;
        if (!item.containsKey("siid")) item.put("siid", siid = "subject-item-" + UUID.randomUUID().toString().replace("-", ""));
        else siid = item.get("siid").toString();
        if (!item.containsKey("time")) item.put("time", System.currentTimeMillis());
        if (!item.containsKey("pids")) item.put("pids", new LinkedList<String>());
        Map<String, Object> items = (Map<String, Object>) sub.get("items");
        items.put(siid, item);
    }
    
    public List<Map<String, Object>> delSubject(String... sid) {
        List<Map<String, Object>> list = new LinkedList<>();
        for (String s : sid) {
            Map<String, Object> sub = data.remove(s);
            if (null != sub) list.add(sub);
        }
        return list;
    }
    
    public List<Map<String, Object>> delSubjectItem(String sid, String... siid) {
        if (!data.containsKey(sid)) return null;
        Map<String, Object> sub = data.get(sid);
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
        if (!data.containsKey(sid)) return null;
        Map<String, Object> sub_old = data.get(sid);
        sub_old.putAll(sub);
        sub.putAll(sub_old);
        return data.put(sid, sub);
    }
    
    public List<Map<String, Object>> getSubject(String... sid) {
        if (null != sid && 0 < sid.length) {
            return data.entrySet().parallelStream()
                    .filter(e->{for (String s : sid) if (e.getKey().equals(s)) return true; return false;})
                    .map(e->{
                        Map<String, Object> map = new HashMap<>(e.getValue());
                        map.put("items", ((Map<String, Object>) map.get("items")).size());
                        return map;
                    })
                    .collect(Collectors.toList());
        } else {
            return data.entrySet().parallelStream()
                    .map(e->{
                        Map<String, Object> map = new HashMap<>(e.getValue());
                        map.put("items", ((Map<String, Object>) map.get("items")).size());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
    }
    
    public List<Map<String, Object>> getSubjectItem(String sid) {
        if (!data.containsKey(sid)) return null;
        return new LinkedList<Map<String, Object>>(((Map<String, Map<String, Object>>) ((Map<String, Object>) data.get(sid)).get("items")).values());
    }

}
