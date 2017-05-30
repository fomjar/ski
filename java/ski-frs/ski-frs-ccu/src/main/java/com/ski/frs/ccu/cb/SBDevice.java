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
 * <tr><td>did  </td><td>设备编号</td></tr>
 * <tr><td>path </td><td>路径</td></tr>
 * <tr><td>time </td><td>创建时间</td></tr>
 * <tr><td>host </td><td>主机地址</td></tr>
 * <tr><td>user </td><td>用户名</td></tr>
 * <tr><td>pass </td><td>密码</td></tr>
 * <tr><td>pids </td><td>图片</td></tr>
 * </table>
 */
@SuppressWarnings("unchecked")
public class SBDevice extends StoreBlock {

    private static final long serialVersionUID = 1L;
    
    public Map<String, Object> setDevice(Map<String, Object> dev) {
        String did = null;
        if (!dev.containsKey("did"))    dev.put("did", did = "device-" + UUID.randomUUID().toString().replace("-", ""));
        else did = dev.get("did").toString();
        if (!dev.containsKey("time"))   dev.put("time", System.currentTimeMillis());
        if (!dev.containsKey("pids"))   dev.put("pids", new LinkedList<String>());
        return (Map<String, Object>) data().put(did, dev);
    }
    
    public List<Map<String, Object>> getDevice(String... did) {
        if (null != did && 0 < did.length) {
            return data().entrySet().parallelStream()
                    .filter(e->{for (String d : did) if (e.getKey().equals(d)) return true; return false;})
                    .map(e->{
                        Map<String, Object> map = new HashMap<>((Map<String, Object>) e.getValue());
                        map.put("pids", ((List<String>) map.remove("pids")).size());
                        return map;
                    })
                    .collect(Collectors.toList());
        } else {
            return data().entrySet().parallelStream()
                    .map(e->{
                        Map<String, Object> map = new HashMap<>((Map<String, Object>) e.getValue());
                        map.put("pids", ((List<String>) map.remove("pids")).size());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
    }
    
    public List<Map<String, Object>> delDevice(String... did) {
        List<Map<String, Object>> list = new LinkedList<>();
        for (String d : did) {
            Map<String, Object> dev = (Map<String, Object>) data().remove(d);
            if (null != dev) list.add(dev);
        }
        return list;
    }
    
}
