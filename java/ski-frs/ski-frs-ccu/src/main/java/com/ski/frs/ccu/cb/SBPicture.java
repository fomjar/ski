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
 * <table>
 * <tr><th>键</th><th>描述</th></tr>
 * <tr><td>pid  </td><td>图片编号（一个图片，要么来自于设备、要么来自于主体项）</td></tr>
 * <tr><td>name </td><td>名称</td></tr>
 * <tr><td>time </td><td>创建时间</td></tr>
 * <tr><td>type </td><td>类型：0 - 人物，1 - 汽车</td></tr>
 * <tr><td>size </td><td>尺寸：0 - 大图(全图)，1 - 中图(半身)，2 - 小图(头像)</td></tr>
 * <tr><td>path </td><td>真实路径</td></tr>
 * </table>
 */
public class SBPicture extends StoreBlock {
    
    private static final long serialVersionUID = 1L;
    
    private Map<String, Map<String, Object>> data;
    
    public SBPicture() {data = new HashMap<>();}
    
    public Map<String, Object> setPicture(Map<String, Object> pic) {
        String pid = null;
        if (!pic.containsKey("pid"))    pic.put("pid", pid = "picture-" + UUID.randomUUID().toString().replace("-", ""));
        else pid = pic.get("pid").toString();
        if (!pic.containsKey("time"))   pic.put("time", System.currentTimeMillis());
        return data.put(pid, pic);
    }
    public List<Map<String, Object>> getPicture(String... pid) {
        return data.entrySet().parallelStream()
                .filter(e->{for (String p : pid) if (e.getKey().equals(p)) return true; return false;})
                .map(e->e.getValue())
                .collect(Collectors.toList());
    }
    public List<Map<String, Object>> getPicture(float[] fv, float min, float max) {
        return data.values().parallelStream()
                .filter(p->{
                    if (!p.containsKey("fv")) return false;
                    float tv = transvection(fv, (float[]) p.get("fv"));
                    p.put("tv", tv);
                    if (min <= tv && tv >= max) return true;
                    return false;})
                .collect(Collectors.toList());
    }
    private static float transvection(float[] v1, float[] v2) {
        float tv = 0;
        for (int i = 0; i < Math.min(v1.length, v2.length); i++) tv += v1[i] * v2[i];
        return tv;
    }
    public List<Map<String, Object>> delPicture(String... pid) {
        List<Map<String, Object>> list = new LinkedList<>();
        for (String p : pid) {
            Map<String, Object> pic = data.remove(p);
            if (null != pic) list.add(pic);
        }
        return list;
    }
}
