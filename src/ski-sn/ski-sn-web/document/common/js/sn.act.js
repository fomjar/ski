
(function($) {

sn.act = {};
sn.act.data = [];


sn.act.new = function() {
    var dialog = sn.ui.dialog();
    dialog.addClose('取消');
    dialog.append(create_new_activity_panel(dialog));
    dialog.appear();
};

sn.act.wrap = function(data) {
    var activity = {};
    if (!data.ucover) data.ucover = 'res/user.png';
    activity.data = data;
    
    activity.data.getRole = function(arsn) {
        var result = null;
        $.each(activity.data.roles, function(i, role) {
            if (role.arsn == arsn) {
                result = role;
                return false;
            }
        });
        return result;
    };
    
    activity.data.getModule = function(amsn) {
        var role = activity.data.role();
        if (!role) return null;
        
        var result = null;
        $.each(activity.data.modules, function(i, module) {
            if (module.amsn == amsn) {
                result = module;
                return false;
            }
        });
        return result;
    };
    
    activity.data.getPrivilege = function(amsn) {
        var role = activity.data.role();
        if (!role) return null;
        var module = activity.data.getModule(amsn);
        if (!module) return null;
        
        var result = null;
        $.each(module.privilege, function(i, p) {
            if (p.amsn == module.amsn && p.arsn == role.arsn) {
                result = p;
                return false;
            }
        });
        return result;
    };
    
    activity.data.role = function() {
        var result = null;
        $.each(activity.data.players, function(i, p) {
            if (p.uid == sn.uid) {
                result = activity.data.getRole(p.arsn);
                return false;
            }
        });
        return result;
    };
    

    wrap_module(activity);
    
    var panel = create_activity_panel(activity);
    var detail = create_activity_detail(activity);
    activity.ui = {};
    activity.ui.panel = panel;
    activity.ui.detail = detail;
    
    panel.bind('click', function() {
        if (!sn.user) {
            sn.ui.login();
            return;
        }
        var dialog = sn.ui.dialog();
        dialog.append(activity.ui.detail);
        dialog.appear();
    });
    
    return activity;
};

function wrap_module(activity) {
    $.each(activity.data.modules, function(i, module) {
        module.readable = function() {
            var privilege = activity.data.getPrivilege(module.amsn);
            if (!privilege) return false;
            return 0 < (privilege.privilege & (1<<0));
        };
        module.writable = function() {
            var privilege = activity.data.getPrivilege(module.amsn);
            if (!privilege) return false;
            return 0 < (privilege.privilege & (1<<1));
        };
        switch (module.type) {
        case 1:
            if (module.vote) {
                var values = [];
                var total = 0;
                var voted = false;
                $.each(module.vote.players, function(i, p) {
                    if (!values[p.amvisn]) values[p.amvisn] = 0;
                    if (1 == p.result) {
                        values[p.amvisn]++;
                        total++;
                    }
                    if (p.uid == sn.uid) {
                        voted = true;
                    }
                });
                module.vote.total = total;
                $.each(module.vote.items, function(i, item) {
                    item.value      = values[item.amvisn];
                    if (!item.value) item.value = 0;
                    item.percentage = function() {return 0 == module.vote.total ? 0 : item.value / module.vote.total * 100;};
                });
                module.vote.voted = voted;
            }
            break;
        }
    });
}


function create_activity_panel(activity) {
    var div = $('<div></div>');
    div.addClass('act-panel');

    var ac = $('<div></div>');
    ac.addClass('ac');
    ac.append("<img src='" + activity.data.ucover + "' />")
    ac.append('<div>' + activity.data.uname + '</div>')
    ac.append('<div>' + activity.data.acreate.substring(5, 16) + '</div>');
    ac.append('<div>' + activity.data.player + '人已参与</div>');
    ac.append('<div>[' + (activity.data.astate == 0 ? '未开始' : activity.data.astate == 1 ? '已开始' : '已结束') + '] ' + activity.data.atitle + '</div>');
    if (activity.data.atext)        ac.append('<div>' + activity.data.atext + '</div>');
    var img = $("<img class='image' src='" + (activity.data.aimage ? activity.data.aimage : '') + "' />");
    sn.ui.browse(img);
    ac.append(img);
    if (activity.data.abegin)       ac.append('<div>开始: ' + activity.data.abegin + '</div>');
    if (activity.data.aend)         ac.append('<div>结束: ' + activity.data.aend + '</div>');
    
    div.append(ac);
    
    return div;
}

function create_activity_detail(activity) {
    var dialog = sn.ui.dialog();

    var div = $('<div></div>');
    div.addClass('act-detail');
    div.append(create_activity_panel(activity));
    
    var mods = $('<div></div>');
//     if (!activity.data.role()) {
//         div.append('<div>正确选择你的角色</div>');
//         var roles = $('<div></div>');
//         roles.addClass('roles');
//         $.each(activity.data.roles, function(i, role) {
//             if (1 != role.apply) return;
//             
//             var div_rol = $("<div class='button'>" + role.name + "</div>");
//             roles.append(div_rol);
//             div_rol.bind('click', function() {
//                 fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_PLAYER, {
//                     aid     : activity.data.aid,
//                     uid     : sn.uid,
//                     arsn    : role.arsn
//                 }, function(code, desc) {
//                     if (0 != code) {
//                         dialog.shake();
//                         sn.ui.toast(desc);
//                         return;
//                     }
//                     
//                     activity.data.players.push({
//                         aid     : activity.data.aid,
//                         uid     : sn.uid,
//                         arsn    : role.arsn
//                     });
//                     sn.ui.toast('选择成功');
//                     roles.remove();
//     
//                     $.each(activity.data.modules, function(i, module) {
//                         if (!module.readable()) return true;
//                         
//                         switch (module.type) {
//                         case 1: {
//                             mods.append(create_activity_detail_vote(activity, module));
//                             break;
//                         }
//                         }
//                     });
//                 });
//             });
//         });
//         div.append(roles);
//     } else {
        $.each(activity.data.modules, function(i, module) {
//             if (!module.readable()) return true;
            
            switch (module.type) {
            case 1: {
                mods.append(create_activity_detail_vote(activity, module));
                break;
            }
            }
        });
        setInterval(function() {
            if (activity.data.astate == 1 && div.is(':visible')) {
                sn.ui.toast('正在刷新投票结果');
                
                fomjar.net.send(ski.ISIS.INST_QUERY_ACTIVITY_MODULE, {
                    aid : activity.data.aid
                }, function(code, desc) {
                    if (0 != code) {
                        sn.ui.toast(desc);
                        return;
                    }
                    activity.data.modules = desc;
                    
                    fomjar.net.send(ski.ISIS.INST_QUERY_ACTIVITY_MODULE_PRIVILEGE, {
                        aid : activity.data.aid
                    }, function(code, desc) {
                        if (0 != code) {
                            sn.ui.toast(desc);
                            return;
                        }
                        $.each(activity.data.modules, function(i, m) {
                            var ps = [];
                            $.each(desc, function(i, p) {
                                if (m.amsn == p.amsn) ps.push(p);
                            });
                            m.privilege = ps;
                    
                            switch (m.type) {
                            case 1: {
                                fomjar.net.send(ski.ISIS.INST_QUERY_ACTIVITY_MODULE_VOTE, {
                                    aid     : activity.data.aid,
                                    amsn    : m.amsn
                                }, function(code, desc) {
                                    if (0 != code) {
                                        sn.ui.toast(desc);
                                        return;
                                    }
                                    m.vote = desc[0];
                                    
                                    fomjar.net.send(ski.ISIS.INST_QUERY_ACTIVITY_MODULE_VOTE_ITEM, {
                                        aid     : activity.data.aid,
                                        amsn    : m.amsn
                                    }, function(code, desc) {
                                        if (0 != code) {
                                            sn.ui.toast(desc);
                                            return;
                                        }
                                        m.vote.items = desc;
                                        
                                        fomjar.net.send(ski.ISIS.INST_QUERY_ACTIVITY_MODULE_VOTE_PLAYER, {
                                            aid     : activity.data.aid,
                                            amsn    : m.amsn
                                        }, function(code, desc) {
                                            if (0 != code) {
                                                sn.ui.toast(desc);
                                                return;
                                            }
                                            m.vote.players = desc;
                                            
                                            wrap_module(activity);
                                            mods.children().remove();
                                            $.each(activity.data.modules, function(i, module) {
                                    //             if (!module.readable()) return true;
                                                
                                                switch (module.type) {
                                                case 1: {
                                                    mods.append(create_activity_detail_vote(activity, module));
                                                    break;
                                                }
                                                }
                                            });
                                        });
                                    });
                                });
                                break;
                            }
                            }
                        });
                    });
                });
            }
        }, 5000);
//     }
    div.append(mods);
    var buttons = $('<div></div>');
    buttons.addClass('btns');
    var back = $("<div class='button'>返回</div>");
    back.bind('click', function() {dialog.disappear()});
    buttons.append(back);
    if (sn.uid == activity.data.owner) {
        switch (activity.data.astate) {
        case 0: // 初始化
            var open = $("<div class='button'>开启活动</div>");
            open.bind('click', function() {
                fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY, {
                    aid     : activity.data.aid,
                    state   : 1
                }, function(code, desc) {
                    if (0 != code) {
                        sn.ui.dialog().shake();
                        sn.ui.toast(desc);
                        return;
                    }
                    open.remove();
                });
            });
            buttons.append(open);
            break;
        case 1: { // 开始
            var close = $("<div class='button'>关闭活动</div>");
            close.bind('click', function() {
                fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY, {
                    aid     : activity.data.aid,
                    state   : 2
                }, function(code, desc) {
                    if (0 != code) {
                        sn.ui.dialog().shake();
                        sn.ui.toast(desc);
                        return;
                    }
                    close.remove();
                });
            });
            buttons.append(close);
            break;
        }
        case 2: { // 关闭
            break;
        }
        }
    }
    div.append(buttons);
    return div;
}

function create_activity_detail_vote(activity, module) {
    var vote = $('<div></div>');
    vote.addClass('vote');
    vote.append('<div>' + module.title + '</div>')
    if (module.text) vote.append('<div>' + module.text + '</div>')
    var items = $('<div></div>');
    items.addClass('items');
    $.each(module.vote.items, function(i, item) {
        var div_ite = $('<div></div>');
        div_ite.append('<div></div>');  // value
        div_ite.append("<img src='" + item.arg1 + "' />");   // image
        div_ite.append('<div>' + item.arg0 + '</div>'); // name
        div_ite.append('<div>(' + item.value + ', ' + item.percentage().toFixed(1) + '%)</div>')
        
        sn.ui.browse(div_ite.find('img'));
        div_ite.find('>*:nth-child(1)').css('width', item.percentage().toFixed(1) + '%');
        
//         if (module.writable() && !module.vote.voted) {
        if (activity.data.astate == 1 && !module.vote.voted) {
            var div_vot = $("<div class='button'>投票</div>");
            div_ite.append(div_vot);
            div_vot.bind('click', function() {
                fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_PLAYER, {
                    aid     : activity.data.aid,
                    uid     : sn.uid,
                    arsn    : 0
//                     arsn    : role.arsn
                }, function(code, desc) {
                    if (0 != code) {
                        dialog.shake();
                        sn.ui.toast(desc);
                        return;
                    }
                    
                    activity.data.players.push({
                        aid     : activity.data.aid,
                        uid     : sn.uid,
                        arsn    : 0
                    });
                });
                
                fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_MODULE_VOTE_PLAYER, {
                    aid     : activity.data.aid,
                    amsn    : module.amsn,
                    amvisn  : item.amvisn,
                    uid     : sn.uid,
                    result  : 1
                }, function(code, desc) {
                    if (0 != code) {
                        dialog.shake();
                        sn.ui.toast(desc);
                        return;
                    }
                    module.vote.voted = true;
                    module.vote.total ++;
                    item.value ++;
                    
                    div_ite.find('>*:nth-child(5)').text('(' + item.value + ', ' + item.percentage().toFixed(1) + '%)');
                    div_ite.find('>*:nth-child(1)').css('width', item.percentage().toFixed(1) + '%');
                    
                    items.find('.button').remove();
                });
            });
        }
        
        items.append(div_ite);
    });
    vote.append(items);
    return vote;
}


function create_new_activity_panel(dialog) {
    var activity = {};
    
    var pages = sn.ui.page();
    pages.page_append('基本',     create_activity_basic(dialog, pages, activity));
    pages.page_append('角色',     create_activity_roles(dialog, pages, activity));
    pages.page_append('模块',     create_activity_module(dialog, pages, activity));
    pages.page_append('创建',     create_activity_creating(dialog, pages, activity));
    pages.page_append('成功',     create_activity_done(dialog, pages, activity));
    pages.page_append('模块-投票', create_activity_module_vote(dialog, pages, activity));
    
    return pages;
}

function create_activity_basic(dialog, pages, activity) {
    var div = $('<div></div>');
    div.addClass('act-basic');
    div.append('<div>新活动</div>');
    div.append("<div><input type='text' placeholder='活动标题'></div>");
    div.append("<div><textarea placeholder='活动描述'></textarea></div>");
    div.append(sn.ui.choose_image(1024 * 1024 * 2, function() {}, function() {dialog.shake();}));
    div.append("<div><input type='text' placeholder='开始时间'><input type='text' placeholder='结束时间'></div>");
    div.append("<div><div class='button'>继续</div></div>");
    
    var div_tit = div.find('>*:nth-child(2) input');
    var div_tex = div.find('>*:nth-child(3) textarea');
    var div_img = div.find('>*:nth-child(4) img');
    var div_beg = div.find('>*:nth-child(5) input:nth-child(1)');
    var div_end = div.find('>*:nth-child(5) input:nth-child(2)');
    
    div.find('>*:nth-child(6) .button').bind('click', function() {
        var basic = {};
        basic.owner = sn.uid;
        basic.lat   = sn.location.point.lat;
        basic.lng   = sn.location.point.lng;
        basic.title = div_tit.val();
        basic.text  = div_tex.val();
        basic.image = div_img.attr('src');
        basic.begin = div_beg.val();
        basic.end   = div_end.val();
        
        if (0 == basic.title.length) {
            dialog.shake();
            sn.ui.toast('标题不能为空');
            return;
        }
        
        activity.basic = basic;
        pages.page_set('角色');
    });
    return div;
}

function create_activity_roles(dialog, pages, activity) {
    var div = $('<div></div>');
    div.addClass('act-roles');
    div.append('<div>配置角色</div>');
    div.append("<div><div class='button'>添加</div></div>");
    var div_roles = $('<div></div>');
    div.append(div_roles);
    div.append("<div><div class='button'>返回</div><div class='button'>继续</div></div>");
    
    div_roles.append(create_activity_role('观众'));
    div.find('>*:nth-child(2) .button').bind('click', function() {
        div_roles.append(create_activity_role());
    });
    
    div.find('>*:nth-child(4) .button:nth-child(1)').bind('click', function() {
        pages.page_set('基本');
    });
    div.find('>*:nth-child(4) .button:nth-child(2)').bind('click', function() {
        var roles = [];
        
        var valid = true;
        $.each(div_roles.children(), function(i, r) {
            r = $(r);
            var role = {};
            role.arsn   = i;
            role.name   = r.find('>*:nth-child(2)').val();
            role.count  = r.find('>*:nth-child(3)').val();
            role.apply  = r.find('>*:nth-child(4)').is(':checked') ? 1 : 0;
            
            if (0 == role.name.length) {
                dialog.shake();
                sn.ui.toast('角色名称不能为空');
                return valid = false;
            }
            
            if (/[0-9]+/.test(role.count)) {
                role.count = parseInt(role.count);
            } else {
                dialog.shake();
                sn.ui.toast('人数必须是数字');
                return valid = false;
            }
            
            roles.push(role);
        });
        
        if (!valid) return;
        if (0 == roles.length) {
            dialog.shake();
            sn.ui.toast('至少配置1个角色');
            return;
        }
        
        activity.roles = roles;
        pages.page_set('模块');
    });
    
    return div;
}

function create_activity_role(name) {
    var div = $('<div></div>');
    div.addClass('act-role');
    div.append("<div class='button'>删</div>");
    div.append("<input type='text' placeholder='角色名'>");
    div.append("<input type='number' placeholder='人数'>");
    div.append("<input type='checkbox' checked='checked' >");
    div.append('<label>开放申请</label>');
    
    if (name) {
        div.find('>*:nth-child(2)').val(name);
        div.find('>*:nth-child(3)').val(9999);
    }
    
    div.find('.button').bind('click', function() {div.remove();});
    
    return div;
}

function create_activity_module(dialog, pages, activity) {
    var div = $('<div></div>');
    div.addClass('act-mod');
    div.append('<div>添加模块</div>');
    div.append("<div><div class='button'>投票</div></div>");
    div.append('<div>已添加的模块</div>');
    var div_mod = $('<div></div>');
    div.append(div_mod);
    div.append("<div><div class='button'>返回</div><div class='button'>继续</div></div>");
    
    div.find(">*:nth-child(2) .button:nth-child(1)").bind('click', function() {
        pages.page_set('模块-投票');
    });
    activity.modules = [];
    div.onappear = function() {
        div_mod.children().remove();
        $.each(activity.modules, function(i, module) {
            switch (module.module.type) {
            case 1: {
                var m = $('<div></div>');
                m.append("<label>[投票]</label><label>" + module.module.title + '</label><label>' + module.vote.items.length + '项</label>');
                div_mod.append(m);
                break;
            }
            }
        });
    };
    div.find('>*:nth-child(5) .button:nth-child(1)').bind('click', function() {
        pages.page_set('角色');
    });
    div.find('>*:nth-child(5) .button:nth-child(2)').bind('click', function() {
        pages.page_set('创建');
    });
    
    return div;
}

function create_activity_module_privilege(activity) {
    var div = $("<div style='display : none'></div>");
    div.addClass('act-mod-pvl');
    
    div.onappear = function() {
        div.children().remove();
        $.each(activity.roles, function(i, role) {
            div.append("<div><label>" + role.name + "</label><input type='checkbox' checked='checked'><label>可操作</label><input type='checkbox' checked='checked'><label>可查看</label></div>");
        });
    };
    
    return div;
}

function create_activity_module_vote(dialog, pages, activity) {
    var div = $('<div></div>');
    div.addClass('act-mod-vote');
    
    div.append("<div>模块 - 投票</div>");
    div.append("<input type='text' placeholder='投票标题'>");
    div.append("<textarea type='text' placeholder='投票描述'></textarea>");
    div.append("<div><div class='button'>添加</div></div>");
    var div_ite = $('<div></div>');
    div_ite.addClass('act-mod-vote-items');
    div.append(div_ite);
    div.append("<div style='display : none'>模块 - 权限</div>");
    var div_pvl = create_activity_module_privilege(activity);
    div.append(div_pvl);
    div.append("<div><div class='button'>返回</div><div class='button button-default'>确认</div></div>");
    
    div_ite.append(create_activity_module_vote_item('投票项 1'));
    div.find('>*:nth-child(4) .button').bind('click', function() {
        div_ite.append(create_activity_module_vote_item());
    });
    div.find('>*:nth-child(8) >.button:nth-child(1)').bind('click', function() {
        pages.page_set('模块');
    });
    div.find('>*:nth-child(8) >.button:nth-child(2)').bind('click', function() {
        var amsn = activity.modules.length;
    
        var mod = {};
        mod.amsn    = amsn;
        mod.type    = 1;
        mod.title   = div.find('>*:nth-child(2)').val();
        mod.text    = div.find('>*:nth-child(3)').val();
        
        var vote = {};
        vote.amsn   = amsn;
        vote.select = 0;    // 单选
        vote.anonym = 0;    // 实名
        vote.item   = 1;    // 图文
        
        vote.items = [];
        $.each(div_ite.children(), function(i, it) {
            it = $(it);
            var item = {};
            item.amsn   = amsn;
            item.amvisn = i;
            item.arg0   = it.find('>*:nth-child(2)').val();
            item.arg1   = it.find('>*:nth-child(3) img').attr('src');
            
            vote.items.push(item);
        });
        
        var privileges = [];
        $.each(div_pvl.children(), function(i, p) {
            p = $(p);
            var privilege = {};
            privilege.amsn = amsn;
            privilege.arsn = i;
            var pw = p.find('>*:nth-child(2)').is(':checked') ? 1 : 0;
            var pr = p.find('>*:nth-child(4)').is(':checked') ? 1 : 0;
            privilege.privilege = (pw<<1) | (pr<<0);
            
            privileges.push(privilege);
        });
        
        if (0 == mod.title.length) {
            dialog.shake();
            sn.ui.toast('投票标题不能为空');
            return;
        }
        var valid = true;
        $.each(vote.items, function(i, item) {
            if (0 == item.arg0.length) {
                dialog.shake();
                sn.ui.toast('投票项名称不能为空');
                valid = false;
                return false;
            }
        });
        
        if (!valid) return;
        
        activity.modules.push({
            module      : mod,
            privileges  : privileges,
            vote        : vote
        });
        
        pages.page_set('模块');
    });
    
    div.onappear = function() {
        div_pvl.onappear();
    };
    
    return div;
}

function create_activity_module_vote_item(item) {
    var div = $('<div></div>');
    div.append("<div class='button'>删</div>");
    div.append("<input type='text' placeholder='投票项'>");
    div.append(sn.ui.choose_image());
    
    div.find('>.button').bind('click', function() {div.remove();});
    if (item) div.find('>input').val(item);
    
    return div;
}

function create_activity_creating(dialog, pages, activity) {
    var div = $('<div></div>');
    div.addClass('act-create');
    div.append('<div>创建中</div>');
    
    div.onappear = function() {
        dialog.removeClose();
        fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY, activity.basic, function(code, desc) {
            if (0 != code) {
                dialog.shake();
                sn.ui.toast(desc);
                return;
            }
            var aid = parseInt(desc);
            $.each(activity.roles, function(i, r) {
                r.aid = aid;
                fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_ROLE, r, function() {});
            });
            
            $.each(activity.modules, function(i, m) {
                m.module.aid = aid;
                fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_MODULE, m.module, function() {});
                
                $.each(m.privileges, function(i, p) {
                    p.aid = aid;
                    fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_MODULE_PRIVILEGE, p, function() {});
                });
                if (m.vote) {
                    m.vote.aid = aid;
                    fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_MODULE_VOTE, m.vote, function() {});
                    $.each(m.vote.items, function(i, it) {
                        it.aid = aid;
                        fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_MODULE_VOTE_ITEM, it, function() {});
                    });
                }
            });
            setTimeout(function() {
                pages.page_set('成功');
            }, 5000);
        });
    };
    
    return div;
}

function create_activity_done(dialog, pages, activity) {
    var div = $('<div></div>');
    div.addClass('act-create-done');
    div.append('<div>请刷新查看</div>');
    div.append("<div><div class='button'>完成</div></div>");
    div.find('.button').bind('click', function() {
        dialog.disappear();
    });
    return div;
}

})(jQuery)
