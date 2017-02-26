
main();

function main() {
    build_world();
    register_event();
    init();
}

function build_world() {
    ch.go.map = new ch.go.Map();
    ch.go.me  = new ch.go.Me();

    Matter.World.add(Matter.engine.world, ch.go.map.body);
    Matter.World.add(Matter.engine.world, ch.go.me.body);
}

function register_event() {
    register_event_move();
    register_event_look();
}

function register_event_move() {
    var body = ch.go.me.body;
    body.layaSprite.on(ch.event.ME_MOVE, this, function(e) {
        if (e.up)           body.move({y : -ch.d.human.force_move});
        else if (e.down)    body.move({y : ch.d.human.force_move});
        else                body.move({y : 0});
        if (e.left)         body.move({x : -ch.d.human.force_move});
        else if (e.right)   body.move({x : ch.d.human.force_move});
        else                body.move({x : 0});
    });

    var max_width   = Laya.stage.width;
    var max_height  = Laya.stage.height;
    Laya.timer.frameLoop(1, this, function() {
        var delta = {x : Laya.stage.width / 2 - ch.go.me.body.position.x, y : Laya.stage.height / 2 - ch.go.me.body.position.y};
        var move_map = {x : delta.x, y : delta.y};
        move_map.x *= Math.abs(move_map.x) / max_width / ch.d.map.slop_factor;
        move_map.y *= Math.abs(move_map.y) / max_height / ch.d.map.slop_factor;
        var move_me = {x : - delta.x + move_map.x, y : - delta.y + move_map.y};
        Matter.Body.setPosition(ch.go.me.body, {x : Laya.stage.width / 2 + move_me.x, y : Laya.stage.height / 2 + move_me.y});
        Matter.Body.setPosition(ch.go.map.body, {x : ch.go.map.body.position.x + move_map.x, y : ch.go.map.body.position.y + move_map.y});
    });
}

function register_event_look() {
    var body = ch.go.me.body;
    body.layaSprite.on(ch.event.ME_LOOK, this, function(e) {
        Matter.Body.setAngle(body, body.look.angle);   // 默认向右
    });
}

function init() {
    ch.go.map.rebuild(function() {
        var me = ch.go.me;
        var map = ch.go.map;
        me.body.force.x = 0.25;
        me.body.force.y = 0.58;
        me.body.look = {x : map.width / 2, y : 1e9, angle : Math.PI / 2};
    });
}
