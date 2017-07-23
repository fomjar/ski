package com.ski.frs.ccu.cb;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ski.frs.isis.ISIS;

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
 * <table>
 * <tr><th>键</th><th>描述</th></tr>
 * <tr><td>pid  </td><td>图片编号（一个图片，要么来自于设备、要么来自于主体项）</td></tr>
 * <tr><td>name </td><td>名称</td></tr>
 * <tr><td>time </td><td>创建时间</td></tr>
 * <tr><td>type </td><td>类型：0 - 人物，1 - 汽车</td></tr>
 * <tr><td>size </td><td>尺寸：0 - 大图(全图)，1 - 中图(半身)，2 - 小图(头像)</td></tr>
 * <tr><td>path </td><td>真实路径</td></tr>
 * <tr><td>fv   </td><td>特征向量</td></tr>
 * <tr><td>dpath</td><td>设备虚拟路径（如果从属于设备）</td></tr>
 * <tr><td>gender</td><td>性别：0 - 女，1 - 男，2 - 未识别</td></tr>
 * <tr><td>age  </td><td>年龄</td></tr>
 * <tr><td>hat  </td><td>帽子</td></tr>
 * <tr><td>glass</td><td>眼镜</td></tr>
 * <tr><td>mask </td><td>口罩</td></tr>
 * <tr><td>color</td><td>上衣颜色</td></tr>
 * <tr><td>nation</td><td>民族</td></tr>
 * </table>
 */
public class SBPicture extends StoreBlock {
    
    private static final long serialVersionUID = 1L;
    
    public JSONObject setPicture(JSONObject pic) {
        String pid = null;
        if (!pic.has("pid"))    pic.put("pid", pid = "picture-" + UUID.randomUUID().toString().replace("-", ""));
        else pid = pic.getString("pid");
        if (!pic.has("time"))   pic.put("time", System.currentTimeMillis());
        data().put(pid, pic);
        return pic;
    }
    
    public List<JSONObject> getPicture(JSONObject args) {
        return data().entrySet().parallelStream()
                .map(e->(JSONObject) e.getValue())
                .filter(p->p.getInt("size") == ISIS.FIELD_PIC_SIZE_SMALL)
                .filter(p->{
                    if (!args.containsKey("type")) return true;
                    if (!p.has("type")) return false;
                    if (p.getInt("type") == args.getInt("type")) return true;
                    return false;
                })
                .filter(p->{
                    if (!args.has("dids")) return true;
                    if (!p.has("did")) return false;
                    
                    JSONArray array = args.getJSONArray("dids");
                    List<String> dids = new LinkedList<>();
                    for (int i = 0; i < array.size(); i++) dids.add(array.getString(i));
                    String did = p.getString("did");
                    for (String d : dids) if (d.equals(did)) return true;
                    return false;
                })
                .filter(p->{
                    if (!args.has("time-from")) return true;
                    
                    if (args.getLong("time-from") <= p.getLong("time")) return true;
                    return false;
                })
                .sorted((p1, p2)->{
                    if (!args.has("time-from")) return 0;
                    
                    return (int) (p1.getLong("time") - p2.getLong("time"));
                })
                .filter(p->{
                    if (!args.containsKey("gender")) return true;
                    if (!p.has("gender")) return false;
                    if (p.getInt("gender") == args.getInt("gender")) return true;
                    return false;
                })
                .filter(p->{
                    if (!args.containsKey("age")) return true;
                    if (!p.has("age")) return false;
                    if (p.getInt("age") == args.getInt("age")) return true;
                    return false;
                })
                .filter(p->{
                    if (!args.containsKey("hat")) return true;
                    if (!p.has("hat")) return false;
                    if (p.getInt("hat") == args.getInt("hat")) return true;
                    return false;
                })
                .filter(p->{
                    if (!args.containsKey("glass")) return true;
                    if (!p.has("glass")) return false;
                    if (p.getInt("glass") == args.getInt("glass")) return true;
                    return false;
                })
                .filter(p->{
                    if (!args.containsKey("mask")) return true;
                    if (!p.has("mask")) return false;
                    if (p.getInt("mask") == args.getInt("mask")) return true;
                    return false;
                })
                .filter(p->{
                    if (!args.containsKey("color-up")) return true;
                    if (!p.containsKey("color-up")) return false;
                    
                    int colora = args.getInt("color-up");
                    int colorp = p.getInt("color-up");
                    if (0 == colorp) return false;
                    
                    for (int i = 0; i < 32; i++) {
                        int c = colora & (1 << i);
                        if (0 == c) continue;
                        if (0 == (colorp & c)) return false;
                    }
                    return true;
                })
                .filter(p->{
                    if (!args.containsKey("color-down")) return true;
                    if (!p.containsKey("color-down")) return false;
                    
                    int colora = args.getInt("color-down");
                    int colorp = p.getInt("color-down");
                    if (0 == colorp) return false;
                    
                    for (int i = 0; i < 32; i++) {
                        int c = colora & (1 << i);
                        if (0 == c) continue;
                        if (0 == (colorp & c)) return false;
                    }
                    return true;
                })
                .filter(p->{
                    if (!args.containsKey("transport")) return true;
                    if (!p.has("transport")) return false;
                    if (p.getInt("transport") == args.getInt("transport")) return true;
                    return false;
                })
                .filter(p->{
                    if (!args.containsKey("nation")) return true;
                    if (!p.has("nation")) return false;
                    if (p.getInt("nation") == args.getInt("nation")) return true;
                    return false;
                })
                .filter(p->{
                    if (!args.has("fv") || !args.has("min") || !args.has("max")) return true;
                    if (!p.containsKey("fv")) return false;
                    
                    JSONArray array = args.getJSONArray("fv");
                    double[] fva = new double[array.size()];
                    for (int i = 0; i < array.size(); i++) fva[i] = array.getDouble(i);
                    double min = args.getDouble("min");
                    double max = args.getDouble("max");
                    
                    array = p.getJSONArray("fv");
                    double[] fvp = new double[array.size()];
                    for (int i = 0; i < array.size(); i++) fvp[i] = array.getDouble(i);
                    double tv = transvection(fva, fvp);
                    p.put("tv", tv);
                    if (min <= tv && tv <= max) return true;
                    return false;
                })
                .sorted((p1, p2)->{
                    if (!p2.has("tv") && !p1.has("tv")) return 0;
                    if (!p2.has("tv")) return 1;
                    if (!p1.has("tv")) return -1;
                    return (int) (p2.getDouble("tv") * 100000 - p1.getDouble("tv") * 100000);
                })
                .collect(Collectors.toList());
    }
    private static double transvection(double[] v1, double[] v2) {
        double tv = 0;
        for (int i = 0; i < Math.min(v1.length, v2.length); i++) tv += v1[i] * v2[i];
        return tv;
    }
    public List<JSONObject> delPicture(String... pid) {
        List<JSONObject> list = new LinkedList<>();
        for (String p : pid) {
            JSONObject pic = (JSONObject) data().remove(p);
            if (null != pic) list.add(pic);
        }
        return list;
    }
}
