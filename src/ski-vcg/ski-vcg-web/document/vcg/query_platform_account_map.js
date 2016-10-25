(function($) {

fomjar.framework.phase.append('dom', build_list);
fomjar.framework.phase.append('dom', build_buttons);
fomjar.framework.phase.append('ren', setup);

function build_list() {
    var list = $('<div></div>');
    list.addClass('list');
    $('.vcg .frame .body').append(list);
}

function build_buttons() {
    var button = $('<div></div>');
    button.addClass('button button-major button-large');
    button.bind('touchend', function() {window.location = 'update_platform_account_map.html';});
    button.text('更新');

    $('.vcg .frame .body').append(button);
}

function setup() {
    vcg.show_toast('正在获取...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT_MAP, function(code, desc) {
        vcg.hide_toast();
        if (0 != code) {
            vcg.show_toast('获取失败', 10000);
            return;
        }
        var has_taobao = false;
        $.each(desc, function(i, u) {
            switch (u.channel) {
            case 0: // taobao
                has_taobao = true;
                $('.list').append("<div class='cell-kv'><div>淘宝帐号</div><div>"+u.user+"</div></div>");
                break;
            case 1: // vcg
                $('.list').append("<div class='cell-kv'><div>昵称</div><div>"+u.name+"</div></div>");
                $('.list').append("<div class='cell-kv'><div>手机</div><div>"+u.phone+"</div></div>");
                break;
            case 2: // alipay
                $('.list').append("<div class='cell-kv'><div>支付宝帐号</div><div>"+u.user+"</div></div>");
                break;
            case 3: // psn
                $('.list').append("<div class='cell-kv'><div>PSN-ID</div><div>"+u.user+"</div></div>");
                break;
            }
        });

        if (!has_taobao) {
            var tips = $('<div></div>');
            tips.addClass('tips');
            tips.html('如果您曾在\"VC电玩\"淘宝店铺消费，请联系淘宝客服录入手机号。<br/>'
                    + '录入手机之后可以关联到您的消费记录、账户余额等信息哦～');
            $('.vcg .frame .body').append(tips);
        }
    });
}


})(jQuery)

