var fomjar = {};
fomjar.util = {};
fomjar.util.base64 = function() {
    // 转码表
    var table = [
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O' ,'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '/'
    ];

    var UTF16ToUTF8 = function(str) {
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
    };

    var UTF8ToUTF16 = function(str) {
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
    };

    this.encode = function(str) {
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
    };

    this.decode = function(str) {
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
    };

};


fomjar.util.queue = function() {
    var q = [];
    
    this.push = function(options) {
        var args = [];
        if (undefined != options.args) args.push(options.args);
        q.push({
            meth    : options.meth,
            args    : args,
            cb      : options.cb
        });
        return this;
    };
    
    this.apply = function() {
        if (0 == q.length) return;

        var queue = this;
        var e = q.shift();
        if ("Array" == e.cb.constructor.name) {
            $.each(e.cb, function(i, c) {
                e.args.push(function() {
                    c.apply(this, arguments);
                    queue.apply();
                });
            });
        } else {
            e.args.push(function() {
                e.cb.apply(this, arguments);
                queue.apply();
            });
        }
        e.meth.apply(this, e.args);
    };
};

fomjar.util.args = (function() {
    var params = {};
    var arrays = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < arrays.length; i++) {
        var kv = arrays[i].split('=');
        params[kv[0]] = decodeURIComponent(kv[1]);
    }
    return params;
})();

fomjar.util.cookie = function(key, val, domain, path, expires)  {
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
    case 3: {
        var origin = $.cookie(key);
        expires = domain;
        $.cookie(key, val, {path: "/", expires : expires});
        return origin;
    }
    case 5: {
        var origin = $.cookie(key);
        $.cookie(key, val, {domain : domain, path : path, expires : expires});
        return origin;
    }
    }
};

fomjar.util.title = function(t) {
    var $body = $('body');
    document.title = t;
    // hack在微信等webview中无法修改document.title的情况
    var $iframe = $('<iframe src="/favicon.ico"></iframe>').on('load', function() {
        setTimeout(function() {
            $iframe.off('load').remove();
        }, 0)
    }).appendTo($body);
};

fomjar.net = {

    api : '/ski-web',

    sendto : function(url, data, cb) {
        if (2 == arguments.length) {
            cb = data;
            data = {};
        }
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
        fomjar.net.sendto(fomjar.net.api, data, cb);
    }
};

fomjar.framework = {};
fomjar.framework.phase = {
    boot : false,

    ini : [],
    dom : [],
    ren : [],

    append : function(p, f) {
        this[p].push(f);

        if (!this.boot) {
            this.boot = true;

            var phase = this;
            $(function() {
                $.each(phase.ini, function(i, f) {return f(i);});
                $.each(phase.dom, function(i, f) {return f(i);});
                $.each(phase.ren, function(i, f) {return f(i);});
            });
        }
        return this;
    }
};

// prototype
Date.prototype.format = function (fmt) {
    var o = {
        "M+" : this.getMonth() + 1,     //月份
        "d+" : this.getDate(),          //日
        "H+" : this.getHours(),         //小时
        "m+" : this.getMinutes(),       //分
        "s+" : this.getSeconds(),       //秒
        "q+" : Math.floor((this.getMonth() + 3) / 3), //季度
        "S"  : this.getMilliseconds()   //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    }
    return fmt;
};

