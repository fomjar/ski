
var omc = {
    channel : function(i) {
        switch (i) {
        case 0:     return '淘宝';
        case 1:     return '微信';
        case 2:     return '支付宝';
        case 3:     return 'PSN';
        default:    return '未知';
        }
    },
    show_dialog : function (options) {
        var dialog = $('<div></div>');
        dialog.append('<div></div>');   // mask
        dialog.append('<div>'           // dialog
                         +'<div></div>' // head
                         +'<div></div>' // body
                     +'</div>');
        dialog.addClass('dialog');
        var mask = dialog.find('>div:nth-child(1)');
        var real = dialog.find('>div:nth-child(2)');
        var head = dialog.find('>div:nth-child(2) >div:nth-child(1)');
        var body = dialog.find('>div:nth-child(2) >div:nth-child(2)');

        head.bind('mousedown', function(e) {
            var delta = {top : (e.pageY-real.offset().top-real.height()/2), left : (e.pageX-real.offset().left-real.width()/2)};
            head.bind('mousemove', function(e1) {
                real.css({top : e1.pageY-delta.top+'px', left : e1.pageX-delta.left+'px'});
            });
        });
        head.bind('mouseup', function(e) {
            head.unbind('mousemove');
        });

        if (options.head) {
            head.show();
            head.html('');
            head.append(options.head);
        } else {
            head.hide();
        }
        if (options.body) {
            body.html('');
            body.append(options.body);
        }
        if (options.width)  real.css('width',  options.width);
        if (options.height) real.css('height', options.height);

        real.css({left : '50%', top : '50%'});
        $('.omc .frame').css(        'filter', 'blur(6px)');
        $('.omc .frame').css('-webkit-filter', 'blur(6px)');
        dialog.show();

        if (options.open) options.open();
        mask.bind('click', function() {
            dialog.remove();
            if (options.close) options.close();

            if (0 == $('.omc .dialog').length) {
                $('.omc .frame').css(        'filter', '');
                $('.omc .frame').css('-webkit-filter', '');
            }
        });

        $('.omc').append(dialog);
        return {
            mask    : mask,
            dialog  : dialog,
            head    : head,
            body    : body
        };
    }
};

(function($) {

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('dom', build_frame_head);
fomjar.framework.phase.append('dom', build_frame_body);

function build_frame() {
    var omc = $('<div></div>');
    omc.addClass('omc');

    var frame = $('<div></div>');
    frame.addClass('frame');

    omc.append(frame);

    $('body').append(omc);
}

function build_frame_head() {
    var head = $('<div></div>');
    head.addClass('head');

    var menu = $('<div></div>');
    menu.addClass('menu');
    var m_game     = menu.clone();
    var m_account  = menu.clone();
    var m_ticket   = menu.clone();
    var m_report   = menu.clone();
    m_game.text('游戏');
    m_account.text('帐号');
    m_ticket.text('工单');
    m_report.text('统计');

    head.append(m_game);
    head.append(m_account);
    head.append(m_ticket);
    head.append(m_report);

    $('.omc .frame').append(head);
}

function build_frame_body() {
    var body = $('<div></div>');
    body.addClass('body');

    $('.omc .frame').append(body);
}


})(jQuery)

