
var url_api = 'http://ski.craftvoid.com/ski-web';

var ISIS = {
    //////////////////////////////// 用户指令 ////////////////////////////////
    /** 发往用户的响应 */
    INST_USER_RESPONSE      : 0x00001001,
    /** 来自用户的请求 */
    INST_USER_REQUEST       : 0x00001002,
    /** 用户订阅/关注 */
    INST_USER_SUBSCRIBE     : 0x00001003,
    /** 用户取消订阅/取消关注 */
    INST_USER_UNSUBSCRIBE   : 0x00001004,
    /** 用户通用命令 */
    INST_USER_COMMAND       : 0x00001005,
    /** 用户跳转界面/网页 */
    INST_USER_VIEW          : 0x00001006,
    /** 用户地理位置 */
    INST_USER_LOCATION      : 0x00001007,
    /** 用户(结果)通知 */
    INST_USER_NOTIFY        : 0x00001008,
    
    //////////////////////////////// 电商指令 ////////////////////////////////
    // QUERY
    /** 查询游戏 */
    INST_ECOM_QUERY_GAME                    : 0x00002001,
    /** 查询游戏账号 */
    INST_ECOM_QUERY_GAME_ACCOUNT            : 0x00002002,
    /** 查询游戏账户下的游戏 */
    INST_ECOM_QUERY_GAME_ACCOUNT_GAME       : 0x00002003,
    /** 查询游戏账户租赁状态 */
    INST_ECOM_QUERY_GAME_ACCOUNT_RENT       : 0x00002004,
    /** 查询渠道账号 */
    INST_ECOM_QUERY_CHANNEL_ACCOUNT         : 0x00002005,
    /** 查询订单 */
    INST_ECOM_QUERY_ORDER                   : 0x00002006,
    /** 查询订单商品 */
    INST_ECOM_QUERY_COMMODITY               : 0x00002007,
    /** 查询游戏租赁价格 */
    INST_ECOM_QUERY_GAME_RENT_PRICE         : 0x00002008,
    /** 查询平台账户 */
    INST_ECOM_QUERY_PLATFORM_ACCOUNT        : 0x00002009,
    /** 查询平台账户与渠道账户间的映射关系 */
    INST_ECOM_QUERY_PLATFORM_ACCOUNT_MAP    : 0x0000200A,
    /** 查询平台用户充值记录 */
    INST_ECOM_QUERY_PLATFORM_ACCOUNT_MONEY  : 0x0000200B,
    /** 查询TAG */
    INST_ECOM_QUERY_TAG                     : 0x0000200C,
    /** 查询工单 */
    INST_ECOM_QUERY_TICKET                  : 0x0000200D,
    /** 查询通知 */
    INST_ECOM_QUERY_NOTIFICATION            : 0x0000200E,
    /** 查询访问记录 */
    INST_ECOM_QUERY_ACCESS_RECORD           : 0x0000200F,
    /** 查询渠道商品 */
    INST_ECOM_QUERY_CHANNEL_COMMODITY       : 0x00002010,
    /** 查询聊天室 */
    INST_ECOM_QUERY_CHATROOM                : 0x00002011,
    /** 查询聊天室成员 */
    INST_ECOM_QUERY_CHATROOM_MEMBER         : 0x00002012,
    /** 查询聊天室消息 */
    INST_ECOM_QUERY_CHATROOM_MESSAGE        : 0x00002013,
    // APPLY
    /** 验证账户、密码等的正确性 */
    INST_ECOM_APPLY_GAME_ACCOUNT_VERIFY     : 0x00002101,
    /** 锁定账户，锁定后无法变更数据 */
    INST_ECOM_APPLY_GAME_ACCOUNT_LOCK       : 0x00002102,
    /** 平台账户合并操作 */
    INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE  : 0x00002103,
    /** 平台账户充值/退款等 */
    INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY  : 0x00002104,
    /** 起租 */
    INST_ECOM_APPLY_RENT_BEGIN              : 0x00002106,
    /** 退租 */
    INST_ECOM_APPLY_RENT_END                : 0x00002107,
    /** 认证 */
    INST_ECOM_APPLY_AUTHORIZE               : 0x00002108,
    /** 制作封面 */
    INST_ECOM_APPLY_MAKE_COVER              : 0x00002109,
    // UPDATE
    /** 更新游戏 */
    INST_ECOM_UPDATE_GAME                   : 0x00002401,
    /** 更新账号 */
    INST_ECOM_UPDATE_GAME_ACCOUNT           : 0x00002402,
    /** 更新游戏账户下的游戏 */
    INST_ECOM_UPDATE_GAME_ACCOUNT_GAME      : 0x00002403,
    /** 更新游戏帐户租赁状态 */
    INST_ECOM_UPDATE_GAME_ACCOUNT_RENT      : 0x00002404,
    /** 更新渠道账户 */
    INST_ECOM_UPDATE_CHANNEL_ACCOUNT        : 0x00002405,
    /** 更新订单 */
    INST_ECOM_UPDATE_ORDER                  : 0x00002406,
    /** 更新订单商品 */
    INST_ECOM_UPDATE_COMMODITY              : 0x00002407,
    /** 更新游戏租赁价格 */
    INST_ECOM_UPDATE_GAME_RENT_PRICE        : 0x00002408,
    /** 更新平台账户 */
    INST_ECOM_UPDATE_PLATFORM_ACCOUNT       : 0x00002409,
    /** 更新平台账户与渠道账户间的映射关系 */
    INST_ECOM_UPDATE_PLATFORM_ACCOUNT_MAP   : 0x0000240A,
    /** 更新TAG */
    INST_ECOM_UPDATE_TAG                    : 0x0000240B,
    /** 删除TAG */
    INST_ECOM_UPDATE_TAG_DEL                : 0x0000240C,
    /** 更新工单 */
    INST_ECOM_UPDATE_TICKET                 : 0x0000240D,
    /** 更新通知 */
    INST_ECOM_UPDATE_NOTIFICATION           : 0x0000240E,
    /** 更新访问记录 */
    INST_ECOM_UPDATE_ACCESS_RECORD          : 0x0000240F,
    /** 更新渠道商品 */
    INST_ECOM_UPDATE_CHANNEL_COMMODITY      : 0x00002410,
    /** 更新聊天室 */
    INST_ECOM_UPDATE_CHATROOM               : 0x00002411,
    /** 更新聊天室成员 */
    INST_ECOM_UPDATE_CHATROOM_MEMBER        : 0x00002412,
    /** 更新聊天室成员(删除) */
    INST_ECOM_UPDATE_CHATROOM_MEMBER_DEL    : 0x00002413,
    /** 更新聊天室消息 */
    INST_ECOM_UPDATE_CHATROOM_MESSAGE       : 0x00002414,
};

function sendto(url, inst, data, cb) {
    data.inst = inst;
    console.log('[request]  ' + data);

    wx.request({
        method  : 'POST',
        url     : url,
        data    : data,
        header  : { 
            'content-type': 'application/json'
        },
        success : function(res) {
            var args = res.data;
            if (undefined == args || undefined == args.code) return;

            console.log('[response] ' + args);
            cb(args.code, args.desc);
        }
    });
}

function send(inst, data, cb) {
    sendto(url_api, inst, data, cb);
}

module.exports = {
    ISIS    : ISIS,
    send    : send,
};
