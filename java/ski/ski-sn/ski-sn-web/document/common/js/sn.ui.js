
(function($) {

sn.ui = {};

sn.ui.mask = function() {
    if (!sn.ui._mask) {
        var div = $('.sn .mask');
        div.appear = function() {
            div.removeClass('mask-appear');
            setTimeout(function() {
                div.show();
                div.addClass('mask-appear');
            }, 0);
        };
        div.disappear = function() {
            setTimeout(function() {
                div.removeClass('mask-appear');
            }, 0);
            setTimeout(function() {
                div.hide();
            }, 200);
        };
        var pd = function(e) {e.preventDefault();};
        div.bind('click',       pd);
        div.bind('touchstart',  pd);
        div.bind('touchmove',   pd);
        div.bind('touchend',    pd);
        
        sn.ui._mask = div;
    }
    return sn.ui._mask;
};


sn.ui.dialog = function() {
    if (!sn.ui._dialog) {
        var div = $('.sn .dialog');
        div.content = div.find('.content');
        div.action = div.find('.action');
        
        div.action.add = function(data) {
            var d = $('<div></div>');
            d.addClass('button');
            d.append(data);
            div.action.append(d);
            return d;
        };
        div.action.add_default = function(data) {
            var d = div.action.add(data);
            d.addClass('button-default');
            return d;
        };
        div.action.clear = function(data) {div.action.children().remove();};
        
        div.appear = function() {
            sn.ui.mask().appear();
            div.removeClass('dialog-appear');
            div.show();
            div.scrollTop(0);
            setTimeout(function() {
                div.addClass('dialog-appear');
            }, 0);
            setTimeout(function() {
                var d = null;
                if (0 < div.find('input:nth-child(1)').length) d = div.find('input')[0];
                else if (0 < div.find('textarea:nth-child(1)').length) d = div.find('textarea')[0];
                if (d) d.focus();
            }, 200);
        };
        div.disappear = function() {
            setTimeout(function() {
                div.removeClass('dialog-appear');
            }, 0);
            sn.ui.mask().disappear();
            setTimeout(function() {
                div.content.children().detach();
                div.action.clear();
                div.remove_close();
                div.attr('class', '');
                div.attr('style', '');
                div.hide();
                div.addClass('dialog');
            }, 200);
        };
        div.add_close = function(title, cb) {
            if (0 < div.find('.close').length) return;
            
            var close = $('<div>' + title + '</div>');
            close.addClass('button');
            close.addClass('close');
            
            if (null != cb) {
                close.bind('click', function() {
                    div.disappear();
                    cb();
                });
            } else {
                close.bind('click', function() {div.disappear();});
            }
            div.append(close);
        };
        div.remove_close = function() {
            div.find('.close').remove();
        };
        div.shake = function() {
            div.removeClass('dialog-shake');
            setTimeout(function() {
                div.addClass('dialog-shake');
            }, 0);
        };
        div.h1 = function(data) {
            var d = $('<div></div>');
            d.addClass('h1');
            d.append(data);
            return d;
        };
        div.h2 = function(data) {
            var d = $('<div></div>');
            d.addClass('h2');
            d.append(data);
            return d;
        };
        div.h3 = function(data) {
            var d = $('<div></div>');
            d.addClass('h3');
            d.append(data);
            return d;
        };
        div.p1 = function(data) {
            var d = $('<div></div>');
            d.addClass('p1');
            d.append(data);
            return d;
        };
        div.p2 = function(data) {
            var d = $('<div></div>');
            d.addClass('p2');
            d.append(data);
            return d;
        };
        div.t1 = function(data) {
            var d = $('<div></div>');
            d.addClass('t1');
            d.append(data);
            return d;
        };
        div.t2 = function(data) {
            var d = $('<div></div>');
            d.addClass('t2');
            d.append(data);
            return d;
        };
        sn.ui._dialog = div;
    }
    return sn.ui._dialog;
};


sn.ui.toast = function(text, timeout) {
    if (!timeout) timeout = 1500;
    
    var div = $('.sn .toast');
    div.text(text);
    div.hide();
    div.removeClass('toast-appear');
    div.show();
    div.addClass('toast-appear');
    setTimeout(function() {
        div.removeClass('toast-appear');
        setTimeout(function() {
            div.hide();
        }, 200);
    }, timeout);
};


sn.ui.browse = function(img) {
    if (!sn.ui._browse) {
        sn.ui._browse = $('.sn .browse');
        sn.ui._browse.bind('click', function() {
            sn.ui._browse.removeClass('browse-appear');
            setTimeout(function() {
                sn.ui._browse.hide();
            }, 200);
        });
    }
    img.bind('click', function() {
        sn.ui._browse.find('img').attr('src', img.attr('src'));
        sn.ui._browse.removeClass('browse-appear');
        sn.ui._browse.show();
        sn.ui._browse.addClass('browse-appear');
    });
};


sn.ui.page = function() {
    var div = $('<div></div>');
    div.addClass('page');
    var pages = {};
    var curr = null;
    
    div.page_get = function(title) {
        return pages[title];
    };
    div.page_append = function(title, content) {
        pages[title] = content;
        if (0 == div.html().length) {
            curr = title;
            div.append(content);
            content.addClass('in');
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
        
        curr = title;
        if (!div.is(':visible')) {
            div.children().detach();
            div.append(pages[title]);
            pages[t_old].removeClass('ol or in');
            pages[title].removeClass('ol or in');
            pages[title].addClass('in');
        } else {
            if (i_old > i_new) { // come in from left
                pages[title].removeClass('ol or in');
                pages[title].addClass('ol');
                div.append(pages[title]);
                
                setTimeout(function() {
                    pages[t_old].removeClass('ol or in');
                    pages[t_old].addClass('or');
                    pages[title].removeClass('ol or in');
                    pages[title].addClass('in');
                    
                    setTimeout(function(){pages[t_old].detach();}, 300);
                }, 0);
            } else { // come in from right
                pages[title].removeClass('ol or in');
                pages[title].addClass('or');
                div.append(pages[title]);
                
                setTimeout(function() {
                    pages[t_old].removeClass('ol or in');
                    pages[t_old].addClass('ol');
                    pages[title].removeClass('ol or in');
                    pages[title].addClass('in');
                    
                    setTimeout(function(){pages[t_old].detach();}, 300);
                }, 0);
            }
        }
        if (pages[t_old].ondisappear) pages[t_old].ondisappear();
        if (pages[title].onappear)    pages[title].onappear();
    };
    div.page_curr = function() {return curr;};
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
};


sn.ui.choose_image = function(size, success, failure) {
    if (!size) size = 1024 * 1024 * 1024;

    var div = $('<div></div>');
    div.addClass('choose-image');
    div.append('<img />');
    div.append("<input type='file' accept='image/*'>");
    
    var div_image = div.find('img');
    var div_input = div.find('input');
    
    div_input.bind('change', function(e) {
        var files = e.target.files || e.dataTransfer.files;
        if (!files || !files[0]) return;
        
        var file = files[0];
        if (file.size > size) {
            if (failure) failure(file.size);
            return;
        }
        
        var reader = new FileReader();
        reader.onload = function(e1) {
            div_image.attr('src', e1.target.result);
            if (success) success(e1.target.result);
        };
        reader.readAsDataURL(file);
    });
    
    return div;
};

sn.ui.cover = function(src) {
    var div = $('<div></div>');
    div.addClass('cover');
    div.append("<img src='" + src + "' />");
    return div;
}


sn.ui.scroll = function(div, begin, end, top, bottom, timeout) {
    div.scrolling = false;
    
    div.bind('scroll', function(e) {
        div.scrollend = new Date().getTime();
        
        if (!div.scrolling) {
            div.scrolling = true;
            
            var i = setInterval(function() {
                if (new Date().getTime() - div.scrollend > timeout) {
                    div.scrolling = false;
                    clearInterval(i);
                    
                    if (end) end();
                }
            }, 100);
            
            if (begin) begin();
        }
        if (0 == div.scrollTop() && top) top();
        if (div.scrollTop() + div.outerHeight(true) == div[0].scrollHeight && bottom) bottom();
    });
};


sn.ui.pull = function(div, down, up, offset) {
    var istop = false;
    var isbottom = false;
    var isdown = false;
    var isup = false;
    var length_down = 0;
    var length_up = 0;
    var t = null;
    var y = 0;
    div.bind('touchstart', function(e) {
        if (t) return;
        
        var touch = e.targetTouches[0];
        t = div.css('top');
        y = touch.pageY;
        
        if (div.scrollTop() == 0) istop = true;
        else istop = false;
        
        if (div.scrollTop() + div.outerHeight(true) == div[0].scrollHeight) isbottom = true;
        else isbottom = false;
        
        isdown = false;
        isup = false;
    });
    div.bind('touchmove', function(e) {
        var touch = e.targetTouches[0];
        if (istop) {
            if (touch.pageY - y > 0) {
                isdown = true;
                length_down = Math.pow(touch.pageY - y, 1 / 1.2);
                div.css('top', length_down + 'px');
                e.preventDefault(); // band elastic
            } else {
                istop = false; // band elastic
                isdown = false;
            }
        }
        if (isbottom) {
            if (touch.pageY - y < 0) {
                isup = true;
                length_up = Math.pow(y - touch.pageY, 1 / 1.2);
                div.css('top', (0 - length_up) + 'px');
                e.preventDefault(); // band elastic
            } else {
                isbottom = false; // band elastic
                isup = false;
            }
        }
    });
    div.bind('touchend', function(e) {
        var touch = e.targetTouches[0];
        div.css('top', t);
        t = null;
        
        if (isdown && length_down > offset && down) down();
        if (isup && length_up > offset && up) up();
    });
};


sn.ui.longtouch = function(div, cb) {
    var timer = null;
    var down = function(e) {
        timer = setTimeout(function() {
            cb(e);
        }, 1000);
    };
    var up = function(e) {
        if (timer) {
            clearTimeout(timer);
            timer = null;
        }
        e.preventDefault();
    };
    var out = function(e) {
        if (timer) {
            clearTimeout(timer);
            timer = null;
        }
        e.preventDefault();
    };
    div.bind('touchstart',  down);
    div.bind('mousedown',   down);
    div.bind('touchend',    up);
    div.bind('mouseup',     up);
    div.bind('touchmove',   out);
    div.bind('mouseleave',  out);
};


sn.ui.state = function(i) {
    var s = null;
    if ('number' == typeof i) s = $($('.sn .head .state')[0]);
    else s = i;
    s.flash = function() {
        s.removeClass('state-flash');
        setTimeout(function() {
            s.addClass('state-flash');
        }, 0);
    };
    s.isopen = function() {
        return s.width() != s.height();
    };
    s.open = function() {
        s.css('width', s.width() * 1.4 + s.find('>div').outerWidth(true) + 'px');
        s.find('>div').css('opacity', '0');
        s.find('>div').show();
        s.find('>div').css('opacity', '1');
    };
    s.close = function() {
        s.css('width', s.height() + 'px');
        s.find('>div').css('opacity', '0');
        setTimeout(function() {
            s.find('>div').hide();
        }, 200);
    };
    return s;
};


sn.ui.login = function() {
    var dialog = sn.ui.dialog();
    dialog.addClass('dialog-login');
    dialog.add_close('关闭');
    dialog.content.append(create_user_login(dialog));
    dialog.appear();
};


sn.ui.logout = function() {
    fomjar.util.cookie('token', '');
    fomjar.util.cookie('uid', '');
    sn.token = null;
    sn.uid = null;
    sn.user = null;
    $('.sn .head .cover img').attr('src', 'res/user.png');
    $('.sn .head >*:nth-child(2)').text('登录 / 注册');
    $('.sn .head .cover').unbind('click');
    $('.sn .head .cover').bind('click', sn.ui.login);
    
    $.each(sn.stub.logout, function(i, f) {f();});
};

sn.ui.detail = function() {
    var dialog = sn.ui.dialog();
    dialog.addClass('dialog-user-detail');
    dialog.add_close('关闭');
    dialog.content.append(create_user_detail(dialog));
    dialog.appear();
};


function create_user_login(dialog) {
    var page = sn.ui.page();
    page.page_append('登录',      create_user_login_1(dialog, page));
    page.page_append('注册-1',    create_user_register_1(dialog, page));
    page.page_append('注册-2',    create_user_register_2(dialog, page));
    page.page_append('注册-成功',  create_user_register_done(dialog));
    page.page_append('协议',      create_user_protocol(dialog, page));
    page.page_set('注册-1');
    return page;
}

function create_user_login_1(dialog, page) {
    var div = $('<div></div>');
    div.append(dialog.h1('登录'));
    div.append(dialog.t1('输入要登录的账户的手机号码。'));
    div.append(dialog.p1("<input type='text' placeholder='手机号码' >"));
    div.append(dialog.p1("<input type='password' placeholder='密码' >"));
    div.append(dialog.p1("<div class='button button-default'>登录</div>"));
    div.append(dialog.p2("还没有声呐账户？<div class='button'>立即注册</div>"));
    
    var div_pho = div.find('>div:nth-child(3) input');
    var div_pas = div.find('>div:nth-child(4) input');
    var div_log = div.find('>div:nth-child(5) .button');
    var div_reg = div.find('>div:nth-child(6) .button');
    
    div_log.doing = false;
    div_log.bind('click', function() {
        if (div_log.doing) return;
    
        var phone = div_pho.val();
        var pass  = div_pas.val();
        var error = null;
        if (0 == phone.length) {
            dialog.shake();
            sn.ui.toast("请输入手机号");
            return;
        }
        if (0 == pass.length) {
            dialog.shake();
            sn.ui.toast("请输入密码");
            return;
        }
        div_log.doing = true;
        div_log.addClass('button-disable');
        sn.login_manually(phone, pass, function() {
            div_log.doing = false;
            div_log.removeClass('button-disable');
            dialog.disappear();
        }, function(code, desc) {
            div_log.doing = false;
            div_log.removeClass('button-disable');
            dialog.shake();
            sn.ui.toast(desc);
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
//    div.append(dialog.h1('注册'));
    div.append(dialog.h1('身份验证'));
    div.append(dialog.p1('声呐账户会开启很多权益。'));
    div.append(dialog.p1("<input type='text' placeholder='手机号码' >"));
    div.append(dialog.p1("<input type='text' placeholder='4位验证码' ><div class='button'>获取</div>"));
    div.append(dialog.p1("<input type='password' placeholder='创建密码' >"));
    div.append(dialog.p2("选择“提交”即表示您同意<br/><div class='button'>声呐服务协议</div>。"));
    
    div.find('>div:nth-child(2)').css('display', 'none');
    
    var div_pho = div.find('>div:nth-child(3) input');
    var div_vco = div.find('>div:nth-child(4) input');
    var div_get = div.find('>div:nth-child(4) .button');
    var div_pas = div.find('>div:nth-child(5) input');
    var div_pro = div.find('>div:nth-child(6) .button');
    
    div_get.bind('click', function() {
        var phone = div_pho.val();
        var error = null;
        if (error = check_phone(phone)) {
            dialog.shake();
            sn.ui.toast(error);
            return;
        }
        
        div_get.addClass('button-disable');
        fomjar.net.send(ski.ISIS.INST_APPLY_VERIFY, {type : 'phone', phone : phone}, function(code, desc) {
            if (0 == code) {
                var t = 60;
                var i = setInterval(function() {
                    div_get.text('('+t+')');
                    t--;
                    if (t < 0) {
                        clearInterval(i);
                        div_get.removeClass('button-disable');
                        div_get.text('获取');
                    }
                }, 1000);
            } else {
                dialog.shake();
                sn.ui.toast(desc);
                div_get.removeClass('button-disable');
            }
        });
    });
    div_pro.bind('click', function() {page.page_set('协议');});
    
    div.ondisappear = function() {dialog.action.clear();};
    div.onappear = function() {
        dialog.action.clear();
        dialog.action.add('直接登录').bind('click', function() {page.page_set('登录');});
        dialog.action.add_default('提交').bind('click', function() {
            var phone = div_pho.val();
            var error = null;
            if (error = check_phone(phone)) {
                dialog.shake();
                sn.ui.toast(error);
                return;
            }
            var vcode = div_vco.val();
            if (0 == vcode.length) {
                dialog.shake();
                sn.ui.toast('验证码不能为空');
                return;
            }
            var pass = div_pas.val();
            if (0 == pass.length) {
                dialog.shake();
                sn.ui.toast('密码不能为空');
                return;
            }
            if (/'|"/.test(pass)) {
                dialog.shake();
                sn.ui.toast("密码不能包含“\"”(英文双引号)或“'”(英文单引号)");
                return;
            }
            
            fomjar.net.send(ski.ISIS.INST_APPLY_VERIFY, {type : 'phone', phone : phone, vcode : vcode}, function(code, desc) {
                if (0 == code) {
                    user_register.phone = phone;
                    user_register.vcode = vcode;
                    user_register.pass  = pass;
                    user_register.cover = null;
                    user_register.name  = phone.substring(0, phone.length - 4) + '****';
                    user_register.gender= 0;
//                    page.page_to_next();
                    fomjar.net.send(ski.ISIS.INST_UPDATE_USER, {
                        phone   : user_register.phone,
                        vcode   : user_register.vcode,
                        pass    : user_register.pass,
                        cover   : user_register.cover,
                        name    : user_register.name,
                        gender  : user_register.gender
                    }, function(code, desc) {
                        doing = false;
                        if (0 == code) {
                            sn.login_manually(user_register.phone, user_register.pass, function() {
                                page.page_set('注册-成功');
                            }, function(code, desc) {
                                dialog.shake();
                                sn.ui.toast(desc);
                            });
                        } else {
                            dialog.shake();
                            sn.ui.toast(desc);
                        }
                    });
                } else {
                    dialog.shake();
                    sn.ui.toast(desc);
                }
            });
        });
    };
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
    div.append(dialog.h1('个人信息'));
    div.append(dialog.p1(sn.ui.choose_image(1024 * 1024 * 1024, function(image) {
        user_register.cover = image;
    }, function(size) {
        dialog.shake();
        sn.ui.toast('图片太大');
    })));
    div.append(dialog.p1("<label>姓名</label><input type='text' placeholder='您的姓名'>"));
    div.append(dialog.p1("<label>性别</label><select><option value='0' selected='selected'>女</option><option value='1'>男</option></select>"));
    div.append(dialog.t2('性别一旦注册成功无法修改，请谨慎选择'));
    
    var div_nam = div.find('>div:nth-child(3) input');
    var div_gen = div.find('>div:nth-child(4) select');
    
    div.ondisappear = function() {dialog.action.clear();};
    div.onappear = function() {
        dialog.action.clear();
        dialog.action.add('上一步').bind('click', function() {page.page_to_prev();});
        var doing = false;
        dialog.action.add_default('提交').bind('click', function() {
            var name = div_nam.val();
            if (0 == name.length) {
                dialog.shake();
                sn.ui.toast('请输入您的姓名');
                return;
            }
            if (/'|"/.test(name)) {
                dialog.shake();
                sn.ui.toast("姓名不能包含“\"”(英文双引号)或“'”(英文单引号)");
                return;
            }
            
            if (doing) return;
            doing = true;
            sn.ui.toast('正在提交', 10000);
            user_register.name = name;
            user_register.gender = parseInt(div_gen.val());
            
            fomjar.net.send(ski.ISIS.INST_UPDATE_USER, {
                phone   : user_register.phone,
                vcode   : user_register.vcode,
                pass    : user_register.pass,
                cover   : user_register.cover,
                name    : user_register.name,
                gender  : user_register.gender
            }, function(code, desc) {
                doing = false;
                sn.ui.toast('', 0);
                if (0 == code) {
                    sn.login_manually(user_register.phone, user_register.pass, function() {
                        page.page_to_next();
                    }, function(code, desc) {
                        dialog.shake();
                        sn.ui.toast(desc);
                    });
                } else {
                    dialog.shake();
                    sn.ui.toast(desc);
                }
            });
        });
    };

    return div;
}

function create_user_register_done(dialog) {
    var div = $('<div></div>');
    div.append(dialog.h1('注册成功'));
    div.append(dialog.p1('欢迎您加入声呐'));
    div.append(dialog.p1("<div class='button button-default'>开启声呐之旅</div>"));
    
    div.find('.button').bind('click', function() {dialog.disappear();});
    
    div.onappear = function() {
        dialog.action.clear();
        dialog.remove_close();
    };
    
    return div;
}

function create_user_protocol(dialog, page) {
    var iframe = $('<iframe></iframe>');
    iframe.attr('src', 'protocol.html');
    var div = $('<div></div>');
    div.addClass('protocol');
    div.append(iframe);
    div.onappear = function() {
        dialog.action.clear();
        dialog.action.add('返回').bind('click', function() {page.page_set('注册-1');});
    };
    div.ondisappear = function() {dialog.action.clear();};
    return div;
}


function create_user_detail(dialog) {
    var page = sn.ui.page();
    page.page_append('信息',  create_user_detail_info(dialog, page));
    page.page_append('头像',  create_user_detail_cover(dialog, page));
    page.page_append('姓名',  create_user_detail_name(dialog, page));
    page.page_append('手机',  create_user_detail_phone(dialog, page));
    page.page_append('密码',  create_user_detail_pass(dialog, page));
    return page;
}

function create_user_detail_info(dialog, page) {
    var div = $('<div></div>');
    div.addClass('page-user-detail-info');
    div.append(dialog.h1(sn.ui.cover(sn.user.cover)));
    var list = $('<div></div>');
    list.addClass('list');
    list.append("<div class='pair'><div>姓名</div><div>" + sn.user.name + "</div></div>");
    list.append("<div class='pair'><div>手机</div><div>" + sn.user.phone + "</div></div>");
    list.append("<div class='pair'><div>性别</div><div>" + (0 == sn.user.gender ? '女' : '男') + "</div></div>");
    list.append("<div class='pair'><div>密码</div><div>******</div></div>");
    div.append(dialog.p1(list));
    div.append(dialog.p1("<div class='button'>注销</div>"));
    
    div.find('.cover').bind('click', function() {page.page_set('头像');});
    div.find('.list .pair:nth-child(1)').bind('click', function() {page.page_set('姓名');});
    div.find('.list .pair:nth-child(2)').bind('click', function() {page.page_set('手机');});
    div.find('.list .pair:nth-child(3)').bind('click', function() {dialog.shake();sn.ui.toast('不能编辑');});
    div.find('.list .pair:nth-child(4)').bind('click', function() {page.page_set('密码');});
    div.find('.button').bind('click', function() {
        sn.ui.logout();
        dialog.disappear();
    });
    
    return div;
}

function create_user_detail_cover(dialog, page) {
    var div = $('<div></div>');
    div.addClass('page-user-detail-cover');
    div.append(dialog.h1('修改头像'));
    div.append(dialog.h1(sn.ui.choose_image((1024 * 1024 * 1024), function() {}, function() {dialog.shake();sn.ui.toast('图片太大');})));
    div.find('img').attr('src', sn.user.cover);
    
    div.ondisappear = function() {dialog.action.clear();};
    div.onappear = function() {
        dialog.action.clear();
        dialog.action.add('返回').bind('click', function() {page.page_set('信息');});
        var doing = false;
        dialog.action.add('提交').bind('click', function() {
            if (doing) return;
            doing = true;
            sn.ui.toast('正在提交', 10000);
            fomjar.net.send(ski.ISIS.INST_UPDATE_USER, {
                cover : div.find('img').attr('src')
            }, function(code, desc) {
                doing = false;
                if (0 == code) {
                    sn.ui.toast('修改头像成功');
                    sn.user.cover = div.find('img').attr('src');
                    page.page_get('信息').find('img').attr('src', sn.user.cover);
                    $('.sn .head .cover img').attr('src', sn.user.cover);
                    page.page_set('信息');
                } else {
                    dialog.shake();
                    sn.ui.toast(desc);
                }
            });
        });
    };
    return div;
}

function create_user_detail_name(dialog, page) {
    var div = $('<div></div>');
    div.append(dialog.h1('修改姓名'));
    div.append(dialog.p1("<input type='text' placeholder='姓名'>"));
    div.find('input').val(sn.user.name);
    
    div.ondisappear = function() {dialog.action.clear();};
    div.onappear = function() {
        dialog.action.clear();
        dialog.action.add('返回').bind('click', function() {page.page_set('信息');});
        dialog.action.add('提交').bind('click', function() {
            fomjar.net.send(ski.ISIS.INST_UPDATE_USER, {
                name : div.find('input').val()
            }, function(code, desc) {
                if (0 == code) {
                    sn.ui.toast('修改姓名成功');
                    sn.user.name = div.find('input').val();
                    page.page_get('信息').find('.pair:nth-child(1) div:nth-child(2)').text(sn.user.name);
                    $('.sn .head >*:nth-child(2)').text(sn.user.name);
                    page.page_set('信息');
                } else {
                    dialog.shake();
                    sn.ui.toast(desc);
                }
            });
        });
    };
    return div;
}

function create_user_detail_phone(dialog, page) {
    var div = $('<div></div>');
    div.addClass('page-user-detail-phone');
    div.append(dialog.h1('修改手机'));
    div.append(dialog.p1("<input type='text' placeholder='手机号码'>"));
    div.append(dialog.p1("<input type='text' placeholder='4位验证码' ><div class='button'>获取</div>"));
    div.find('>input').val(sn.user.phone);
    
    var div_pho = div.find('>div:nth-child(2) input');
    var div_vco = div.find('>div:nth-child(3) input');
    var div_get = div.find('>div:nth-child(3) .button');
    
    div_get.doing = false;
    div_get.bind('click', function() {
        if (div_get.hasClass('button-disable')) return;
        
        var phone = div_pho.val();
        var error = null;
        if (error = check_phone(phone)) {
            dialog.shake();
            sn.ui.toast(error);
            return;
        }
        
        div_get.addClass('button-disable');
        fomjar.net.send(ski.ISIS.INST_APPLY_VERIFY, {type : 'phone', phone : phone}, function(code, desc) {
            if (0 == code) {
                var t = 60;
                var i = setInterval(function() {
                    div_get.text('('+t+')');
                    t--;
                    if (t < 0) {
                        clearInterval(i);
                        div_get.removeClass('button-disable');
                        div_get.text('获取');
                    }
                }, 1000);
            } else {
                dialog.shake();
                sn.ui.toast(desc);
                div_get.removeClass('button-disable');
            }
        });
    });
    
    div.ondisappear = function() {dialog.action.clear();};
    div.onappear = function() {
        dialog.action.clear();
        dialog.action.add('返回').bind('click', function() {page.page_set('信息');});
        dialog.action.add('提交').bind('click', function() {
            var phone = div_pho.val();
            var error = null;
            if (error = check_phone(phone)) {
                dialog.shake();
                sn.ui.toast(error);
                return;
            }
            var vcode = div_vco.val();
            if (0 == vcode.length) {
                dialog.shake();
                sn.ui.toast('验证码不能为空');
                return;
            }
            
            fomjar.net.send(ski.ISIS.INST_UPDATE_USER, {
                phone : div_pho.val(),
                vcode : vcode
            }, function(code, desc) {
                if (0 == code) {
                    sn.ui.toast('修改电话成功');
                    sn.user.phone = div_pho.val();
                    page.page_get('信息').find('.pair:nth-child(2) div:nth-child(2)').text(sn.user.phone);
                    page.page_set('信息');
                } else {
                    dialog.shake();
                    sn.ui.toast(desc);
                }
            });
        });
    };
    
    return div;
}

function create_user_detail_pass(dialog, page) {
    var div = $('<div></div>');
    div.append(dialog.h1('修改密码'));
    var list = $('<div></div>');
    list.addClass('list');
    list.append("<div class='pair'><div>旧密码</div><input type='password' placeholder='旧密码'>");
    list.append("<div class='pair'><div>新密码</div><input type='password' placeholder='新密码'>");
    list.append("<div class='pair'><div>重复</div><input type='password' placeholder='重复新密码'>");
    div.append(list);
    
    var div_old = div.find('.pair:nth-child(1) input');
    var div_new1 = div.find('.pair:nth-child(2) input');
    var div_new2 = div.find('.pair:nth-child(3) input');
    
    div.ondisappear = function() {dialog.action.clear();};
    div.onappear = function() {
        dialog.action.clear();
        dialog.action.add('返回').bind('click', function() {page.page_set('信息');});
        dialog.action.add('提交').bind('click', function() {
            var pass_old = div_old.val();
            var pass_new1 = div_new1.val();
            var pass_new2 = div_new2.val();
            
            if (pass_new1 != pass_new2) {
                dialog.shake();
                sn.ui.toast('两次输入密码不相同');
                return;
            }
            if (0 == pass_new1.length) {
                dialog.shake();
                sn.ui.toast('密码不能为空');
                return;
            }
            if (/'|"/.test(pass_new1)) {
                dialog.shake();
                sn.ui.toast("密码不能包含“\"”(英文双引号)或“'”(英文单引号)");
                return;
            }
            if (sn.user.pass != pass_old) {
                dialog.shake();
                sn.ui.toast('旧密码错误');
                return;
            }
            
            fomjar.net.send(ski.ISIS.INST_UPDATE_USER, {
                pass : pass_new1
            }, function(code, desc) {
                if (0 == code) {
                    sn.ui.toast('修改密码成功');
                    sn.user.pass = pass_new1;
                    page.page_set('信息');
                } else {
                    dialog.shake();
                    sn.ui.toast(desc);
                }
            });
        });
    };
    return div;
}



})(jQuery)
