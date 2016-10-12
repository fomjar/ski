
var wechat = {
    channel : function(i) {
        switch (i) {
        case 0:     return '淘宝';
        case 1:     return '微信';
        case 2:     return '支付宝';
        case 3:     return 'PSN';
        default:    return '未知';
        }
    },

    create_list_cell_game : function(game) {
        var cell = $('<div></div>');
        cell.addClass('cell-ild');
    
        var cover = $('<img />');
        cover.attr('src', game.url_icon);
        var name = $('<div></div>');
        name.addClass('lab');
        name.text(game.name_zh_cn);
        var rent_a = $('<div></div>');
        rent_a.addClass('m2')
        rent_a.text('认证');
        if (game.rent_avail_a)  rent_a.addClass('button button-major');
        else                    rent_a.addClass('button button-disable');
        var rent_b = $('<div></div>');
        rent_b.addClass('m1');
        if (game.rent_avail_b)  rent_b.addClass('button button-major');
        else                    rent_b.addClass('button button-disable');
        rent_b.text('不认证');
        var intr = $('<div></div>')
        intr.addClass('des');
        intr.text(game.introduction);

        cell.append(cover);
        cell.append(name);
        cell.append(rent_a);
        cell.append(rent_b);
        cell.append(intr);

        return cell;
    },

    create_list_cell_order_now : function(c) {
    },

    create_tab : function(tabs) {
        var tab = $('<div></div>');
        tab.addClass('tab');

        var body = $('<div></div>');
        $.each(tabs, function(i, t) {
            var b = $('<div></div>');
            b.append(t.body);
            body.append(b);
        });

        var select = 0;
        var head = $('<div></div>');
        var tab_name_width = 100 / tabs.length;
        $.each(tabs, function(i, t) {
            if (t.select) select = i;

            var h = $('<div></div>');
            h.css('width', tab_name_width + '%');
            h.text(t.head);
            h.bind('click', function() {
                $.each(head.children(), function(j, he) {
                    he = $(he);
                    he.removeClass('ac');
                    he.addClass('da');
                });
                h.removeClass('da');
                h.addClass('ac');

                $.each(body.children(), function(j, b) {
                    b = $(b);
                    b.css('opacity', '0');
                    setTimeout(function() {b.hide();}, 200);
                });
                setTimeout(function() {
                    $(body.children()[i]).show();
                    $(body.children()[i]).css('opacity', '1');
                }, 200);
            })
            head.append(h);
        });

        $(head.children()[select]).trigger('click');

        tab.append([head, body]);

        return tab;
    },

    show_toast : function(text, delay) {
        var toast = $('.toast');
        if (0 == toast.length) {
            toast = $('<div></div>');
            toast.addClass('toast');
            $('body').append(toast);
            toast = $('.toast');
        } else {
            this.hide_toast();
        }
        toast.text(text);
        toast.css('opacity', '1');
        if (null != delay) setTimeout(function() {toast.css('opacity', '0');}, delay);
    },

    hide_toast : function() {
        var toast = $('.toast');
        if (0 == toast.length) return;

        toast.css('opacity', '0');
    }
};

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('dom', build_frame_head);
fomjar.framework.phase.append('dom', build_frame_body);

function build_frame() {
    $('body').addClass('wechat');

    var frame = $('<div></div>');
    frame.addClass('frame');

    $('body').append(frame);
}

function build_frame_head() {
    var head = $('<div></div>');
    head.addClass('head');

    $('.wechat .frame').append(head);
}

function build_frame_body() {
    var body = $('<div></div>');
    body.addClass('body');

    $('.wechat .frame').append(body);
}

