var fomjar = {};

fomjar.util = {
    base64 : {
        // 转码表  
        table : [  
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',  
                'I', 'J', 'K', 'L', 'M', 'N', 'O' ,'P',  
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',  
                'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',  
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',  
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v',  
                'w', 'x', 'y', 'z', '0', '1', '2', '3',  
                '4', '5', '6', '7', '8', '9', '+', '/' 
        ],  
        UTF16ToUTF8 : function(str) {  
            var res = [], len = str.length;  
            for (var i = 0; i < len; i++) {  
                var code = str.charCodeAt(i);  
                if (code > 0x0000 && code <= 0x007F) {  
                    // 单字节，这里并不考虑0x0000，因为它是空字节  
                    // U+00000000 – U+0000007F  0xxxxxxx  
                    res.push(str.charAt(i));  
                } else if (code >= 0x0080 && code <= 0x07FF) {  
                    // 双字节  
                    // U+00000080 – U+000007FF  110xxxxx 10xxxxxx  
                    // 110xxxxx  
                    var byte1 = 0xC0 | ((code >> 6) & 0x1F);  
                    // 10xxxxxx  
                    var byte2 = 0x80 | (code & 0x3F);  
                    res.push(  
                        String.fromCharCode(byte1),   
                        String.fromCharCode(byte2)  
                    );  
                } else if (code >= 0x0800 && code <= 0xFFFF) {  
                    // 三字节  
                    // U+00000800 – U+0000FFFF  1110xxxx 10xxxxxx 10xxxxxx  
                    // 1110xxxx  
                    var byte1 = 0xE0 | ((code >> 12) & 0x0F);  
                    // 10xxxxxx  
                    var byte2 = 0x80 | ((code >> 6) & 0x3F);  
                    // 10xxxxxx  
                    var byte3 = 0x80 | (code & 0x3F);  
                    res.push(  
                        String.fromCharCode(byte1),   
                        String.fromCharCode(byte2),   
                        String.fromCharCode(byte3)  
                    );  
                } else if (code >= 0x00010000 && code <= 0x001FFFFF) {  
                    // 四字节  
                    // U+00010000 – U+001FFFFF  11110xxx 10xxxxxx 10xxxxxx 10xxxxxx  
                } else if (code >= 0x00200000 && code <= 0x03FFFFFF) {  
                    // 五字节  
                    // U+00200000 – U+03FFFFFF  111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx  
                } else /** if (code >= 0x04000000 && code <= 0x7FFFFFFF)*/ {  
                    // 六字节  
                    // U+04000000 – U+7FFFFFFF  1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx  
                }  
            }  
     
            return res.join('');  
        },  
        UTF8ToUTF16 : function(str) {  
            var res = [], len = str.length;  
            var i = 0;  
            for (var i = 0; i < len; i++) {  
                var code = str.charCodeAt(i);  
                // 对第一个字节进行判断  
                if (((code >> 7) & 0xFF) == 0x0) {  
                    // 单字节  
                    // 0xxxxxxx  
                    res.push(str.charAt(i));  
                } else if (((code >> 5) & 0xFF) == 0x6) {  
                    // 双字节  
                    // 110xxxxx 10xxxxxx  
                    var code2 = str.charCodeAt(++i);  
                    var byte1 = (code & 0x1F) << 6;  
                    var byte2 = code2 & 0x3F;  
                    var utf16 = byte1 | byte2;  
                    res.push(String.fromCharCode(utf16));  
                } else if (((code >> 4) & 0xFF) == 0xE) {  
                    // 三字节  
                    // 1110xxxx 10xxxxxx 10xxxxxx  
                    var code2 = str.charCodeAt(++i);  
                    var code3 = str.charCodeAt(++i);  
                    var byte1 = (code << 4) | ((code2 >> 2) & 0x0F);  
                    var byte2 = ((code2 & 0x03) << 6) | (code3 & 0x3F);  
                    utf16 = ((byte1 & 0x00FF) << 8) | byte2  
                    res.push(String.fromCharCode(utf16));  
                } else if (((code >> 3) & 0xFF) == 0x1E) {  
                    // 四字节  
                    // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx  
                } else if (((code >> 2) & 0xFF) == 0x3E) {  
                    // 五字节  
                    // 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx  
                } else /** if (((code >> 1) & 0xFF) == 0x7E)*/ {  
                    // 六字节  
                    // 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx  
                }  
            }  
     
            return res.join('');  
        },  
        encode : function(str) {  
            if (!str) {  
                return '';  
            }  
            var utf8    = this.UTF16ToUTF8(str); // 转成UTF8  
            var i = 0; // 遍历索引  
            var len = utf8.length;  
            var res = [];  
            while (i < len) {  
                var c1 = utf8.charCodeAt(i++) & 0xFF;  
                res.push(this.table[c1 >> 2]);  
                // 需要补2个=  
                if (i == len) {  
                    res.push(this.table[(c1 & 0x3) << 4]);  
                    res.push('==');  
                    break;  
                }  
                var c2 = utf8.charCodeAt(i++);  
                // 需要补1个=  
                if (i == len) {  
                    res.push(this.table[((c1 & 0x3) << 4) | ((c2 >> 4) & 0x0F)]);  
                    res.push(this.table[(c2 & 0x0F) << 2]);  
                    res.push('=');  
                    break;  
                }  
                var c3 = utf8.charCodeAt(i++);  
                res.push(this.table[((c1 & 0x3) << 4) | ((c2 >> 4) & 0x0F)]);  
                res.push(this.table[((c2 & 0x0F) << 2) | ((c3 & 0xC0) >> 6)]);  
                res.push(this.table[c3 & 0x3F]);  
            }  
     
            return res.join('');  
        },  
        decode : function(str) {  
            if (!str) {  
                return '';  
            }  
     
            var len = str.length;  
            var i   = 0;  
            var res = [];  
     
            while (i < len) {  
                code1 = this.table.indexOf(str.charAt(i++));  
                code2 = this.table.indexOf(str.charAt(i++));  
                code3 = this.table.indexOf(str.charAt(i++));  
                code4 = this.table.indexOf(str.charAt(i++));  
     
                c1 = (code1 << 2) | (code2 >> 4);  
                c2 = ((code2 & 0xF) << 4) | (code3 >> 2);  
                c3 = ((code3 & 0x3) << 6) | code4;  
     
                res.push(String.fromCharCode(c1));  
     
                if (code3 != 64) {  
                    res.push(String.fromCharCode(c2));  
                }  
                if (code4 != 64) {  
                    res.push(String.fromCharCode(c3));  
                }  
     
            }  
     
            return this.UTF8ToUTF16(res.join(''));  
        }  
    },
    args : function() {
        var params = {};
        var arrays = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
        for(var i = 0; i < arrays.length; i++) {
            var kv = arrays[i].split('=');
            params[kv[0]] = decodeURIComponent(kv[1]);
        }
        return params;
    },
    cookie : function(key, val, domain, path, expires)  {
        switch (arguments.length) {
        case 0:
            return $.cookie();
        case 1:
            return $.cookie(key);
        case 2: {
            var origin = $.cookie(key);
            $.cookie(key, val, {path: "/"});
            return origin;
        }
        case 5: {
            var origin = $.cookie(key);
            $.cookie(key, val, {domain : domain, path : path, expires : expires});
            return origin;
        }
        }
    },
    user : function() {
        return this.cookie('user');
    },
    title : function(t) {
        var $body = $('body');
        document.title = t;
        // hack在微信等webview中无法修改document.title的情况
        var $iframe = $('<iframe src="/favicon.ico"></iframe>').on('load', function() {
            setTimeout(function() {
                $iframe.off('load').remove();
            }, 0)
        }).appendTo($body);
    }
};

fomjar.net = {
    ISIS : {
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
    },
    api : function(inst) {
        if (null == inst) return '/ski-web';

        switch (typeof inst) {
        case 'number'   : return '/ski-web?inst=' + inst.toString(16);
        default         : return '/ski-web?inst=' + inst.toString();
        }
    },
    sendto : function(url, data, cb) {
        $.post(url, $.toJSON(data), function(args) {cb(args.code, args.desc)});
    },
    send : function(inst, data, cb) {
        if (2 == arguments.length) {
            cb = data;
            data = {};
        }
        switch (typeof inst) {
        case 'number'   : data.inst = inst; break;
        default         : data.inst = parseInt(inst.toString(), 16); break;
        }
        this.sendto(this.api(), data, cb);
    }
};

fomjar.framework = {
    phase : {
        boot : false,

        ini : [],
        dom : [],
        ren : [],
    
        append : function(p, f) {
            this[p].push(f);

            if (!this.boot) {
                this.boot = true;

                var p = this;
                $(function() {
                    $.each(p.ini, function(i, f) {return f(i);});
                    $.each(p.dom, function(i, f) {return f(i);});
                    $.each(p.ren, function(i, f) {return f(i);});
                });
            }
        }
    }
};


// prototype
Date.prototype.format = function (fmt) { //author: meizz 
    var o = {
        "M+" : this.getMonth() + 1,  //月份 
        "d+" : this.getDate(),       //日 
        "H+" : this.getHours(),      //小时 
        "m+" : this.getMinutes(),    //分 
        "s+" : this.getSeconds(),    //秒 
        "q+" : Math.floor((this.getMonth() + 3) / 3), //季度 
        "S"  : this.getMilliseconds() //毫秒 
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    }
    return fmt;
};

