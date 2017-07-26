
/** 
 * 时间对象的格式化; 
 */  
Date.prototype.format = function(format) {  
    /* 
     * 使用例子:format="yyyy-MM-dd hh:mm:ss"; 
     */  
    var o = {  
        "M+" : this.getMonth() + 1, // month  
        "d+" : this.getDate(), // day  
        "H+" : this.getHours(), // hour  
        "m+" : this.getMinutes(), // minute  
        "s+" : this.getSeconds(), // second  
        "q+" : Math.floor((this.getMonth() + 3) / 3), // quarter  
        "S" : this.getMilliseconds()  
        // millisecond  
    }  
   
    if (/(y+)/.test(format)) {  
        format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4  
                        - RegExp.$1.length));  
    }  
   
    for (var k in o) {  
        if (new RegExp("(" + k + ")").test(format)) {  
            format = format.replace(RegExp.$1, RegExp.$1.length == 1  
                            ? o[k]  
                            : ("00" + o[k]).substr(("" + o[k]).length));  
        }  
    }  
    return format;  
}

function path_var(key) { 
    var reg = new RegExp("(^|&)" + key + "=([^&]*)(&|$)", "i"); 
    var r = window.location.search.substr(1).match(reg); 
    if (r != null) return unescape(r[2]); 
    return null; 
} 

function cookie_set(name,value) {
	var days = 30;
	var exp = new Date();
	exp.setTime(exp.getTime() + days*24*60*60*1000);
	document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();
}

function cookie_get(name) {
	var arr, reg = new RegExp("(^| )"+name+"=([^;]*)(;|$)");
	if (arr = document.cookie.match(reg)) return unescape(arr[2]);
	return null;
}

function cookie_del(name) {
	var exp = new Date();
	exp.setTime(exp.getTime() - 1);
	var cval = getCookie(name);
	if (cval != null) document.cookie = name + "="+cval+";expires="+exp.toGMTString();
}






var new_day = 30;
function article_panel(article) {
    var day = (new Date().getTime() - new Date(article['time.update']).getTime()) / 1000 / 60 / 60 / 24;
    return $("<a class='article-panel' href=\"javascript: window.open('article-view.html?aid=" + article.aid + "');\" ><div class='panel panel-default'><div class='panel-body'>"
        + "<h4>" + article.name
            + (day < new_day ? "<span class='label label-danger'>new</span>" : "")
            + "<small>" + new Date(article['time.create']).format('yyyy/MM/dd HH:mm:ss') + "</small>"
        + "</h4>"
    + "</div></div></a>");
}




