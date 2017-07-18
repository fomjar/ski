
var vcg = {

    channel : function(i) {
        switch (i) {
        case 0:     return '淘宝';
        case 1:     return '微信';
        case 2:     return '支付宝';
        case 3:     return 'PSN';
        default:    return '未知';
        }
    },

    show_toast : function(text, delay) {
        var toast = $('.vcg .toast');
        if (0 == toast.length) {
            toast = $('<div></div>');
            toast.addClass('toast');
            $('.vcg').append(toast);
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
            h.bind('touchend', function() {
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

        $(head.children()[select]).trigger('touchend');

        tab.append([head, body]);

        return tab;
    },

    create_list_cell_game : function(game) {
        var cell = $('<div></div>');
        cell.addClass('cell-ild');

        cell.append("<img src='"+game.url_icon+"' />");
        cell.append("<div class='lab'>"+game.name_zh_cn+"</div>");
        cell.append("<div class='des'>"+game.introduction+"</div>");
        cell.append("<div class='m2 button "+(game.rent_avail_a?'button-major':'button-disable')+"'>认证</div>");
        cell.append("<div class='m1 button "+(game.rent_avail_b?'button-major':'button-disable')+"'>不认证</div>");

        return cell;
    },

    create_list_cell_order_now : function(c) {
        var cell = $('<div></div>');
        cell.addClass('cell-ild');

        var game = c.game;
        cell.append("<img src='"+game.url_icon+"' />");
        cell.append("<div class='lab'>"+game.name_zh_cn+"</div>");
        cell.append("<div class='des'>帐号密码: "+c.account+' / '+c.pass+"<br/>开始时间: "+c.begin+"</div>");

        return cell;
    },

    create_list_cell_order_old : function(c) {
        var cell = $('<div></div>');
        cell.addClass('cell-ild');

        var game = c.game;
        cell.append("<img src='"+game.url_icon+"' />");
        cell.append("<div class='lab'>"+game.name_zh_cn+"</div>");
        cell.append("<div class='des'>时间: "+c.begin.substring(5)+' ~ '+c.end.substring(5)+"<br/>消费: "+c.expense+"元 - "+c.type+' ('+c.price+"元/天)</div>");

        return cell;
    }
};

(function ($) {

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('dom', build_frame_head);
fomjar.framework.phase.append('dom', build_frame_body);

function build_frame() {

    var main = $('<div></div>');
    main.addClass('vcg');

    var frame = $('<div></div>');
    frame.addClass('frame');

    main.append(frame);
    $('body').append(main);

    $('body').bind('touchstart', function() {});
}

function build_frame_head() {
    var head = $('<div></div>');
    head.addClass('head');

    $('.vcg .frame').append(head);
}

function build_frame_body() {
    var body = $('<div></div>');
    body.addClass('body');

    $('.vcg .frame').append(body);
}


})(jQuery)

