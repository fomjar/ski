
main();

function main() {
    build_world();
    register_event();
}

function build_world() {
    ch.bean.me  = new ch.bean.Me();
    ch.bean.map = new ch.bean.Map();

    Matter.World.add(Matter.engine.world, ch.bean.map.body);
    Matter.World.add(Matter.engine.world, ch.bean.me.body);
}

function register_event() {
    Laya.timer.once(1000, this, function() {
        register_event_move();
        register_event_look();
    });
}

function register_event_move() {
    var body = ch.bean.me.body;
    body.layaSprite.on(ch.event.ME_MOVE, this, function(e) {
        if (e.up)           body.move({y : -ch.d.human.force_move});
        else if (e.down)    body.move({y : ch.d.human.force_move});
        else                body.move({y : 0});
        if (e.left)         body.move({x : -ch.d.human.force_move});
        else if (e.right)   body.move({x : ch.d.human.force_move});
        else                body.move({x : 0});
    });
}

function register_event_look() {
    var body = ch.bean.me.body;
    body.layaSprite.on(ch.event.ME_LOOK, this, function(e) {
        Matter.Body.setAngle(body, body.look.angle);   // 默认向右
    });
}
