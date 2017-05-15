package com.ski.frs.web;

import net.sf.json.JSONObject;

public class WebToolkit {

    public static int getIntFromArgs(JSONObject args, String name) {
        if (!args.has(name)) return -1;

        Object obj = args.get(name);
        if (obj instanceof Integer) return (Integer) obj;
        else return Integer.parseInt(obj.toString(), 16);
    }
    
}
