
fomjar.framework.phase.append('dom', build_user_search);  
fomjar.framework.phase.append('dom', build_user_list);  
fomjar.framework.phase.append('dom', build_user_detail);

function build_user_search() {
    var search = $("<div><select>"
                      +"<option value='-1'>全搜索</option>"
                      +"<option value='0'>搜淘宝</option>"
                      +"<option value='1'>搜微信</option>"
                      +"<option value='2'>支付宝</option>"
                      +"<option value='3'>PSN</option>"
                  +"</select><input></div>");
    search.addClass('index-search');
    $('.omc .frame .body').append(search);

    search.find('select').bind('change', search_user);
    search.find('input').bind('keydown', search_user);
}

function build_user_list() {
    var list = $('<div></div>');
    list.addClass('list');
    list.addClass('index-list-user');

    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_CHANNEL_ACCOUNT, function(code, desc) {
        if (0 != code) return;

        $.each(desc, function(i, user) {
            list.append(create_user_cell(user));
        });
    });

    $('.omc .frame .body').append(list);
}

function create_user_cell(user) {
    var cell = $('<div></div>');
    cell.addClass('cell-user');

    var cover = $('<div></div>');
    if (0 < user.url_cover.length) cover.append("<img src='"+user.url_cover+"' />")
    else cover.append("<img src='"+fomjar.net.api()+'?inst='+fomjar.net.ISIS.INST_ECOM_APPLY_MAKE_COVER.toString(16)+'&string='+user.display_name.replace(/ /g, '_')+"'/>");
    var chann = $('<div>['+omc.channel(user.channel)+']</div>')
    var name  = $('<div>'+user.display_name+'</div>');
    var phone = $('<div>电话: '+user.phone+'</div>')

    cell.append(cover);
    cell.append(chann);
    cell.append(name);
    cell.append(phone);

    return cell;
}

function build_user_detail() {
    var detail = $('<div></div>');
    detail.addClass('detail');

    $('.omc .frame .body').append(detail);
}

function search_user() {
/*
    var select = $('.index-search select');
    var input = $('.index-search input');
    switch (search.val()) {
    case '-1' :
        $.each($('.index-list-user .cell-user').children(), function(i, c) {
            if ($($(c).children()[3]).text())
            $(c).show();
        });
        break;
    }
    case '1' :
        $.each($('.index-list-user .cell-user').children(), function(i, c) {
            if $()
            $(c).show();
        });
        break;
    }
    case '-1' :
        $.each($('.index-list-user .cell-user').children(), function(i, c) {
            $(c).show();
        });
        break;
    }
    case '-1' :
        $.each($('.index-list-user .cell-user').children(), function(i, c) {
            $(c).show();
        });
        break;
    }
    case '-1' :
        $.each($('.index-list-user .cell-user').children(), function(i, c) {
            $(c).show();
        });
        break;
    }
    */
}


