
(function($) {

fomjar.framework.phase.append('dom', build_foot);
fomjar.framework.phase.append('ren', init_event);
fomjar.framework.phase.append('ren', animate);


var MSG_COUNT = 10;
var MSG_COUNT_FIRST = MSG_COUNT * 2;


function build_foot() {
    var send = $('<div></div>');
    send.addClass('send');
    
    send.bind('click', sn.msg.new);
    
    sn.ui.longtouch(send, sn.act.new);
    
    $('.sn .foot').append(send);
}

function init_event() {
    // scroll event
    sn.ui.scroll($('.sn .body'),
        function() {    // begin
//             $('.sn .head').css('top', '-3em');
//             if (sn.user) $('.sn .foot').css('bottom', '-4em');
        },
        function() {    // end
//             $('.sn .head').css('top', '0');
//             if (sn.user) $('.sn .foot').css('bottom', '0');
        },
        function() {    // top
//             $('.sn .head').css('top', '0');
//             if (sn.user) $('.sn .foot').css('bottom', '0');
        },
        function() {    // bottom
            load_message(sn.msg.data.length, MSG_COUNT);
        },
        200);
    
    // pull event
    sn.ui.pull($('.sn .body'),
        function() {    // down
            load_activity();
            load_message(0, MSG_COUNT_FIRST);
        },
        function() {},  // up
        80);

}

function animate() {
    // revert
    var bg_img  = $('.sn .bg >img');
    var bg_mask = $('.sn .bg >div');
    var sn_head = $('.sn .head');
    
    bg_img.css('opacity', '0');
    bg_img.css('width', '100%');
    bg_img.css('height', '100%');
    bg_img.css(        'filter', 'none');
    bg_img.css('-webkit-filter', 'none');
    
    bg_mask.css('opacity', '0');
    
    sn_head.css('top', '-3em');
    
    // animate
    bg_img.bind('load', function() {
        bg_img.css('opacity', '1');
        
        setTimeout(function() {
            bg_img.css('width',  '105%');
            bg_img.css('height', '105%');
            
            setTimeout(function() {
                bg_img.css(        'filter', 'blur(.3em)');
                bg_img.css('-webkit-filter', 'blur(.3em)');
                
                bg_mask.css('opacity', '.3');
                
                sn_head.css('top', '0');
                
                animate_done();
            }, 3000);
        }, 500);
    });
}

function animate_done() {
    load_activity();
    load_message(0, MSG_COUNT_FIRST);
    
    if (sn.user) $('.sn .foot').addClass('foot-appear');
    
    sn.stub.login.push(function() {
        $('.sn .foot').addClass('foot-appear');
        load_activity();
        load_message(0, MSG_COUNT_FIRST);
    });
    sn.stub.logout.push(function() {
        $('.sn .foot').removeClass('foot-appear');
        load_activity();
        load_message(0, MSG_COUNT_FIRST);
    });
}

function load_activity() {
    if (!sn.location) return;
    
    sn.act.data = [];
    sn.act.load(function(activities) {
        var delay = 0;
        $.each(activities, function(i, data) {
            var exist = false;
            $.each(sn.act.data, function(i, a0) {
                if (data.aid == a0.data.aid) {
                    exist = true;
                    return false;
                }
            });
            if (exist) return true;
            
            var act = sn.act.wrap(data);
            sn.act.data.push(act);
            setTimeout(function() {
                act.ui.panel.css('opacity', '0');
                $('.sn .body').prepend(act.ui.panel);
                setTimeout(function() {act.ui.panel.css('opacity', '1');}, 0);
            }, delay);
            
            delay += 50;
        });
    });
}


function load_message(pos, len) {
    if (!sn.location) return;
    
    sn.ui.toast('正在加载', 1000 * 10);
    if (0 == pos) {
        $('.sn .body').children().remove();
        sn.msg.data = [];
    }
    sn.msg.load(pos, len, function(msgs) {
        if (0 == msgs.length) {
            sn.ui.toast('已没有消息');
            return;
        }
        sn.ui.toast('加载完成', 1000);
        var delay = 0;
        $.each(msgs, function(i, data) {
            var exist = false;
            $.each(sn.msg.data, function(i, msg) {
                if (msg.data.mid == data.mid) {
                    exist = true;
                    return false;
                }
            });
            if (exist) return true;

            var msg = sn.msg.wrap(data);
            sn.msg.data.push(msg);
            
            setTimeout(function() {
                msg.ui.panel.css('opacity', '0');
                $('.sn .body').append(msg.ui.panel);
                setTimeout(function() {msg.ui.panel.css('opacity', '1');}, 0);
            }, delay);
            
            delay += 100;
        });
    });
}

})(jQuery)

