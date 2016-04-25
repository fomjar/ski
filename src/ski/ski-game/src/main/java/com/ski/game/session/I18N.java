package com.ski.game.session;

import java.util.HashMap;
import java.util.Map;

public class I18N {
    
    private static final Map<String, String> map_zh = new HashMap<String, String>();
    
    public static final String REQUIRE_INSTANCE = "require.instance";
    public static final String PRODUCT_LIST     = "product.list";
    public static final String ACCOUNT          = "account";
    public static final String TYPE_A           = "type.a";
    public static final String TYPE_B           = "type.b";
    public static final String UNKNOWN          = "unknown";
    
    static {
        map_zh.put(REQUIRE_INSTANCE,    "请指定操作实例");
        map_zh.put(PRODUCT_LIST,        "产品清单");
        map_zh.put(ACCOUNT,             "账号");
        map_zh.put(TYPE_A,              "A类");
        map_zh.put(TYPE_B,              "B类");
        map_zh.put(UNKNOWN,             "未知");
    }
    
    public static String zh(String key) {return map_zh.get(key);}

}
