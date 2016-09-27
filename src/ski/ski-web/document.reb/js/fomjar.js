define('fomjar', ['jquery', 'jquery_cookie', 'jquery_json'],
function($) {

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

return {
    util : {
        title : function(title) {
            switch (arguments.length) {
            case 0: {
                return document.title;
            }
            case 1: {
                var title_old = document.title;
    		    var $body = $('body');
    		    document.title = title;
    		    // hack在微信等webview中无法修改document.title的情况
    		    var $iframe = $('<iframe src="/favicon.ico"></iframe>').on('load', function() {
    		    	setTimeout(function() {
    		    		$iframe.off('load').remove();
    		    	}, 0)
    		    }).appendTo($body);
                return title_old;
            }
            }
        },

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
        }, // end fomjar.util.base64

        urlpar : function() {
            var params = {};
            var arrays = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
            for(var i = 0; i < arrays.length; i++) {
                var kv = arrays[i].split('=');
                params[kv[0]] = decodeURIComponent(kv[1]);
            }
            return params;
        } // end fomjar.util.urlpar
    }, // end fomjar.util

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
    }, // end fomjar.cookie

    net : {
        api : function(url) {
            var key = '/ski-web';
            if (null == url) return key;
            else return key + url;
        },

        sendto : function(url, data, callback) {
            $.post(url, $.toJSON(data),

            function(args){
                if (undefined == args.code || undefined == args.desc) callback(args);
                else callback(args.code, args.desc);
            });
        },

        send : function(inst, data, callback) {
            if (1 >= arguments.length) return;

            if (2 == arguments.length) {
                callback = data;
                data = {};
            }
            if ('number' == typeof(inst)) data.inst = inst;
            if ('string' == typeof(inst)) data.inst = parseInt(inst, 16);
        
            this.sendto(this.api(), data, callback);
        }
    } // end fomjar.net

}; // end return
});


