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
 * <table>
 * <tr><th>键</th><th>描述</th></tr>
 * <tr><td>mid  </td><td>布控任务编号</td></tr>
 * <tr><td>time </td><td>创建时间</td></tr>
 * <tr><td>devs </td><td>监控设备</td></tr>
 * <tr><td>subs </td><td>对比人像库</td></tr>
 * <tr><td>tv   </td><td>对比阈值</td></tr>
 * <tr><td>logs </td><td>布控日志</td></tr>
 * </table>
 * 
 * logs
 * <table>
 * <tr><th>键</th><th>描述</th></tr>
 * <tr><td>mid  </td><td>布控任务编号</td></tr>
 * <tr><td>lid  </td><td>日志编号</td></tr>
 * <tr><td>time </td><td>创建时间</td></tr>
 * <tr><td>pic_dev </td><td>设备图片</td></tr>
 * <tr><td>pic_sub </td><td>人像库图片</td></tr>
 * <tr><td>tv   </td><td>相似度</td></tr>
 * </table>
 */
@SuppressWarnings("unchecked")
public class SBMonitor extends StoreBlock {
    
    private static final long serialVersionUID = 1L;
    
    public JSONObject setMonitor(JSONObject mon) {
        String mid = null;
        if (!mon.has("mid"))    mon.put("mid", mid = "monitor-" + UUID.randomUUID().toString().replace("-", ""));
        else mid = mon.getString("mid");
        if (!mon.has("time"))   mon.put("time", System.currentTimeMillis());
        if (!mon.has("logs"))   mon.put("logs", new JSONArray());
        return (JSONObject) data().put(mid, mon);
    }
    
    public List<JSONObject> getMonitor(String... mids) {
        return data().entrySet().parallelStream()
                .map(e->(JSONObject) e.getValue())
                .filter(m->{
                    if (null == mids || 0 == mids.length) return true;
                    
                    for (String mid : mids) if (mid.equals(m.getString("mid"))) return true;
                    return false;
                })
                .map(m->JSONObject.fromObject(m))
                .map(m->{
                    m.put("devs", new LinkedList<Object>(m.getJSONArray("devs")).stream()
                            .map(dev->JSONObject.fromObject(dev))
                            .map(dev->{
                                dev.put("pics", dev.getJSONArray("pics").size());
                                return dev;
                            })
                            .collect(Collectors.toList()));
                    return m;
                })
                .map(m->{
                    JSONArray subs = m.getJSONArray("subs");
                    JSONArray subs_new = new JSONArray();
                    for (int i = 0; i < subs.size(); i++) {
                        JSONObject sub = subs.getJSONObject(i);
                        JSONObject sub_new = JSONObject.fromObject(sub);
                        sub_new.put("items", sub_new.getJSONObject("items").size());
                        subs_new.add(sub_new);
                    }
                    m.put("subs", subs_new);
                    return m;
                })
                .collect(Collectors.toList());
    }
    
    public List<JSONObject> delMonitor(String... mid) {
        List<JSONObject> list = new LinkedList<>();
        for (String m : mid) {
            JSONObject mon = (JSONObject) data().remove(m);
            if (null != mon) list.add(mon);
        }
        return list;
    }
}
