
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
    div.append("<div class='button'>删除</div>");
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
    div.append("<div><select><option selected='selected' value='0'>选择模块</option><option value='1'>投票</option></select><div>");
    div.append("<div><div class='button'>继续</div></div>");
    
    var div_sel = div.find(">*:nth-child(2) select");
    
    div_sel.bind('change', function() {
        var val = parseInt(div_sel.val());
        switch(val) {
        case 1:
            pages.page_set('模块-投票');
            break;
        }
    });
    return div;
}

function create_activity_module_privilege(roles) {
    var div = $('<div></div>');
    div.addClass('act-mod-pvl');
    
    $.each(roles, function(i, role) {
        div.append("<label>" + role.name + "</label><input type='checkbox'><label>可操作</label><input type='checkbox'><label>可查看</label>");
    });
    
    return div;
}

function create_activity_module_vote(dialog, pages, activity) {
    var div = $('<div></div>');
    
    div.append(create_activity_module_privilege(activity.roles));
    return div;
}

})(jQuery)
