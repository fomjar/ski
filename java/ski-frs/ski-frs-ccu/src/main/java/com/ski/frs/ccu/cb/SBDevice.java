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
 * <table>
 * <tr><th>键</th><th>描述</th></tr>
 * <tr><td>did  </td><td>设备编号</td></tr>
 * <tr><td>path </td><td>路径</td></tr>
 * <tr><td>time </td><td>创建时间</td></tr>
 * <tr><td>host </td><td>主机地址</td></tr>
 * <tr><td>user </td><td>用户名</td></tr>
 * <tr><td>pass </td><td>密码</td></tr>
 * <tr><td>pics </td><td>图片</td></tr>
 * </table>
 */
public class SBDevice extends StoreBlock {

    private static final long serialVersionUID = 1L;
    
    public JSONObject setDevice(JSONObject dev) {
        String did = null;
        if (!dev.has("did"))    dev.put("did", did = "device-" + UUID.randomUUID().toString().replace("-", ""));
        else did = dev.get("did").toString();
        if (!dev.has("time"))   dev.put("time", System.currentTimeMillis());
        if (!dev.has("pics"))   dev.put("pics", new JSONArray());
        return (JSONObject) data().put(did, dev);
    }
    
    public List<JSONObject> getDevice(String... did) {
        if (null != did && 0 < did.length) {
            return data().entrySet().parallelStream()
                    .filter(e->{for (String d : did) if (e.getKey().equals(d)) return true; return false;})
                    .map(e->(JSONObject) e.getValue())
                    .collect(Collectors.toList());
        } else {
            return data().entrySet().parallelStream()
                    .map(e->{
                        JSONObject dev = JSONObject.fromObject((JSONObject) e.getValue());
                        dev.put("pics", dev.getJSONArray("pics").size());
                        return dev;
                    })
                    .collect(Collectors.toList());
        }
    }
    
    public List<JSONObject> delDevice(String... did) {
        List<JSONObject> list = new LinkedList<>();
        for (String d : did) {
            JSONObject dev = (JSONObject) data().remove(d);
            if (null != dev) list.add(dev);
        }
        return list;
    }
    
}
