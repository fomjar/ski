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
        var utf8    = UTF16ToUTF8(str); // 转成UTF8
        var i = 0; // 遍历索引
        var len = utf8.length;
        var res = [];
        while (i < len) {
            var c1 = utf8.charCodeAt(i++) & 0xFF;
            res.push(table[c1 >> 2]);
            // 需要补2个=
            if (i == len) {
                res.push(table[(c1 & 0x3) << 4]);
                res.push('==');
                break;
            }
            var c2 = utf8.charCodeAt(i++);
            // 需要补1个=
            if (i == len) {
                res.push(table[((c1 & 0x3) << 4) | ((c2 >> 4) & 0x0F)]);
                res.push(table[(c2 & 0x0F) << 2]);
                res.push('=');
                break;
            }
            var c3 = utf8.charCodeAt(i++);
            res.push(table[((c1 & 0x3) << 4) | ((c2 >> 4) & 0x0F)]);
            res.push(table[((c2 & 0x0F) << 2) | ((c3 & 0xC0) >> 6)]);
            res.push(table[c3 & 0x3F]);
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
            code1 = table.indexOf(str.charAt(i++));
            code2 = table.indexOf(str.charAt(i++));
            code3 = table.indexOf(str.charAt(i++));
            code4 = table.indexOf(str.charAt(i++));

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

        return UTF8ToUTF16(res.join(''));
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
    
    time : new Date().getTime(),

    sendto : function(url, data, cb) {
        if (2 == arguments.length) {
            cb = data;
            data = {};
        }
        
        var interval = 200;
        var lasttime = fomjar.net.time;
        var currtime = new Date().getTime();
        var delay = currtime - lasttime >= interval ? 0 : interval - (currtime - lasttime);
        
        setTimeout(function() {
            $.post(url, $.toJSON(data), function(args) {cb(args.code, args.desc)});
        }, delay);
        
        fomjar.net.time = currtime + delay;
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

fomjar.geo = {
    convertor : function() {
        var a    = 6378245.0;
        var ee   = 0.00669342162296594323;
        var x_pi = Math.PI * 3000.0 / 180.0;
        
        var transformLat = function(x, y) {
            var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
            ret += ( 20.0 * Math.sin( 6.0 * x * Math.PI) +  20.0 * Math.sin( 2.0 * x * Math.PI)) * 2.0 / 3.0;
            ret += ( 20.0 * Math.sin(       y * Math.PI) +  40.0 * Math.sin( y / 3.0 * Math.PI)) * 2.0 / 3.0;
            ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320.0 * Math.sin(y / 30.0 * Math.PI)) * 2.0 / 3.0;
            return ret;
        };
        var transformLng = function(x, y) {
            var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
            ret += ( 20.0 * Math.sin( 6.0 * x * Math.PI) +  20.0 * sin( 2.0 * x * Math.PI)) * 2.0 / 3.0;
            ret += ( 20.0 * Math.sin(       x * Math.PI) +  40.0 * sin( x / 3.0 * Math.PI)) * 2.0 / 3.0;
            ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
            return ret;
        };
        
        this.out_china = function(p) {
            if (
                   p.lng < 72.004
                || p.lng > 137.8347
                || p.lat < 0.8293
                || p.lat > 55.8271
            ) return true;
            return false;
        };
        this.earth_mars = function(p) {
            if (out_china(p)) return p;
            
            var dLat = transformLat(p.lng - 105.0, p.lat - 35.0);
            var dLng = transformLng(p.lng - 105.0, p.lat - 35.0);
            var radLat = p.lat / 180.0 * Math.PI;
            var magic = Math.sin(radLat);
            magic = 1 - ee * magic * magic;
            var sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
            dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
            
            return {lat : p.lat + dLat, lng : p.lng + dLng};
        };
        this.mars_baidu = function(p) {
            var x = p.lng;
            var y = p.lat;
            var z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
            var theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
            return {
                lng : z * Math.cos(theta) + 0.0065,
                lat : z * Math.sin(theta) + 0.006
            };
        };
        this.baidu_mars = function(p) {
            var x = p.lng - 0.0065;
            var y = p.lat - 0.006;
            var z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
            var theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
            return {
                lng : z * Math.cos(theta),
                lat : z * Math.sin(theta)
            };
        };
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

