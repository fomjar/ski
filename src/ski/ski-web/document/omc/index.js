(function($) {

fomjar.framework.phase.append('dom', build_user_search);  
fomjar.framework.phase.append('dom', build_user_list);  
fomjar.framework.phase.append('dom', build_user_detail);

fomjar.framework.phase.append('ren', setup);

function build_user_search() {
    var search = $("<div><select>"
                      +"<option value='-1'>全搜索</option>"
                      +"<option value='0'>搜淘宝</option>"
                      +"<option value='1'>搜微信</option>"
                      +"<option value='2'>支付宝</option>"
                      +"<option value='3'>PSN</option>"
                  +"</select><input placeholder='搜索: 用户名 / 昵称 / 电话'></div>");
    search.addClass('index-search');
    $('.omc .frame .body').append(search);

    search.find('select').bind('change', search_user);
    search.find('input').bind('keyup', search_user);
}

function build_user_list() {
    var list = $('<div></div>');
    list.addClass('list');
    list.addClass('index-list-user');

    $('.omc .frame .body').append(list);
}

function build_user_detail() {
    var detail = $('<div></div>');
    detail.addClass('detail');

    $('.omc .frame .body').append(detail);
}

function search_user() {
    var select = parseInt($('.index-search select').val());
    var input = $('.index-search input').val();
    $.each($('.index-list-user .cell-user'), function(i, c) {
        c = $(c);
        var ca = c.data('ca');
        if (-1 != select) {
            if (ca.channel != select) {
                c.hide();
                return;
            }
        }
        if (-1 != ca.user.toLowerCase().indexOf(input.toLowerCase())
         || -1 != ca.name.toLowerCase().indexOf(input.toLowerCase())
         || -1 != ca.phone.toLowerCase().indexOf(input.toLowerCase())) c.show();
        else c.hide();
    });
}

function setup() {
    var dialog = omc.show_dialog({head : '正在加载'});
    var dialog = omc.show_dialog({head : '正在加载'});
    setup_user(dialog);
}

function setup_user(dialog) {
    dialog.body.append('<div>正在加载用户...</div>')
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_CHANNEL_ACCOUNT, function(code, desc) {
        if (0 != code) {
            dialog.body.append('<div>'+desc+'</div>')
            return;
        }

        $('.index-list-user').html('');
        $.each(desc, function(i, user) {
            var cell = create_user_cell(user);
            cell.data('ca', user);
            $('.index-list-user').append(cell);
        });
    });
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



})(jQuery)

