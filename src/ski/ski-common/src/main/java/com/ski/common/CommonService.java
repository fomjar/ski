package com.ski.common;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.ski.common.bean.BeanAccessRecord;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanChannelCommodity;
import com.ski.common.bean.BeanChatroom;
import com.ski.common.bean.BeanChatroomMember;
import com.ski.common.bean.BeanChatroomMessage;
import com.ski.common.bean.BeanCommodity;
import com.ski.common.bean.BeanGame;
import com.ski.common.bean.BeanGameAccount;
import com.ski.common.bean.BeanGameAccountGame;
import com.ski.common.bean.BeanGameAccountRent;
import com.ski.common.bean.BeanGameRentPrice;
import com.ski.common.bean.BeanNotification;
import com.ski.common.bean.BeanOrder;
import com.ski.common.bean.BeanPlatformAccount;
import com.ski.common.bean.BeanPlatformAccountMap;
import com.ski.common.bean.BeanPlatformAccountMoney;
import com.ski.common.bean.BeanTag;
import com.ski.common.bean.BeanTicket;

import fomjar.server.FjSender;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CommonService {
    
    private static final Map<Integer, BeanChannelAccount>   cache_channel_account           = new LinkedHashMap<Integer, BeanChannelAccount>();     // caid
    private static final Map<Integer, BeanGame>             cache_game                      = new LinkedHashMap<Integer, BeanGame>();               // gid
    private static final Map<Integer, BeanGameAccount>      cache_game_account              = new LinkedHashMap<Integer, BeanGameAccount>();        // gaid
    private static final Set<BeanGameAccountGame>           cache_game_account_game         = new LinkedHashSet<BeanGameAccountGame>();
    private static final Set<BeanGameAccountRent>           cache_game_account_rent         = new LinkedHashSet<BeanGameAccountRent>();
    private static final Map<String, BeanGameRentPrice>     cache_game_rent_price           = new LinkedHashMap<String, BeanGameRentPrice>();       // gid + type
    private static final Map<Integer, BeanOrder>            cache_order                     = new LinkedHashMap<Integer, BeanOrder>();              // oid
    private static final Map<Integer, BeanPlatformAccount>  cache_platform_account          = new LinkedHashMap<Integer, BeanPlatformAccount>();    // paid
    private static final Set<BeanPlatformAccountMap>        cache_platform_account_map      = new LinkedHashSet<BeanPlatformAccountMap>();
    private static final Set<BeanPlatformAccountMoney>      cache_platform_account_money    = new LinkedHashSet<BeanPlatformAccountMoney>();
    private static final Set<BeanTag>                       cache_tag                       = new LinkedHashSet<BeanTag>();
    private static final Map<Integer, BeanTicket>           cache_ticket                    = new LinkedHashMap<Integer, BeanTicket>();
    private static final Map<Integer, BeanNotification>     cache_notification              = new LinkedHashMap<Integer, BeanNotification>();
    private static final Set<BeanAccessRecord>              cache_access_record             = new LinkedHashSet<BeanAccessRecord>();
    private static final Set<BeanChannelCommodity>          cache_channel_commodity         = new LinkedHashSet<BeanChannelCommodity>();
    private static final Map<Integer, BeanChatroom>         cache_chatroom                  = new LinkedHashMap<Integer, BeanChatroom>();
    private static final Set<BeanChatroomMember>            cache_chatroom_member           = new LinkedHashSet<BeanChatroomMember>();
    private static final Set<BeanChatroomMessage>           cache_chatroom_message          = new LinkedHashSet<BeanChatroomMessage>();
    
    public static final int CHANNEL_TAOBAO = 0;
    public static final int CHANNEL_WECHAT = 1;
    public static final int CHANNEL_ALIPAY = 2;
    
    public static final int GENDER_FEMALE  = 0;
    public static final int GENDER_MALE    = 1;
    public static final int GENDER_UNKNOWN = 2;
    
    public static final int MONEY_CONSUME   = 0;
    public static final int MONEY_CASH      = 1;
    public static final int MONEY_COUPON    = 2;
    
    public static final int RENT_TYPE_A = 0;
    public static final int RENT_TYPE_B = 1;
    
    public static final int RENT_STATE_IDLE = 0;
    public static final int RENT_STATE_RENT = 1;
    public static final int RENT_STATE_LOCK = 2;
    
    public static final int TAG_GAME        = 0;
    public static final int TAG_CHATROOM    = 1;
    
    public static final int TICKET_TYPE_REFUND  = 0;
    public static final int TICKET_TYPE_ADVICE  = 1;
    public static final int TICKET_TYPE_NOTIFY  = 2;
    public static final int TICKET_TYPE_RESERVE = 3;
    public static final int TICKET_TYPE_COMMENT = 4;
    
    public static final int TICKET_STATE_OPEN   = 0;
    public static final int TICKET_STATE_CLOSE  = 1;
    public static final int TICKET_STATE_CANCEL = 2;
    
    public static final int CHATROOM_MESSAGE_TEXT   = 0;
    public static final int CHATROOM_MESSAGE_IMAGE  = 1;
    public static final int CHATROOM_MESSAGE_VOICE  = 2;
    
    public static String createGameAccountPassword() {
        int     len     = 5;            // 密码长度
        Random  random  = new Random();
        int[]   number  = new int[len];
        
        for (int i = 0; i < len; i++) {
            boolean available;
            int     n;
            do {
                available   = true;
                n           = Math.abs(random.nextInt()) % 10;
                // 第一个数字不管
                if (0 == i) break;
                // 不能跟上一个数字相同
                if (n == number[i - 1]) available = false;
                // 不能跟上一个数字连续
                if (1 == Math.abs(n - number[i - 1])) available = false;
            } while (!available);
            
            number[i] = n;
        }
        
        return String.format("vcg%d%d%d%d%d", number[0], number[1], number[2], number[3], number[4]);
    }
    
    public static Set<BeanAccessRecord> getAccessRecordAll() {
        return new LinkedHashSet<BeanAccessRecord>(cache_access_record);
    }
    
    public static List<BeanAccessRecord> getAccessRecordByCaid(int caid) {
        synchronized (cache_access_record) {
            return cache_access_record
                    .stream()
                    .filter(access->access.i_caid == caid)
                    .collect(Collectors.toList());
        }
    }
    
    public static Map<Integer, BeanChannelAccount> getChannelAccountAll() {
        return new LinkedHashMap<Integer, BeanChannelAccount>(cache_channel_account);
    }
    
    public static BeanChannelAccount getChannelAccountByCaid(int caid){
        synchronized (cache_channel_account) {
            return cache_channel_account.get(caid);
        }
    }
    
    public static int getChannelAccountByGaid(int gaid, int type) {
        synchronized (cache_game_account_rent) {
            for (BeanGameAccountRent rent : cache_game_account_rent) {
                if (rent.i_gaid == gaid && rent.i_type == type && RENT_STATE_RENT == rent.i_state) return rent.i_caid; 
            }
            
            return -1; // 没有租赁用户
        }
    }
    
    public static List<BeanChannelAccount> getChannelAccountByPaid(int paid) {
        synchronized (cache_platform_account_map) {
            return cache_platform_account_map
                    .stream()
                    .filter(bean->bean.i_paid == paid)
                    .map(bean->getChannelAccountByCaid(bean.i_caid))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanChannelAccount> getChannelAccountByPaidNChannel(int paid, int channel) {
        synchronized (cache_platform_account_map) {
            return cache_platform_account_map
                    .stream()
                    .filter(bean->bean.i_paid == paid)
                    .filter(bean->getChannelAccountByCaid(bean.i_caid).i_channel == channel)
                    .map(bean->getChannelAccountByCaid(bean.i_caid))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanChannelAccount> getChannelAccountByPhone(String phone) {
        synchronized (cache_channel_account) {
            return cache_channel_account.values()
                    .stream()
                    .filter(account->account.c_phone.equals(phone))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanChannelAccount> getChannelAccountByPhoneNChannel(String phone, int channel) {
        synchronized (cache_channel_account) {
            return cache_channel_account.values()
                    .stream()
                    .filter(account->account.c_phone.equals(phone))
                    .filter(account->account.i_channel == channel)
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanChannelAccount> getChannelAccountByUser(String user) {
        synchronized (cache_channel_account) {
            return cache_channel_account.values()
                    .stream()
                    .filter(account->account.c_user.equals(user))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanChannelAccount> getChannelAccountByUserNChannel(String user, int channel) {
        synchronized (cache_channel_account) {
            return cache_channel_account.values()
                    .stream()
                    .filter(account->account.c_user.equals(user))
                    .filter(account->account.i_channel == channel)
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanChannelAccount> getChannelAccountRelatedAll(int caid) {
        synchronized (cache_platform_account_map) {
            int paid = getPlatformAccountByCaid(caid);
            return cache_platform_account_map
                    .stream()
                    .filter(bean->paid == bean.i_paid)
                    .map(bean->getChannelAccountByCaid(bean.i_caid))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanChannelAccount> getChannelAccountRelatedByCaidNChannel(int caid, int channel) {
        synchronized (cache_platform_account_map) {
            int paid = getPlatformAccountByCaid(caid);
            return cache_platform_account_map
                    .stream()
                    .filter(bean->paid == bean.i_paid)
                    .map(bean->getChannelAccountByCaid(bean.i_caid))
                    .filter(user->user.i_channel == channel)
                    .collect(Collectors.toList());
        }
    }
    
    public static Set<BeanChannelCommodity> getChannelCommodityAll() {
        synchronized (cache_channel_commodity) {
            return new LinkedHashSet<BeanChannelCommodity>(cache_channel_commodity);
        }
    }
    
    public static List<BeanChannelCommodity> getChannelCommodityByCid(int cid) {
        synchronized (cache_channel_commodity) {
            return cache_channel_commodity
                    .stream()
                    .filter(bean->bean.i_cid == cid)
                    .collect(Collectors.toList());
        }
    }
    
    public static BeanChatroom getChatroomByCrid(int crid) {
        synchronized (cache_chatroom) {
            return cache_chatroom.get(crid);
        }
    }
    
    public static List<BeanChatroom> getChatroomByGid(int gid) {
        synchronized (cache_chatroom) {
            return getTagByTypeNTag(TAG_CHATROOM, Integer.toHexString(gid))
                    .stream()
                    .map(tag->cache_chatroom.get(tag.i_instance))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanChatroomMember> getChatroomMemberByCrid(int crid) {
        synchronized (cache_chatroom_member) {
            return cache_chatroom_member
                    .stream()
                    .filter(crm->crm.i_crid == crid)
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanChatroomMessage> getChatroomMessageByCrid(int crid) {
        synchronized (cache_chatroom_message) {
            return cache_chatroom_message
                    .stream()
                    .filter(crm->crm.i_crid == crid)
                    .collect(Collectors.toList());
        }
    }
    
    public static Map<Integer, BeanGameAccount> getGameAccountAll() {
        return new LinkedHashMap<Integer, BeanGameAccount>(cache_game_account);
    }
    
    public static List<BeanGameAccount> getGameAccountByCaid(int caid, int type) {
        synchronized (cache_game_account_rent) {
            return cache_game_account_rent
                    .stream()
                    .filter(rent->rent.i_caid == caid)
                    .filter(rent->rent.i_state == RENT_STATE_RENT)
                    .filter(rent->rent.i_type == type)
                    .map(rent->cache_game_account.get(rent.i_gaid))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanGameAccount> getGameAccountByPaid(int paid, int type) {
        List<BeanGameAccount> accounts = new LinkedList<BeanGameAccount>();
        getChannelAccountByPaid(paid).forEach(u->accounts.addAll(getGameAccountByCaid(u.i_caid, type)));
        return accounts;
    }
    
    public static BeanGameAccount getGameAccountByGaid(int gaid) {
        synchronized (cache_game_account) {
            return cache_game_account.get(gaid);
        }
    }
    
    public static List<BeanGameAccount> getGameAccountByGid(int gid) {
        synchronized (cache_game_account_game) {
            return cache_game_account_game
                    .stream()
                    .filter(gag->gag.i_gid == gid)
                    .map(gag->getGameAccountByGaid(gag.i_gaid))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanGameAccount> getGameAccountByUser(String user) {
        synchronized (cache_game_account) {
            return cache_game_account.values()
                    .stream()
                    .filter(account->account.c_user.equals(user))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanGameAccount> getGameAccountByGidNRentState(int gid, int state, int type) {
        return getGameAccountByGid(gid)
                .stream()
                .filter(ga->getGameAccountRentStateByGaid(ga.i_gaid, type) == state)
                .collect(Collectors.toList());
    }
    
    public static Set<BeanGameAccountGame> getGameAccountGameAll() {
        return new LinkedHashSet<BeanGameAccountGame>(cache_game_account_game);
    }
    
    public static Set<BeanGameAccountRent> getGameAccountRentAll() {
        return new LinkedHashSet<BeanGameAccountRent>(cache_game_account_rent);
    }
    
    public static int getGameAccountRentStateByGaid(int gaid, int type) {
        synchronized (cache_game_account_rent) {
            for (BeanGameAccountRent rent : cache_game_account_rent) {
                if (rent.i_gaid == gaid && rent.i_type == type) {
                    return rent.i_state;
                }
            }
            
            return RENT_STATE_IDLE; // 未记录，则空闲
        }
    }
    
    public static Map<Integer, BeanGame> getGameAll() {
        return new LinkedHashMap<Integer, BeanGame>(cache_game);
    }
    
    public static List<BeanGame> getGameByGaid(int gaid) {
        synchronized (cache_game_account_game) {
            return cache_game_account_game
                    .stream()
                    .filter(gag->gag.i_gaid == gaid)
                    .map(gag->cache_game.get(gag.i_gid))
                    .filter(game->null != game)
                    .collect(Collectors.toList());
        }
    }
    
    public static BeanGame getGameByGid(int gid) {
        synchronized (cache_game) {
            return cache_game.get(gid);
        }
    }
    
    public static List<BeanGame> getGameByTag(String tag) {
        synchronized (cache_game) {
            return cache_game.values()
                    .stream()
                    .filter(game->0 < getTagByTypeInstance(TAG_GAME, game.i_gid)
                                .stream()
                                .filter(t->t.c_tag.toLowerCase().contains(tag.toLowerCase()))
                                .count())
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanGame> getGameByCategory(String... category) {
        synchronized (cache_game) {
            return cache_game.values()
                    .stream()
                    .filter(game->{
                        for (String c : category) {
                            if (game.c_category.toLowerCase().contains(c.toLowerCase())) return true;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanGame> getGameByVendor(String vendor) {
        synchronized (cache_game) {
            return cache_game.values()
                    .stream()
                    .filter(game->game.c_vendor.toLowerCase().contains(vendor.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanGame> getGameByLanguage(String language) {
        synchronized (cache_game) {
            return cache_game.values()
                    .stream()
                    .filter(game->game.c_language.toLowerCase().contains(language.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 
     * @param gaid
     * @param type RENT_TYPE_X
     * @return
     */
    public static float getGameRentPriceByGaid(int gaid, int type) {
        synchronized (cache_game_rent_price) {
            float price = 0.0f;
            for (BeanGame game : getGameByGaid(gaid)) {
                String key = Integer.toHexString(game.i_gid) + type;
                if (cache_game_rent_price.containsKey(key)) price += cache_game_rent_price.get(key).i_price;
            }
            return price;
        }
    }
    
    public static BeanGameRentPrice getGameRentPriceByGid(int gid, int type) {
        synchronized (cache_game_rent_price) {
            return cache_game_rent_price.get(Integer.toHexString(gid) + type);
        }
    }
    
    public static Map<Integer, BeanOrder> getOrderAll() {
        return new LinkedHashMap<Integer, BeanOrder>(cache_order);
    }
    
    public static List<BeanOrder> getOrderByCaid(int caid) {
        synchronized (cache_order) {
            return cache_order.values()
                    .stream()
                    .filter(order->order.i_caid == caid)
                    .collect(Collectors.toList());
        }
    }
    
    public static BeanOrder getOrderByOid(int oid) {
        synchronized (cache_order) {
            return cache_order.get(oid);
        }
    }
    
    public static List<BeanOrder> getOrderByPaid(int paid) {
        synchronized (cache_order) {
            return cache_order.values()
                    .stream()
                    .filter(o->getPlatformAccountByCaid(o.i_caid) == paid)
                    .collect(Collectors.toList());
        }
    }
    
    public static Map<Integer, BeanPlatformAccount> getPlatformAccountAll() {
        return new LinkedHashMap<Integer, BeanPlatformAccount>(cache_platform_account);
    }
    
    public static int getPlatformAccountByCaid(int caid) {
        synchronized (cache_platform_account_map) {
            for (BeanPlatformAccountMap bean : cache_platform_account_map) {
                if (bean.i_caid == caid) return bean.i_paid;
            }
            return -1;
        }
    }
    
    public static int getPlatformAccountByOid(int oid) {
        synchronized (cache_order) {
            return getPlatformAccountByCaid(cache_order.get(oid).i_caid);
        }
    }
    
    public static BeanPlatformAccount getPlatformAccountByPaid(int paid) {
        synchronized (cache_platform_account) {
            return cache_platform_account.get(paid);
        }
    }
    
    public static Set<BeanPlatformAccountMoney> getPlatformAccountMoneyAll() {
        return new LinkedHashSet<BeanPlatformAccountMoney>(cache_platform_account_money);
    }
    
    public static List<BeanPlatformAccountMoney> getPlatformAccountMoneyByPaid(int paid) {
        synchronized (cache_platform_account_money) {
            return cache_platform_account_money
                    .stream()
                    .filter(money->money.i_paid == paid)
                    .collect(Collectors.toList());
        }
    }
    
    public static Set<BeanTag> getTagAll() {
        return new LinkedHashSet<BeanTag>(cache_tag);
    }
    
    public static List<BeanTag> getTagByType(int type) {
        synchronized (cache_tag) {
            return cache_tag
                    .stream()
                    .filter(tag->tag.i_type == type)
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanTag> getTagByTypeInstance(int type, int instance) {
        synchronized (cache_tag) {
            return cache_tag
                    .stream()
                    .filter(tag->tag.i_type == type && tag.i_instance == instance)
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanTag> getTagByTypeNTag(int type, String tag) {
        synchronized (cache_tag) {
            return cache_tag
                    .stream()
                    .filter(t->t.i_type == type && t.c_tag.equals(tag))
                    .collect(Collectors.toList());
        }
    }
    
    public static Map<Integer, BeanTicket> getTicketAll() {
        return new LinkedHashMap<Integer, BeanTicket>(cache_ticket);
    }
    
    public static BeanTicket getTicketByTid(int tid) {
        synchronized (cache_ticket) {
            return cache_ticket.get(tid);
        }
    }
    
    public static List<BeanTicket> getTicketByCaid(int caid) {
        synchronized (cache_ticket) {
            return cache_ticket.values()
                    .stream()
                    .filter(ticket->ticket.i_caid == caid)
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanTicket> getTicketByPaid(int paid) {
        synchronized (cache_ticket) {
            return cache_ticket.values()
                    .stream()
                    .filter(t->isChannelAccountBelongsToPaid(t.i_caid, paid))
                    .collect(Collectors.toList());
        }
    }
    
    public static List<BeanTicket> getTicketByType(int type) {
        synchronized (cache_ticket) {
            return cache_ticket.values()
                    .stream()
                    .filter(ticket->ticket.i_type == type)
                    .collect(Collectors.toList());
        }
    }
    
    public static String getWsiUrl() {
        String host = null;
        int port    = 8080;
        if (null == wsi_host) {
            FjAddress addr = FjServerToolkit.getSlb().getAddress("wsi");
            host = addr.host;
            port = addr.port;
        } else host = wsi_host;
        return String.format("http://%s:%d/ski-wsi", host, port);
    }
    
    public static boolean isChannelAccountRelated(int... caids) {
        int paid = -1;
        for (int caid : caids) {
            int p = getPlatformAccountByCaid(caid);
            if (0 > paid) paid = p;
            else {
                if (paid != p) return false;
            }
        }
        return true;
    }
    
    public static boolean isChannelAccountBelongsToPaid(int caid, int paid) {
        synchronized (cache_platform_account_map) {
            for (BeanPlatformAccountMap map : cache_platform_account_map) {
                if (map.i_caid == caid && map.i_paid == paid) return true;
            }
            return false;
        }
    }
    
    public static boolean isNotificationNotified(int caid, String content) {
        synchronized (cache_notification) {
            for (BeanNotification n : cache_notification.values()) {
                if (n.i_caid == caid
                        && n.c_content.equals(content)) return true;
            }
            return false;
        }
    }
    
    public static boolean isResponseSuccess(FjDscpMessage rsp) {
        if (null == rsp) return false;
        
        if (CommonDefinition.CODE.CODE_SYS_SUCCESS == getResponseCode(rsp)) return true;
        else return false;
    }
    
    public static int getResponseCode(FjDscpMessage rsp) {
        if (null == rsp) return -1;
        
        return rsp.argsToJsonObject().getInt("code");
    }
    
    public static String getResponseDesc(FjDscpMessage rsp) {
        if (null == rsp) return null;
        
        JSONObject args = rsp.argsToJsonObject();
        Object desc = args.get("desc");
        if (desc instanceof JSONArray) return ((JSONArray) desc).getString(0);
        return desc.toString();
    }
    
    public static float prestatementByCommodity(BeanCommodity c) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long begin  = sdf.parse(c.t_begin).getTime();
            long end    = System.currentTimeMillis();
            long millis = end - begin - 20 * 60 * 1000L; // 优惠20分钟
            if (millis <= 0) return 0.00f;
            int hours = (int) Math.ceil((double) millis / 1000 / 60 / 60);  // 向上取整
            int times = (int) Math.ceil(((double) hours) / 12); 
            float price = c.i_price;
            
            if (times < 2) times = 2;                                       // 至少算1天
            if (12 * 24 < hours && hours <= 15 * 24) times = 12 * 24 / 12;  // 12-15天的钱算作12天，倍数为24
            else if (15 * 24 < hours) price *= 0.8;                         // 15天以上打八折
            
            return times * (price / 2) * c.i_count;
        } catch (Exception e) {e.printStackTrace();}
        return 0.00f;
    }
    
    public static float[] prestatementByCaid(int caid) {
        return prestatementByPaid(getPlatformAccountByCaid(caid));
    }
    
    public static float[] prestatementByPaid(int paid) {
        BeanPlatformAccount puser = getPlatformAccountByPaid(paid);
        float cash      = puser.i_cash;
        float coupon    = puser.i_coupon;
        float cost      = 0.00f;
        try {
            cost = CommonService.getOrderByPaid(paid)
                .stream()
                .filter(order->!order.isClose())
                .map(order->{
                    try {
                        return order.commodities.values()
                                .stream()
                                .filter(c->!c.isClose())
                                .map(c->prestatementByCommodity(c))
                                .reduce(0.00f, (cost1, cost2)->(cost1 + cost2))
                                .floatValue();
                    } catch (Exception e) {}
                    return 0.00f;
                })
                .reduce(0.00f, (cost1, cost2)->(cost1 + cost2))
                .floatValue();
        } catch (Exception e) {}
        
        if (cost <= coupon) {
            coupon -= cost;
        } else {
            cash -= (cost - coupon);
            coupon = 0.00f;
        }
        
        return new float[] {cash, coupon};
    }
    
    public static FjDscpMessage send(String report, int inst, JSONObject args) {
        if (null == args) args = new JSONObject();
        
        args.put("report", report);
        args.put("inst", inst);
        FjHttpRequest req = new FjHttpRequest("POST", getWsiUrl(), "application/json", args.toString());
        FjDscpMessage rsp = (FjDscpMessage) FjSender.sendHttpRequest(req);
        return rsp;
    }
    
    public static FjDscpMessage send(String report, int inst, JSONObject args, int timeout) {
        if (null == args) args = new JSONObject();
        
        args.put("report", report);
        args.put("inst", inst);
        FjHttpRequest req = new FjHttpRequest("POST", getWsiUrl(), "application/json", args.toString());
        FjDscpMessage rsp = (FjDscpMessage) FjSender.sendHttpRequest(req, timeout);
        return rsp;
    }
    
    private static String wsi_host = null;
    public static void setWsiHost(String host) {wsi_host = host;}
    
    public static void updateAccessRecord() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_ACCESS_RECORD, null));
        
        synchronized (cache_access_record) {
            cache_access_record.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanAccessRecord bean = new BeanAccessRecord(line);
                    cache_access_record.add(bean);
                }
            }
        }
    }
    
    public static void updateChannelAccount() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_CHANNEL_ACCOUNT, null));
        
        synchronized (cache_channel_account) {
            cache_channel_account.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanChannelAccount bean = new BeanChannelAccount(line);
                    cache_channel_account.put(bean.i_caid, bean);
                }
            }
        }
    }
    
    public static void updateChannelCommodity() {
        updateChannelCommodity(-1);
    }
    
    public static void updateChannelCommodity(int osn) {
        JSONObject args = new JSONObject();
        args.put("osn", osn);
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_CHANNEL_COMMODITY, args));
        
        synchronized (cache_channel_commodity) {
            cache_channel_commodity.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanChannelCommodity bean = new BeanChannelCommodity(line);
                    cache_channel_commodity.add(bean);
                }
            }
        }
    }
    
    public static void updateChatroom() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_CHATROOM, null));
        
        synchronized (cache_chatroom) {
            cache_chatroom.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanChatroom bean = new BeanChatroom(line);
                    cache_chatroom.put(bean.i_crid, bean);
                }
            }
        }
    }
    
    public static void updateChatroomMember() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_CHATROOM_MEMBER, null));
        
        synchronized (cache_chatroom_member) {
            cache_chatroom_member.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanChatroomMember bean = new BeanChatroomMember(line);
                    cache_chatroom_member.add(bean);
                }
            }
        }
    }
    
    public static void updateChatroomMessage() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_CHATROOM_MESSAGE, null));
        
        synchronized (cache_chatroom_message) {
            cache_chatroom_message.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanChatroomMessage bean = new BeanChatroomMessage(line);
                    cache_chatroom_message.add(bean);
                }
            }
        }
    }
    
    private static void updateCommodity() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_COMMODITY, null));
        
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanCommodity bean = new BeanCommodity(line);
                if (cache_order.containsKey(bean.i_oid)) cache_order.get(bean.i_oid).commodities.put(bean.i_csn, bean);
            }
        }
    }
    
    public static void updateGame() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_GAME, null));
        
        synchronized (cache_game) {
            cache_game.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanGame bean = new BeanGame(line);
                    cache_game.put(bean.i_gid, bean);
                }
            }
        }
    }
    
    public static void updateGameAccount() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_GAME_ACCOUNT, null));
        
        synchronized (cache_game_account) {
            cache_game_account.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanGameAccount bean = new BeanGameAccount(line);
                    cache_game_account.put(bean.i_gaid, bean);
                }
            }
        }
    }
    
    public static void updateGameAccountGame() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_GAME_ACCOUNT_GAME, null));
        
        synchronized (cache_game_account_game) {
            cache_game_account_game.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanGameAccountGame pair = new BeanGameAccountGame(line);
                    cache_game_account_game.add(pair);
                }
            }
        }
    }
    
    public static void updateGameAccountRent() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_GAME_ACCOUNT_RENT, null));
        
        synchronized (cache_game_account_rent) {
            cache_game_account_rent.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanGameAccountRent bean = new BeanGameAccountRent(line);
                    cache_game_account_rent.add(bean);
                }
            }
        }
    }
    
    public static void updateGameRentPrice() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_GAME_RENT_PRICE, null));
        
        synchronized (cache_game_rent_price) {
            cache_game_rent_price.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanGameRentPrice bean = new BeanGameRentPrice(line);
                    cache_game_rent_price.put(Integer.toHexString(bean.i_gid) + bean.i_type, bean);
                }
            }
        }
    }
    
    public static void updateNotification() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_NOTIFICATION, null));
        
        synchronized (cache_notification) {
            cache_notification.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanNotification bean = new BeanNotification(line);
                    cache_notification.put(bean.i_nid, bean);
                }
            }
        }
    }
    
    public static void updateOrder() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_ORDER, null));
        
        synchronized (cache_order) {
            cache_order.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanOrder bean = new BeanOrder(line);
                    cache_order.put(bean.i_oid, bean);
                }
                updateCommodity();
            }
        }
    }
    
    public static void updatePlatformAccount() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT, null));
        
        synchronized (cache_platform_account) {
            cache_platform_account.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanPlatformAccount bean = new BeanPlatformAccount(line);
                    cache_platform_account.put(bean.i_paid, bean);
                }
            }
        }
    }
    
    public static void updatePlatformAccountMap() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT_MAP, null));
        
        synchronized (cache_platform_account_map) {
            cache_platform_account_map.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanPlatformAccountMap bean = new BeanPlatformAccountMap(line);
                    cache_platform_account_map.add(bean);
                }
            }
        }
    }
    
    public static void updatePlatformAccountMoney() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT_MONEY, null));
        
        synchronized (cache_platform_account_money) {
            cache_platform_account_money.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanPlatformAccountMoney bean = new BeanPlatformAccountMoney(line);
                    cache_platform_account_money.add(bean);
                }
            }
        }
    }
    
    public static void updateTag() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_TAG, null));
        
        synchronized (cache_tag) {
            cache_tag.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanTag bean = new BeanTag(line);
                    cache_tag.add(bean);
                }
            }
        }
    }
    
    public static void updateTicket() {
        String rsp = getResponseDesc(send("cdb", CommonDefinition.ISIS.INST_ECOM_QUERY_TICKET, null));
        
        synchronized (cache_ticket) {
            cache_ticket.clear();
            if (null != rsp && !"null".equals(rsp)) {
                String[] lines = rsp.split("\n");
                for (String line : lines) {
                    BeanTicket bean = new BeanTicket(line);
                    cache_ticket.put(bean.i_tid, bean);
                }
            }
        }
    }
}
