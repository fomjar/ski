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
 * </table>
 */
public class SBDevice extends StoreBlock {

    private static final long serialVersionUID = 1L;
    
    private Map<String, Map<String, Object>> data;
    
    public SBDevice() {data = new HashMap<>();}
    
    public Map<String, Object> setDevice(Map<String, Object> dev) {
        String did = null;
        if (!dev.containsKey("did"))    dev.put("did", did = "device-" + UUID.randomUUID().toString().replace("-", ""));
        else did = dev.get("did").toString();
        if (!dev.containsKey("time"))   dev.put("time", System.currentTimeMillis());
        return data.put(did, dev);
    }
    
    public List<Map<String, Object>> getDevice(String... did) {
        return data.entrySet().parallelStream()
                .filter(e->{for (String d : did) if (e.getKey().equals(d)) return true; return false;})
                .map(e->e.getValue())
                .collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> delDevice(String... did) {
        List<Map<String, Object>> list = new LinkedList<>();
        for (String d : did) {
            Map<String, Object> dev = data.remove(d);
            if (null != dev) list.add(dev);
        }
        return list;
    }
    
}
