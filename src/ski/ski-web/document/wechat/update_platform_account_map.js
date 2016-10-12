
fomjar.framework.phase.append('dom', build_tips);
fomjar.framework.phase.append('dom', build_list);
fomjar.framework.phase.append('dom', build_buttons);
fomjar.framework.phase.append('ren', setup);

function build_tips() {
    var tips = $('<div></div>');
    tips.addClass('tips');
    tips.text('认证手机以便于跟您的淘宝帐号关联');

    $('.wechat .frame .body').append(tips);
}

function build_list() {
    var list = $('<div></div>');
    list.addClass('list');
    list.append("<div class='cell-kv'><div>PSN-ID</div> <input type='text' id='psn' /></div>");
    list.append("<div class='cell-kv'><div>手机</div>   <input type='text' id='phone' /></div>");
    list.append("<div class='cell-kv'><div>验证码</div> <input type='text' id='verify' /></div>");
    var button_verify = $('<div></div>');
    button_verify.addClass('button button-major button-small');
    button_verify.text('获取');
    button_verify.bind('click', function() {
        var phone = $('#phone').val();
        if (!verify_phone(phone)) return;

        if (button_verify.hasClass('button-disable')) return;

        button_verify.removeClass('button-major');
        button_verify.addClass('button-disable');
        var times = 60;
        var timer = setInterval(function() {
            button_verify.text('重试('+times+')');
            times--;
            if (times < 0) {
                button_verify.removeClass('button-disable');
                button_verify.addClass('button-major');
                button_verify.text('获取');
                clearInterval(timer);
            }
        }, 1000);

        fomjar.net.send(fomjar.net.ISIS.INST_ECOM_UPDATE_PLATFORM_ACCOUNT_MAP, {phone : phone}, function(code, desc) {
            if (0 != code) {
                wechat.show_toast('获取失败，请稍后重试', 10000);
                return;
            }
        });
    });

    $(list.children()[2]).append(button_verify);

    $('.wechat .frame .body').append(list);
}

function build_buttons() {
    var button = $('<div></div>');
    button.addClass('button button-major button-large');
    button.text('提交');
    button.bind('click', function() {
        var psn     = $('#psn').val(); // can be null
        var phone   = $('#phone').val();
        var verify  = $('#verify').val();

        if (!verify_phone(phone)) return;

        if (0 == verify.length) {
            wechat.show_toast('验证码不能为空', 1500);
            return;
        }
        wechat.show_toast('正在处理...');
        fomjar.net.send(fomjar.net.ISIS.INST_ECOM_UPDATE_PLATFORM_ACCOUNT_MAP, {psn_user : psn, phone : phone, verify : verify}, function(code, desc) {
            wechat.hide_toast();
            if (0 == code) {
                wechat.show_toast('验证通过', 1500);
                setTimeout(function() {history.back(-1);}, 1500);
            } else {
                wechat.show_toast(desc, 10000);
            }
        });

    });

    $('.wechat .frame .body').append(button);
}

function verify_phone(phone) {
    if (0 == phone.length) {
        wechat.show_toast('手机不能为空', 1500);
        return false;
    }
    if(!(/^1[3|4|5|7|8]\d{9}$/.test(phone))) {
        wechat.show_toast('手机号码不合法', 1500);
        return false;
    }
    return true;
}


function setup() {
    wechat.show_toast('正在获取...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT_MAP, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {
            wechat.show_toast('获取失败', 10000);
            return;
        }
        $.each(desc, function(i, u) {
            switch (u.channel) {
            case 1: // wechat
                $('#phone').val(u.phone);
                break;
            case 3: // psn
                $('#psn').val(u.user);
                break;
            }
        });
    });
}

