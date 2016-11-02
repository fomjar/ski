
var sn = {};
sn.ui = {
    mask : function() {
        var div = $('.sn .mask');
        div.appear = function() {
            div.css('opacity', '0');
            setTimeout(function() {
                div.show();
                div.css('opacity', '.4');
            }, 0);
        };
        div.disappear = function() {
            setTimeout(function() {
                div.css('opacity', '0');
            }, 0);
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
            div.css(        'transform', 'translate(-50%, -50%) scale(.8, .8) translateZ(0)');
            div.css('-webkit-transform', 'translate(-50%, -50%) scale(.8, .8) translateZ(0)');
            sn.ui.mask().appear();
            setTimeout(function() {
                div.show();
                div.css('opacity', '.9');
                div.css(        'transform', 'translate(-50%, -50%) scale(1, 1) translateZ(0)');
                div.css('-webkit-transform', 'translate(-50%, -50%) scale(1, 1) translateZ(0)');
            }, 0);
        };
        div.disappear = function() {
            setTimeout(function() {
                div.css(        'transform', 'translate(-50%, -50%) scale(.8, .8) translateZ(0)');
                div.css('-webkit-transform', 'translate(-50%, -50%) scale(.8, .8) translateZ(0)');
                div.css('opacity', '0');
            }, 0);
            sn.ui.mask().disappear();
            setTimeout(function() {
                div.html('');
                div.hide();
            }, 300);
        };
        div.addClose = function(title, onClose) {
            var close = $('<div>' + title + '</div>');
            close.addClass('button');
            close.addClass('close');
            
            if (null != onClose) {
                close.bind('click', function() {
                    div.disappear();
                    onClose();
                });
            } else {
                close.bind('click', function() {div.disappear();});
            }
            div.append(close);
        };
        div.removeClose = function() {
            div.find('.close').remove();
        };
        div.shake = function() {
            div.css(        'animation', 'dialog-shake .5s');
            div.css('-webkit-animation', 'dialog-shake .5s');
            setTimeout(function() {
                div.css(        'animation', '');
                div.css('-webkit-animation', '');
            }, 500);
        }
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
                if (content.onappear) content.onappear();
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
                    setTimeout(function(){pages[t_old].detach();}, 300);
                    setTimeout(function() {
                        div.page_title().text(title);
                        div.page_title().css('opacity', '1');
                    }, 150);
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
                    setTimeout(function(){pages[t_old].detach();}, 300);
                    setTimeout(function() {
                        div.page_title().text(title);
                        div.page_title().css('opacity', '1');
                    }, 150);
                }, 0);
            }
            if (pages[title].onappear)    pages[title].onappear();
            if (pages[t_old].ondisappear) pages[t_old].ondisappear();
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
    },
    state : function(i) {
        var s = null;
        if ('number' == typeof i) s = $('.sn .head .state:nth-child('+(i+1)+')');
        else s = i;
        s.flash = function() {
            s.css(        'animation', 'state-flash 2s');
            s.css('-webkit-animation', 'state-flash 2s');
            setTimeout(function() {
                s.css(        'animation', '');
                s.css('-webkit-animation', '');
            }, 2000);
        };
        s.isopen = function() {
            return s.width() != s.height();
        };
        s.open = function() {
            s.css('width', s.width() * 1.2 + s.find('>div').outerWidth(true) + 'px');
            s.find('>div').css('opacity', '1');
        };
        s.close = function() {
            s.css('width', s.height() + 'px');
            s.find('>div').css('opacity', '0');
        };
        return s;
    }
};


(function($) {

fomjar.framework.phase.append('ini', init_event);
fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('dom', build_head);
fomjar.framework.phase.append('dom', build_foot);

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
}

function build_user_cover() {
    var cover = $('<div></div>');
    cover.addClass('cover');
    cover.append('<div><img /></div>');
    cover.append('<div>登录 / 注册</div>');
    
    $('.sn .head').append(cover);
    
    var login = function() {
        var dialog = sn.ui.dialog();
        dialog.addClose('取消');
        dialog.append(create_user_login(dialog));
        dialog.appear();
    };
    
    if (ski.token) {
        user_login_automatic(function() {}, function() {cover.bind('click', login);});
    } else {
        cover.bind('click', login);
    }
}

function build_user_state() {

    $('.sn .head').append("<div class='state'><img src='/res/state-locate.png' /><div></div></div>");
    $('.sn .head').append("<div class='state'><img src='/res/state-nearby.png' /><div></div></div>");
    $('.sn .head').append("<div class='state'><img src='/res/state-notify.png' /><div></div></div>");
    
    var states = $('.sn .head .state');
    $.each(states, function(i, state) {
        state = sn.ui.state($(state));
        state.bind('click', function() {
            if (!state.isopen()) state.open();
            else state.close();
        });
    });
}

function build_foot() {
    var send = $('<div></div>');
    send.addClass('send');
    
    $('.sn .foot').append(send);
    
    send.bind('click', function() {
        var dialog = sn.ui.dialog();
        dialog.append(create_sending(dialog));
        dialog.appear();
    });
}

function create_sending(dialog) {
    dialog.addClass('dialog-sending');
    
    var div = $('<div></div>');
    div.addClass('sending');
    
    div.append("<div><textarea rows='3' placeholder='想法 / 问询 / 活动 / 段子'></textarea></div>");
    div.append("<div><div class='button'>取消</div><div class='button button-default'>发送</div></div>");
    
    var div_tex = div.find('>*:nth-child(1) textarea');
    var div_can = div.find('>*:nth-child(2) .button:nth-child(1)');
    var div_sen = div.find('>*:nth-child(2) .button:nth-child(2)');
    
    div_can.bind('click', function() {dialog.disappear();});
    div_sen.bind('click', function() {
        var text = div_tex.val();
        if (0 == text.length) {
            dialog.shake();
            return;
        }
        if (/^ +$/.test(text)) {
            dialog.shake();
            return;
        }
        fomjar.net.send(ski.ISIS.INST_UPDATE_MESSAGE, {
            uid     : ski.uid,
            coosys  : 1,
            lat     : ski.user.location.point.lat,
            lng     : ski.user.location.point.lng,
            text    : text
        }, function(code, desc) {
            if (0 == code) {
                dialog.disappear();
            } else {
                dialog.shake();
            }
        });
    });
    
    return div;
}

function geowatch() {
    var run = function(r){
        if (this.getStatus() == BMAP_STATUS_SUCCESS){
            var p = r.point;
            fomjar.net.send(ski.ISIS.INST_UPDATE_USER_STATE, {
                uid         : ski.uid,
                state       : 1,
                terminal    : 1,
                location    : p.lat + ':' + p.lng
            }, function(code, desc) {});
            new BMap.Geocoder().getLocation(p, function(rs) {
                var addr = rs.addressComponents.street + rs.addressComponents.streetNumber;
                if (0 < rs.surroundingPois.length) {
                    addr = rs.surroundingPois[0].title;
                }
                /*
                Object =
                    address: "中影国际影城南京雨花台南站店"
                    addressComponents: Object
                        city: "南京市"
                        district: "雨花台区"
                        province: "江苏省"
                        street: "明城大道"
                        streetNumber: ""
                    “Object”原型
                    business: "宁南"
                    point: H
                        lat: 31.98444
                        lng: 118.803924
                    “H”原型
                    surroundingPois: Array (4)
                        0 Object
                            Ui: "休闲娱乐"
                            address: "南京市雨花台区玉兰路99号(明发商业广场1幢B6区4层17室)"
                            city: "南京市"
                            eu: Array (1)
                                0 "休闲娱乐"
                            “Array”原型
                            phoneNumber: null
                            point: H
                                lat: 31.984822
                                lng: 118.804075
                            “H”原型
                            postcode: null
                            title: "中影国际影城南京雨花台南站店"
                            type: 0
                            uid: "9abe5786376f2dd98bfc06ef"
                        “Object”原型
                        1 {title: "永辉超市(雨花店)", uid: "7a443c7ec83b4ccd2fe67bb8", point: H, city: "南京市", Ui: "购物", …}
                        2 {title: "南京易居智能科技有限公司", uid: "3a46dbe174d7478203ec0106", point: H, city: "南京市", Ui: "公司企业", …}
                        3 {title: "德居欣舒适家居体验中心", uid: "f270eba5dda323d682288ae4", point: H, city: "南京市", Ui: "购物", …}
                    “Array”原型
                    “Object”原型
                */
                if (addr != ski.user.location.address) {
                    var state_locate = sn.ui.state(1);
                    state_locate.find('>div').text(addr);
                    state_locate.find('>div').css('width', addr.length + 'em');
                    state_locate.flash();
                }
                ski.user.location = rs;
                ski.user.location.address = addr;
            });
        } else {}
    };
    setTimeout(function() {new BMap.Geolocation().getCurrentPosition(run);}, 5000);
    setInterval(function() {new BMap.Geolocation().getCurrentPosition(run);}, 1000 * 30);
}
    
function user_login_manually(phone, pass, success, failure) {
    fomjar.net.send(ski.ISIS.INST_APPLY_AUTHORIZE, {phone : phone, pass : pass, terminal : 1}, function(code, desc) {
        if (0 != code) {
            if (failure) failure(code, desc);
            return;
        }
        
        fomjar.util.cookie('token', desc.token, 365);
        fomjar.util.cookie('uid',   desc.uid,   365);
        ski.token = desc.token;
        ski.uid   = desc.uid;
        ski.user  = desc;
        $('.sn .head .cover >div:nth-child(1) img').attr('src', desc.cover);
        $('.sn .head .cover >div:nth-child(2)').text(desc.name);
        $('.sn .head .cover').unbind('click');
        $('.sn .foot').css('bottom', '0');
        
        build_user_state();
        geowatch();
        
        if (success) success();
    });
}

function user_login_automatic(success, failure) {
   fomjar.net.send(ski.ISIS.INST_APPLY_AUTHORIZE, {uid : ski.uid, token : ski.token, terminal : 1}, function(code, desc) {
        if (0 != code) {
            if (failure) failure(code, desc);
            return;
        }
        fomjar.util.cookie('token', desc.token, 365);
        fomjar.util.cookie('uid',   desc.uid,   365);
        ski.token = desc.token;
        ski.uid   = desc.uid;
        ski.user  = desc;
        $('.sn .head .cover >div:nth-child(1) img').attr('src', desc.cover);
        $('.sn .head .cover >div:nth-child(2)').text(desc.name);
        $('.sn .head .cover').unbind('click');
        
        build_user_state();
        geowatch();
        
        if (success) success();
    });
}

function create_user_login(dialog) {
    var page = sn.ui.page();
    page.page_append('登录-1',   create_user_login_1(dialog, page));
    page.page_append('注册-1',   create_user_register_1(dialog, page));
    page.page_append('注册-2',   create_user_register_2(dialog, page));
    page.page_append('注册-成功', create_user_register_done(dialog));
    return page;
}

function create_user_login_1(dialog, page) {
    var div = $('<div></div>');
    div.addClass('page-login-1');
    div.append('<div>登录</div>');
    div.append('<div></div>');
    div.append('<div>输入要登录的账户的手机号码。</div>');
    div.append("<div><input type='text' placeholder='手机号码' ></div>");
    div.append("<div><input type='password' placeholder='密码' ></div>");
    div.append("<div><div class='button button-default'>登录</div></div>");
    div.append("<div><label>还没有声呐账户？<label><div class='button'>立即注册</div></div>");
    
    var div_err = div.find('>div:nth-child(2)');
    var div_pho = div.find('>div:nth-child(4) input');
    var div_pas = div.find('>div:nth-child(5) input');
    var div_log = div.find('>div:nth-child(6) .button');
    var div_reg = div.find('>div:nth-child(7) .button');
    
    div_log.bind('click', function() {
        var phone = div_pho.val();
        var pass  = div_pas.val();
        var error = null;
        if (error = check_phone(phone)) {
            dialog.shake();
            div_err.text(error);
            div_err.show();
            return;
        }
        if (0 == pass.length) {
            dialog.shake();
            div_err.text("请输入密码");
            div_err.show();
            return;
        }
        user_login_manually(phone, pass, function() {
            dialog.disappear();
        }, function(code, desc) {
            dialog.shake();
            div_err.text(desc);
            div_err.show();
        });
    });
    div_reg.bind('click', function() {
        page.page_set('注册-1');
    });
    return div;
}

var user_register = {};

function create_user_register_1(dialog, page) {
    var div = $('<div></div>');
    div.addClass('page-register-1');
    div.append('<div>注册</div>');
    div.append("<div></div>");
    div.append('<div>声呐账户会开启很多权益。</div>');
    div.append("<div><input type='text' placeholder='手机号码' ></div>");
    div.append("<div><input type='text' placeholder='4位验证码' ><div class='button'>获取</div></div>");
    div.append("<div><input type='password' placeholder='创建密码' ></div>");
    div.append("<div>选择“下一步”即表示您同意<br/><div class='button'>声呐服务协议</div>。</div>");
    div.append("<div><div class='button'>返回登录</div><div class='button button-default'>下一步</div></div>");
    
    var div_err = div.find('>div:nth-child(2)');
    var div_pho = div.find('>div:nth-child(4) input');
    var div_vco = div.find('>div:nth-child(5) input');
    var div_get = div.find('>div:nth-child(5) .button');
    var div_pas = div.find('>div:nth-child(6) input');
    var div_bac = div.find('>div:nth-child(8) .button:nth-child(1)');
    var div_nex = div.find('>div:nth-child(8) .button:nth-child(2)');
    
    div_get.bind('click', function() {
        var phone = div_pho.val();
        var error = null;
        if (error = check_phone(phone)) {
            dialog.shake();
            div_err.text(error);
            div_err.show();
            return;
        }
        
        div_err.hide();
        div_get.css('color', 'gray');
        fomjar.net.send(ski.ISIS.INST_APPLY_VERIFY, {type : 'phone', phone : phone}, function(code, desc) {
            if (0 == code) {
                var t = 60;
                var i = setInterval(function() {
                    div_get.text('('+t+')');
                    t--;
                    if (t < 0) {
                        clearInterval(i);
                        div_get.css('color', 'blue');
                        div_get.text('获取');
                    }
                }, 1000);
            } else {
            dialog.shake();
                div_err.text(desc);
                div_err.show();
                div_get.css('color', 'blue');
            }
        });
    });
    div_bac.bind('click', function() {page.page_set('登录-1');});
    div_nex.bind('click', function() {
        var phone = div_pho.val();
        var error = null;
        if (error = check_phone(phone)) {
            dialog.shake();
            div_err.text(error);
            div_err.show();
            return;
        }
        var vcode = div_vco.val();
        if (0 == vcode.length) {
            dialog.shake();
            div_err.text('验证码不能为空');
            div_err.show();
            return;
        }
        var pass = div_pas.val();
        if (0 == pass.length) {
            dialog.shake();
            div_err.text('密码不能为空');
            div_err.show();
            return;
        }
        if (/'|"/.test(pass)) {
            dialog.shake();
            div_err.text("密码不能包含“\"”(英文双引号)或“'”(英文单引号)");
            div_err.show();
            return;
        }
        
        div_err.hide();
        fomjar.net.send(ski.ISIS.INST_APPLY_VERIFY, {type : 'phone', phone : phone, vcode : vcode}, function(code, desc) {
            if (0 == code) {
                user_register.phone = phone;
                user_register.vcode = vcode;
                user_register.pass  = pass;
                page.page_to_next();
            } else {
                dialog.shake();
                div_err.text(desc);
                div_err.show();
            }
        });
    });
    return div;
}

function check_phone(phone) {
    if (0 == phone.length) {
        return '电话不能为空';
    }
    if(!(/^1[3|4|5|7|8]\d{9}$/.test(phone))) {
        return '电话号码不合法';
    }
    return null;
}

function create_user_register_2(dialog, page) {
    var div = $('<div></div>');
    div.addClass('page-register-2');
    div.append('<div>个人信息</div>');
    div.append('<div></div>');
    div.append("<div><img /><input type='file' accept='image/*'></div>");
    div.append("<div><label>姓名</label><input type='text' placeholder='您的姓名'></div>")
    div.append("<div>别担心，这些信息以后还能修改。</div>")
    div.append("<div><div class='button'>上一步</div><div class='button button-default'>提交</div></div>");
    
    var div_err = div.find('>div:nth-child(2)');
    var div_img = div.find('>div:nth-child(3) img');
    var div_cho = div.find('>div:nth-child(3) input');
    var div_nam = div.find('>div:nth-child(4) input');
    var div_bac = div.find('>div:nth-child(6) .button:nth-child(1)');
    var div_sub = div.find('>div:nth-child(6) .button:nth-child(2)');
    
    div_cho.bind('change', function(e) {
        var files = e.target.files || e.dataTransfer.files;
        if (!files || !files[0]) return;
        
        var file = files[0];
        if (file.size > 1024 * 1024 * 2) {
            dialog.shake();
            div_err.text('图片不能大于2M');
            div_err.show();
            return;
        }
        
        div_err.hide();
        var reader = new FileReader();
        reader.onload = function(e1) {
            div_img.attr('src', e1.target.result);
            user_register.cover = e1.target.result;
        };
        reader.readAsDataURL(file);
    });
    div_bac.bind('click', function() {page.page_to_prev();});
    div_sub.bind('click', function() {
        var name = div_nam.val();
        if (0 == name.length) {
            dialog.shake();
            div_err.text('请输入您的姓名');
            div_err.show();
            return;
        }
        if (/'|"/.test(name)) {
            dialog.shake();
            div_err.text("姓名不能包含“\"”(英文双引号)或“'”(英文单引号)");
            div_err.show();
            return;
        }
        
        user_register.name = name;
        
        div_err.hide();
        fomjar.net.send(ski.ISIS.INST_UPDATE_USER, {
            phone : user_register.phone,
            vcode : user_register.vcode,
            pass  : user_register.pass,
            cover : user_register.cover,
            name  : user_register.name
        }, function(code, desc) {
            if (0 == code) {
                user_login_manually(user_register.phone, user_register.pass, function() {
                    page.page_to_next();
                }, function(code, desc) {
                    dialog.shake();
                    div_err.text(desc);
                    div_err.show();
                });
            } else {
                dialog.shake();
                div_err.text(desc);
                div_err.show();
            }
        });
    });
    return div;
}

function create_user_register_done(dialog) {
    var div = $('<div></div>');
    div.addClass('page-register-done');
    div.append('<div>注册成功</div>');
    div.append('<div>欢迎您加入声呐</div>');
    div.append("<div><div class='button button-default'>开启声呐之旅</div></div>");
    
    var div_begin = div.find('>div:nth-child(3) .button');
    div_begin.bind('click', function() {
        dialog.disappear();
    });
    
    div.onappear = function() {dialog.removeClose();};
    div.ondisappear = function() {dialog.addClose();};
    
    return div;
}


})(jQuery);
