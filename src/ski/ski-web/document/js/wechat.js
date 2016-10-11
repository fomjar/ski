
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
        cell.addClass('wechat-list-cell-game');
    
        var cover = $('<img />');
        cover.attr('src', game.url_icon);
        var name = $('<div></div>');
        name.text(game.name_zh_cn);
        var rent_a = $('<div></div>');
        rent_a.text('认证');
        if (game.rent_avail_a)  rent_a.addClass('wechat-button wechat-button-major');
        else                    rent_a.addClass('wechat-button wechat-button-disable');
        var rent_b = $('<div></div>');
        if (game.rent_avail_b)  rent_b.addClass('wechat-button wechat-button-major');
        else                    rent_b.addClass('wechat-button wechat-button-disable');
        rent_b.text('不认证');
        var intr = $('<div></div>')
        intr.text(game.introduction);

        cell.append(cover);
        cell.append(name);
        cell.append(rent_a);
        cell.append(rent_b);
        cell.append(intr);

        return cell;
    },

    show_toast : function(text, delay) {
        var toast = $('.wechat-toast');
        if (0 == toast.length) {
            toast = $('<div></div>');
            toast.addClass('wechat-toast');
            $('body').append(toast);
            toast = $('.wechat-toast');
        } else {
            this.hide_toast();
        }
        toast.text(text);
        toast.css('opacity', '1');
        if (null != delay) setTimeout(function() {toast.css('opacity', '0');}, delay);
    },

    hide_toast : function() {
        var toast = $('.wechat-toast');
        if (0 == toast.length) return;

        toast.css('opacity', '0');
    }
};

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('dom', build_frame_navigator);
fomjar.framework.phase.append('dom', build_frame_content);

function build_frame() {
    var frame = $('<div></div>');
    frame.addClass('wechat-frame');

    $('body').append(frame);
}

function build_frame_navigator() {
    var navigator = $('<div></div>');
    navigator.addClass('wechat-frame-navigator');

    $('.wechat-frame').append(navigator);
}

function build_frame_content() {
    var content = $('<div></div>');
    content.addClass('wechat-frame-content');

    $('.wechat-frame').append(content);
}

