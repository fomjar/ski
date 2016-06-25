package com.ski.omc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.ski.common.SkiCommon;
import com.ski.omc.bean.BeanChannelAccount;
import com.ski.omc.bean.BeanCommodity;
import com.ski.omc.bean.BeanGame;
import com.ski.omc.bean.BeanGameAccount;
import com.ski.omc.bean.BeanGameAccountGame;
import com.ski.omc.bean.BeanGameAccountRent;
import com.ski.omc.bean.BeanGameRentPrice;
import com.ski.omc.bean.BeanOrder;
import com.ski.omc.bean.BeanPlatformAccount;
import com.ski.omc.bean.BeanPlatformAccountMap;

import fomjar.server.FjSender;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Service {
    
    private static String HOST_SKI_WSI = "ski.craftvoid.com";
    public static void setWsiHost(String host) {HOST_SKI_WSI = host;}
    
    public static String getWsiUrl() {return String.format("http://%s:8080/ski-wsi", HOST_SKI_WSI);}
    
    public static final Map<Integer, BeanGame>              map_game                    = new LinkedHashMap<Integer, BeanGame>();               // gid
    public static final Map<Integer, BeanGameAccount>       map_game_account            = new LinkedHashMap<Integer, BeanGameAccount>();        // gaid
    public static final Set<BeanGameAccountGame>            set_game_account_game       = new LinkedHashSet<BeanGameAccountGame>();
    public static final Set<BeanGameAccountRent>            set_game_account_rent       = new LinkedHashSet<BeanGameAccountRent>();
    public static final Map<Integer, BeanChannelAccount>    map_channel_account         = new LinkedHashMap<Integer, BeanChannelAccount>();     // caid
    public static final Map<Integer, BeanOrder>             map_order                   = new LinkedHashMap<Integer, BeanOrder>();              // oid
    public static final Map<String, BeanGameRentPrice>      map_game_rent_price         = new LinkedHashMap<String, BeanGameRentPrice>();       // gid + type
    public static final Map<Integer, BeanPlatformAccount>   map_platform_account        = new LinkedHashMap<Integer, BeanPlatformAccount>();    // paid
    public static final Set<BeanPlatformAccountMap>         set_platform_account_map    = new LinkedHashSet<BeanPlatformAccountMap>();
    
    public static final int USER_TYPE_TAOBAO = 0;
    public static final int USER_TYPE_WECHAT = 1;
    public static final int USER_TYPE_ALIPAY = 2;
    
    public static final int RENT_TYPE_A = 0;
    public static final int RENT_TYPE_B = 1;
    
    public static final int RENT_STATE_IDLE = 0;
    public static final int RENT_STATE_RENT = 1;
    public static final int RENT_STATE_LOCK = 2;
    
    public static final int OPER_TYPE_BUY           = 0;
    public static final int OPER_TYPE_RECHARGE      = 1;
    public static final int OPER_TYPE_RENT_BEGIN    = 2;
    public static final int OPER_TYPE_RENT_END      = 3;
    public static final int OPER_TYPE_RENT_PAUSE    = 4;
    public static final int OPER_TYPE_RENT_RESUME   = 5;
    public static final int OPER_TYPE_RENT_SWAP     = 6;
    public static final int OPER_TYPE_COUPON        = 7;
    
    public static FjDscpMessage send(String report, int inst, JSONObject args) {
        if (null == args) args = new JSONObject();
        
        args.put("report", report);
        args.put("inst", inst);
        System.out.println(">> " + args);
        FjHttpRequest req = new FjHttpRequest("POST", getWsiUrl(), args.toString());
        FjDscpMessage rsp = (FjDscpMessage) FjSender.sendHttpRequest(req);
        System.out.println("<< " + rsp);
        return rsp;
    }
    
    public static boolean isResponseSuccess(FjDscpMessage rsp) {
        if (null == rsp) return false;
        
        JSONObject args = rsp.argsToJsonObject();
        if (0 == args.getInt("code")) return true;
        else return false;
    }
    
    public static String getDescFromResponse(FjDscpMessage rsp) {
        if (null == rsp) return null;
        
        JSONObject args = rsp.argsToJsonObject();
        Object desc = args.get("desc");
        if (desc instanceof JSONArray) return ((JSONArray) desc).getString(0);
        return desc.toString();
    }
    
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    
    public static void doLater(Runnable task) {pool.submit(task);}
    
    public static void updateGame() {
        Service.map_game.clear();
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_GAME, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanGame bean = new BeanGame(line);
                Service.map_game.put(bean.i_gid, bean);
            }
        }
    }
    
    public static void updateGameAccount() {
        Service.map_game_account.clear();
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_GAME_ACCOUNT, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanGameAccount bean = new BeanGameAccount(line);
                Service.map_game_account.put(bean.i_gaid, bean);
            }
        }
    }
    
    public static void updateGameAccountGame() {
        Service.set_game_account_game.clear();
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_GAME_ACCOUNT_GAME, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanGameAccountGame pair = new BeanGameAccountGame(line);
                Service.set_game_account_game.add(pair);
            }
        }
    }
    
    public static void updateChannelAccount() {
        Service.map_channel_account.clear();
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_CHANNEL_ACCOUNT, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanChannelAccount bean = new BeanChannelAccount(line);
                Service.map_channel_account.put(bean.i_caid, bean);
            }
        }
    }
    
    public static void updateGameAccountRent() {
        Service.set_game_account_rent.clear();
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_GAME_ACCOUNT_RENT, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanGameAccountRent bean = new BeanGameAccountRent(line);
                Service.set_game_account_rent.add(bean);
            }
        }
    }
    
    public static void updateOrder() {
        Service.map_order.clear();
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanOrder bean = new BeanOrder(line);
                Service.map_order.put(bean.i_oid, bean);
            }
            updateCommodity();
        }
    }
    
    private static void updateCommodity() {
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_COMMODITY, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanCommodity bean = new BeanCommodity(line);
                if (Service.map_order.containsKey(bean.i_oid)) Service.map_order.get(bean.i_oid).commodities.put(bean.i_csn, bean);
            }
        }
    }
    
    public static void updateGameRentPrice() {
        Service.map_game_rent_price.clear();
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_GAME_RENT_PRICE, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanGameRentPrice bean = new BeanGameRentPrice(line);
                Service.map_game_rent_price.put(Integer.toHexString(bean.i_gid) + bean.i_type, bean);
            }
        }
    }
    
    public static void updatePlatformAccount() {
        Service.map_platform_account.clear();
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanPlatformAccount bean = new BeanPlatformAccount(line);
                Service.map_platform_account.put(bean.i_paid, bean);
            }
        }
    }
    
    public static void updatePlatformAccountMap() {
        Service.set_platform_account_map.clear();
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT_MAP, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanPlatformAccountMap bean = new BeanPlatformAccountMap(line);
                Service.set_platform_account_map.add(bean);
            }
        }
    }
    
    public static int getRentStateByGameAccount(int gaid, int type) {
        for (BeanGameAccountRent rent : set_game_account_rent) {
            if (rent.i_gaid == gaid) {
                if (rent.i_type != type) continue; // 只看对应类型的
                if (rent.i_state == RENT_STATE_IDLE) continue; // 先排除空闲的
                
                return rent.i_state; // 非空闲
            }
        }
        
        return RENT_STATE_IDLE; // 没有非空闲，则空闲
    }
    
    public static int getRentChannelAccountByGameAccount(int gaid, int type) {
        for (BeanGameAccountRent rent : set_game_account_rent) {
            if (rent.i_gaid == gaid && rent.i_type == type && RENT_STATE_RENT == rent.i_state) return rent.i_caid; 
        }
        
        return -1; // 没有租赁用户
    }
    
    public static List<BeanGameAccount> getRentGameAccountByChannelAccount(int caid, int type) {
        return set_game_account_rent
                .stream()
                .filter(rent->rent.i_caid == caid)
                .filter(rent->rent.i_state == RENT_STATE_RENT)
                .filter(rent->rent.i_type == type)
                .map(rent->map_game_account.get(rent.i_gaid))
                .collect(Collectors.toList());
    }
    
    public static List<BeanGame> getGameAccountGames(int gaid) {
        return set_game_account_game
                .stream()
                .filter(gag->gag.i_gaid == gaid)
                .map(gag->map_game.get(gag.i_gid))
                .collect(Collectors.toList());
    }
    
    /**
     * 
     * @param gaid
     * @param type RENT_TYPE_X
     * @return
     */
    public static float getRentPriceByGameAccount(int gaid, int type) {
        float price = 0.0f;
        for (BeanGame game : getGameAccountGames(gaid)) {
            String key = Integer.toHexString(game.i_gid) + type;
            if (map_game_rent_price.containsKey(key)) price += map_game_rent_price.get(key).i_price;
        }
        return price;
    }
    
    public static int getPlatformAccountByChannelAccount(int caid) {
        for (BeanPlatformAccountMap bean : Service.set_platform_account_map) {
            if (bean.i_caid == caid) return bean.i_paid;
        }
        return -1;
    }
    
    public static int getPlatformAccountByOrder(int oid) {
        return getPlatformAccountByChannelAccount(map_order.get(oid).i_caid);
    }
    
    public static List<BeanChannelAccount> getChannelAccountRelated(int caid) {
        int paid = getPlatformAccountByChannelAccount(caid);
        return set_platform_account_map
                .stream()
                .filter(bean->paid == bean.i_paid)
                .map(bean->map_channel_account.get(bean.i_caid))
                .collect(Collectors.toList());
    }
    
    public static List<BeanChannelAccount> getChannelAccountByUserName(String user) {
        return Service.map_channel_account.values().stream().filter(account->account.c_user.equals(user)).collect(Collectors.toList());
    }
    
    public static List<BeanOrder> getOrderByChannelAccount(int caid) {
        return Service.map_order.values().stream().filter(order->order.i_caid == caid).collect(Collectors.toList());
    }
    
    public static List<BeanGameAccount> getGameAccountByUserName(String user) {
        return Service.map_game_account.values().stream().filter(account->account.c_user.equals(user)).collect(Collectors.toList());
    }
    
    public static String createGameAccountPassword() {
        List<Integer> number = new ArrayList<Integer>(20);
        // 密碼中同一個字母不可連續重複 3 次
        for (int i = 0; i <= 9; i++) {
            for (int j = 0; j < 2; j++) {
                number.add(i);
            }
        }
        Random r = new Random();
        return String.format("vcg%d%d%d%d%d",
                number.remove(Math.abs(r.nextInt()) % number.size()),
                number.remove(Math.abs(r.nextInt()) % number.size()),
                number.remove(Math.abs(r.nextInt()) % number.size()),
                number.remove(Math.abs(r.nextInt()) % number.size()),
                number.remove(Math.abs(r.nextInt()) % number.size()));
    }
}
