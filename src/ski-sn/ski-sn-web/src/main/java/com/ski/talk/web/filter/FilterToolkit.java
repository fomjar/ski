package com.ski.talk.web.filter;

import net.sf.json.JSONObject;

public class FilterToolkit {

    public static int getIntFromArgs(JSONObject args, String name) {
        if (!args.has(name)) return -1;

        Object obj = args.get(name);
        if (obj instanceof Integer) return (int) obj;
        else return Integer.parseInt(obj.toString(), 16);
    }

}
