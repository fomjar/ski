
main();

function main() {
    build_launch();
    build_world();
    build_human();
}

function build_launch() {

}

function build_world() {
    ch.go.map = new ch.go.Map();
    Matter.World.add(Matter.engine.world, ch.go.map.body);

    ch.go.map.rebuild(function(width, height) {
        var me = ch.go.me;
        var map = ch.go.map;

        Matter.Body.setPosition(map.body, {x : - width / 11.8, y : - height / 2.6})
        me.body.look = {x : map.width / 2, y : 1e9, angle : Math.PI / 2};
    });
}

function build_human() {
    ch.go.me  = new ch.go.Me();
    Matter.World.add(Matter.engine.world, ch.go.me.body);

    ch.go.me.register_event();
}
