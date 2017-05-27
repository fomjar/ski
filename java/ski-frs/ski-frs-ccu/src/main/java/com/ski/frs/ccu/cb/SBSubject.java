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
public class SBSubject extends StoreBlock {

    private static final long serialVersionUID = 1L;
    
    private Map<String, Map<String, Object>> data;
    
    public SBSubject() {data = new HashMap<>();}
    
    public Map<String, Object> setSubject(Map<String, Object> sub) {
        String sid = null;
        if (!sub.containsKey("sid")) sub.put("sid", sid = UUID.randomUUID().toString().replace("-", ""));
        else sid = sub.get("sid").toString();
        if (!sub.containsKey("items")) sub.put("items", new HashMap<>());
        return data.put(sid, sub);
    }
    
    @SuppressWarnings("unchecked")
    public void addSubjectItem(String sid, Map<String, Object> item) {
        if (!data.containsKey(sid)) return;
        Map<String, Object> sub = data.get(sid);
        
        String siid = null;
        if (!item.containsKey("siid")) item.put("siid", siid = UUID.randomUUID().toString().replace("-", ""));
        else siid = item.get("siid").toString();
        if (!item.containsKey("time")) item.put("time", System.currentTimeMillis());
        Map<String, Object> items = (Map<String, Object>) sub.get("items");
        items.put(siid, item);
    }
    
    public List<Map<String, Object>> getSubject(String... sid) {
        return data.entrySet().parallelStream()
                .filter(e->{for (String s : sid) if (e.getKey().equals(s)) return true; return false;})
                .map(e->e.getValue())
                .collect(Collectors.toList());
    }
    public List<Map<String, Object>> delSubject(String... sid) {
        List<Map<String, Object>> list = new LinkedList<>();
        for (String s : sid) {
            Map<String, Object> sub = data.remove(s);
            if (null != sub) list.add(sub);
        }
        return list;
    }

}
