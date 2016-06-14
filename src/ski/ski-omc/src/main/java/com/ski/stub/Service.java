package com.ski.stub;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.ski.common.SkiCommon;
import com.ski.stub.bean.BeanChannelAccount;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;
import com.ski.stub.bean.BeanGameAccountGame;
import com.ski.stub.bean.BeanGameAccountRent;
import com.ski.stub.bean.BeanGameRentPrice;
import com.ski.stub.bean.BeanOrder;
import com.ski.stub.bean.BeanOrderItem;

import fomjar.server.FjSender;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import net.sf.json.JSONObject;

public class Service {
    
    private static String HOST_SKI_WSI = "www.pan-o.cn";
    public static void setWsiHost(String host) {HOST_SKI_WSI = host;}
    
    public static String getWsiUrl() {return String.format("http://%s:8080/ski-wsi", HOST_SKI_WSI);}
    
    public static final Map<Integer, BeanGame>              map_game                = new LinkedHashMap<Integer, BeanGame>();           // gid
    public static final Map<Integer, BeanGameAccount>       map_game_account        = new LinkedHashMap<Integer, BeanGameAccount>();    // gaid
    public static final Set<BeanGameAccountGame>            set_game_account_game   = new LinkedHashSet<BeanGameAccountGame>();
    public static final Set<BeanGameAccountRent>            set_game_account_rent   = new LinkedHashSet<BeanGameAccountRent>();
    public static final Map<Integer, BeanChannelAccount>    map_channel_account     = new LinkedHashMap<Integer, BeanChannelAccount>(); // caid
    public static final Map<Integer, BeanOrder>             map_order               = new LinkedHashMap<Integer, BeanOrder>();          // oid
    public static final Map<String, BeanGameRentPrice>      map_game_rent_price     = new LinkedHashMap<String, BeanGameRentPrice>();   // gid + type
    
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
        if (0 != args.getInt("code")) return null;
        else return args.getJSONArray("desc").getString(0);
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
            updateOrderItem();
        }
    }
    
    private static void updateOrderItem() {
        String rsp = Service.getDescFromResponse(Service.send("cdb", SkiCommon.ISIS.INST_ECOM_QUERY_ORDER_ITEM, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanOrderItem bean = new BeanOrderItem(line);
                if (Service.map_order.containsKey(bean.i_oid)) Service.map_order.get(bean.i_oid).order_items.put(bean.i_oisn, bean);
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
    
    public static int getGameAccountCurrentRentState(int gaid, int type) {
        for (BeanGameAccountRent rent : set_game_account_rent) {
            if (rent.i_gaid == gaid) {
                if (rent.i_type != type) continue; // 只看对应类型的
                if (rent.i_state == RENT_STATE_IDLE) continue; // 先排除空闲的
                
                return rent.i_state; // 非空闲
            }
        }
        
        return RENT_STATE_IDLE; // 没有非空闲，则空闲
    }
    
    public static int getGameAccountCurrentRentUser(int gaid, int type) {
        for (BeanGameAccountRent rent : set_game_account_rent) {
            if (rent.i_gaid == gaid && rent.i_type == type) {
                if (RENT_STATE_RENT == rent.i_state) return rent.i_caid; 
                else return -1;
            }
        }
        
        return -1; // 没有租赁用户
    }
    
    public static List<BeanGame> getGameAccountGames(int gaid) {
        return set_game_account_game
                .stream()
                .filter(gag->{return gag.i_gaid == gaid;})
                .map(gag->{return map_game.get(gag.i_gid);})
                .collect(Collectors.toList());
    }
    
    /**
     * 
     * @param gaid
     * @param type RENT_TYPE_X
     * @return
     */
    public static float getGameAccountRentPrice(int gaid, int type) {
        float price = 0.0f;
        for (BeanGame game : getGameAccountGames(gaid)) {
            String key = Integer.toHexString(game.i_gid) + type;
            if (map_game_rent_price.containsKey(key)) price += map_game_rent_price.get(key).i_price;
        }
        return price;
    }
}
