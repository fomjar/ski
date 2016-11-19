
(function($) {

sn.act = {};
sn.act.data = [];


sn.act.new = function() {
    var dialog = sn.ui.dialog();
    dialog.addClose('取消');
    dialog.append(create_new_activity_panel(dialog));
    dialog.appear();
};


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
    
    if (name) div.find('>*:nth-child(2)').val(name);
    
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
    var div = $('<div></div>');
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
    div.append("<div>模块 - 权限</div>");
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
                fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_ROLE, r);
            });
            
            $.each(activity.modules, function(i, m) {
                m.module.aid = aid;
                fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_MODULE, m.module);
                
                $.each(m.privileges, function(i, p) {
                    p.aid = aid;
                    fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_MODULE_PRIVILEGE, p);
                });
                if (m.vote) {
                    m.vote.aid = aid;
                    fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_MODULE_VOTE, m.vote);
                    $.each(m.vote.items, function(i, it) {
                        it.aid = aid;
                        fomjar.net.send(ski.ISIS.INST_UPDATE_ACTIVITY_MODULE_VOTE_ITEM, it);
                    });
                }
            });
            pages.page_set('成功');
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
