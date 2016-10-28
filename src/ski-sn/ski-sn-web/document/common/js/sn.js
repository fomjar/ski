
var sn = {};
sn.ui = {
    mask : function() {
        var div = $('.sn .mask');
        div.appear = function() {
            div.css('opacity', '0');
            div.show();
            div.css('opacity', '.4');
        };
        div.disappear = function() {
            div.css('opacity', '0');
            setTimeout(function() {
                div.hide();
            }, 500);
        };
        return div;
    },
    dialog : function() {
        var div = $('.sn .dialog');
        
        div.appear = function() {
            div.css('opacity', '0');
            div.css('width', '70%');
            div.css('height', '70%');
            sn.ui.mask().appear();
            div.show();
            div.css('opacity', '.9');
            div.css('width', '90%');
            div.css('height', '90%');
        };
        div.disappear = function() {
            div.css('width', '70%');
            div.css('height', '70%');
            div.css('opacity', '0');
            setTimeout(function() {
                div.html('');
                div.hide();
            }, 300);
            sn.ui.mask().disappear();
        };
        return div;
    },
    page : function() {
        var div = $('<div></div>');
        div.addClass('page');
        var pages = {};
        
        div.page_append = function(title, content) {
            pages[title] = content;
            if (0 == div.html().length) {
                div.append('<div>' + title + '</div>');
                div.append(content);
                content.css('opacity', '1');
            }
        };
        div.page_index = function(title) {
            var i = 0;
            for (var t in pages) {
                if (t == title) return i;
                i++;
            }
            return -1;
        };
        div.page_of_index = function(i) {
            var j = 0;
            for (var t in pages) {
                if (j == i) return t;
                j++
            }
            return null;
        };
        div.page_size = function() {
            var i = 0;
            for (var t in pages) i++;
            return i;
        }
        div.page_set = function(title) {
            var t_old = div.page_curr();
            var i_old = div.page_index(t_old);
            var i_new = div.page_index(title);
            if (i_old == i_new) return;
            if (i_old > i_new) { // come in from left
                pages[title].css('opacity', '0');
                pages[title].css('left', '-100%');
                div.append(pages[title]);
                
                setTimeout(function() {
                    pages[t_old].css('left', '100%');
                    pages[t_old].css('opacity', '0');
                    pages[title].css('left', '0');
                    pages[title].css('opacity', '1');
                    div.page_title().css('opacity', '0');
                    setTimeout(function(){pages[t_old].detach();}, 500);
                    setTimeout(function() {
                        div.page_title().text(title);
                        div.page_title().css('opacity', '1');
                    }, 250);
                }, 0);
            } else { // come in from right
                pages[title].css('opacity', '0');
                pages[title].css('left', '100%');
                div.append(pages[title]);
                
                setTimeout(function() {
                    pages[t_old].css('left', '-100%');
                    pages[t_old].css('opacity', '0');
                    pages[title].css('left', '0');
                    pages[title].css('opacity', '1');
                    div.page_title().css('opacity', '0');
                    setTimeout(function(){pages[t_old].detach();}, 500);
                    setTimeout(function() {
                        div.page_title().text(title);
                        div.page_title().css('opacity', '1');
                    }, 250);
                }, 0);
            }
        };
        div.page_title = function() {
            return div.find('>div:nth-child(1)');
        }
        div.page_curr = function() {
            return div.page_title().text();
        };
        div.page_prev = function() {
            var i = div.page_index(div.page_curr());
            if (0 < i) return div.page_of_index(i - 1);
            else return div.page_of_index(0);
        };
        div.page_next = function() {
            var i = div.page_index(div.page_curr());
            if (i >= div.page_size() - 1) return div.page_of_index(div.page_size() - 1);
            else return div.page_of_index(i + 1);
        };
        div.page_to_next = function() {
            div.page_set(div.page_next());
        };
        div.page_to_prev = function() {
            div.page_set(div.page_prev());
        };
        
        return div;
    }
};
sn.geo = {};

(function($) {

fomjar.framework.phase.append('ini', init_event);
fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('dom', build_head);

function init_event() {
    $('body').bind('touchstart', function() {});
    FastClick.attach(document.body);
}

function build_frame() {
    var sn = $('<div></div>');
    sn.addClass('sn');
    var bg = $('<div></div>');
    bg.addClass('bg');
    bg.append("<img src='/res/bg.jpg'/>");
    bg.append('<div></div>');
    var head = $('<div></div>');
    head.addClass('head');
    var body = $('<div></div>');
    body.addClass('body');
    var foot = $('<div></div>');
    foot.addClass('foot');
    var mask = $('<div></div>');
    mask.addClass('mask');
    mask.hide();
    var dialog = $('<div></div>');
    dialog.addClass('dialog');
    dialog.hide();
    
    sn.append([bg, head, body, foot, mask, dialog]);
    $('body').append(sn);
}

function build_head() {
    build_user_cover();
    build_user_state();
}

function build_user_cover() {
    var cover = $('<div></div>');
    cover.addClass('cover');
    cover.append('<div></div>');
    cover.append('<div>登录</div>');
    
    $('.sn .head').append(cover);
    
    cover.bind('click', function() {
        var dialog = sn.ui.dialog();
        dialog.append(create_user_login());
        dialog.appear();
    });
}

function build_user_state() {
    var state_location = $("<div class='state'><img /></div>");
    state_location.addClass('state');
    
    $('.sn .head').append("<div class='state'><img src='/res/state-notify.png' /></div>");
    $('.sn .head').append("<div class='state'><img src='/res/state-nearby.png' /></div>");
    $('.sn .head').append("<div class='state'><img src='/res/state-locate.png' /></div>");
}

function create_user_login() {
    var page = sn.ui.page();
    page.page_append('登录-1', create_user_login_1(page));
//     page.page_append('登录-2', create_user_login_2(page));
    page.page_append('注册-1', create_user_register_1(page));
//     page.page_append('注册-2', create_user_register_2(page));
    return page;
}

function create_user_login_1(page) {
    var div = $('<div></div>');
    div.addClass('page-login-1');
    div.append('<div>登录</div>');
    div.append('<div>输入要登录的账户的手机号码。</div>');
    div.append("<div><input type='text' placeholder='手机号码' ></div>");
    div.append("<div><div class='button'>下一步</div></div>");
    div.append("<div><input type='checkbox' ><label>自动登录</label></div>");
    div.append("<div><label>还没有声呐账户？<label><div class='button'>立即注册</div></div>");
    
    div.find('>div:nth-child(6) .button').bind('click', function() {page.page_set('注册-1');});
    return div;
}

function create_user_register_1(page) {
    var div = $('<div></div>');
    div.addClass('page-register-1');
    div.append('<div>注册</div>');
    div.append('<div>声呐账户会开启很多权益。</div>');
    div.append("<div><input type='text' placeholder='手机号码' ></div>");
    div.append("<div><input type='text' placeholder='4位验证码' ><div class='button'>获取</div></div>");
    div.append("<div><input type='password' placeholder='创建密码' ></div>");
    div.append("<div>选择“下一步”即表示你同意<br/><div class='button'>声呐服务协议</div>。</div>");
    div.append("<div><div class='button'>返回登录</div><div class='button'>下一步</div></div>");
    
    div.find('>div:nth-child(7) .button:nth-child(1)').bind('click', function() {page.page_set('登录-1');});
    return div;
}

function create_user_register_2(page) {
    
}


})(jQuery);
