
fomjar.framework.phase.append('dom', build_user_list);  
fomjar.framework.phase.append('dom', build_user_detail);

function build_user_list() {
    var list = $('<div></div>');
    list.addClass('omc-list');
    list.addClass('index-user-list');

    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_CHANNEL_ACCOUNT, function(code, desc) {
        if (0 != code) return;

        $.each(desc, function(i, user) {
            list.append(create_user_cell(user));
        });
    });

    $('.omc-frame-content').append(list);
}

function create_user_cell(user) {
    var cell = $('<div></div>');

    var cover = $('<div></div>');
    if (0 < user.url_cover.length) cover.append("<img src='"+user.url_cover+"' />")
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
    detail.addClass('index-user-detail');

    $('.omc-frame-content').append(detail);
}
