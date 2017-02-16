var ch = {};

ch.d = {
    stage_width         : 800,
    stage_height        : 800,
    stage_background    : '#333333',
    body_density        : 0.001,
    body_friction       : 0.08,
    body_friction_air   : 0.08,
    body_friction_static: 0.2,
    body_restitution    : 0.5,
    body_width          : 20,
    body_height         : 30,
    body_angle          : Math.PI / 2,
    body_fill           : '#999999',
    body_stroke         : '#cccccc',
    body_line           : 1,
    move_force          : 0.0003
};

ch.bean = {};
ch.bean.group = {
    '-10' : Matter.Body.nextGroup(true),
    '-9'  : Matter.Body.nextGroup(true),
    '-8'  : Matter.Body.nextGroup(true),
    '-7'  : Matter.Body.nextGroup(true),
    '-6'  : Matter.Body.nextGroup(true),
    '-5'  : Matter.Body.nextGroup(true),
    '-4'  : Matter.Body.nextGroup(true),
    '-3'  : Matter.Body.nextGroup(true),
    '-2'  : Matter.Body.nextGroup(true),
    '-1'  : Matter.Body.nextGroup(true),
    '0'   : Matter.Body.nextGroup(true),
    '1'   : Matter.Body.nextGroup(true),
    '2'   : Matter.Body.nextGroup(true),
    '3'   : Matter.Body.nextGroup(true),
    '4'   : Matter.Body.nextGroup(true),
    '5'   : Matter.Body.nextGroup(true),    // object
    '6'   : Matter.Body.nextGroup(true),
    '7'   : Matter.Body.nextGroup(true),
    '8'   : Matter.Body.nextGroup(true),
    '9'   : Matter.Body.nextGroup(true),
    '10'  : Matter.Body.nextGroup(true)
};

ch.bean.Object = function(id, name) {
    this.id     = id    || 0;
    this.name   = name  || 'null';
    this.icon   = new Laya.Sprite();
    this.bodies = Matter.Composite.create();

    var bodies = this.bodies;
    this.bodies.add_body = function(body, options) {
        if (!options)           options = {};
        if (!options.render)    options.render = {};

        Matter.Body.setDensity  (body, options.density  || ch.d.body_density);
        Matter.Body.setAngle    (body, options.angle    || ch.d.body_angle);
        body.friction               = options.friction              || ch.d.body_friction;
        body.frictionAir            = options.frictionAir           || ch.d.body_friction_air;
        body.frictionStatic         = options.frictionStatic        || ch.d.body_friction_static;
        body.restitution            = options.restitution           || ch.d.body_restitution;
        body.render.fillStyle       = options.render.fillStyle      || ch.d.body_fill;
        body.render.strokeStyle     = options.render.strokeStyle    || ch.d.body_stroke;
        body.render.lineWidth       = options.render.lineWidth      || ch.d.body_line;
        body.render.sprite.texture  = true; // care angle
        body.layaSprite = new Laya.Sprite();
        body.layaSprite.paint = function() {};  // to override

        Matter.Composite.addBody(bodies, body);
        return body;
    };
    this.bodies.paint = function() {
        for (var i in bodies.bodies) {
            var body = bodies.bodies[i];
            if (body.layaSprite) {
                var sprite = body.layaSprite;
                if (sprite.paint) {
                    sprite.graphics.clear();
                    sprite.paint();
                }
            }
        }
    };
};

ch.bean.Map = function() {
    ch.bean.Object.apply(this, arguments);

    this.bodies.add_body(this.top      = Matter.Bodies.rectangle(ch.d.stage_width / 2, 0, ch.d.stage_width, 1, {isStatic : true}));
    this.bodies.add_body(this.bottom   = Matter.Bodies.rectangle(ch.d.stage_width / 2, ch.d.stage_height, ch.d.stage_width, 1, {isStatic : true}));
    this.bodies.add_body(this.left     = Matter.Bodies.rectangle(0, ch.d.stage_height / 2, 1, ch.d.stage_height, {isStatic : true}));
    this.bodies.add_body(this.right    = Matter.Bodies.rectangle(ch.d.stage_width, ch.d.stage_height / 2, 1, ch.d.stage_height, {isStatic : true}));
};

ch.bean.Human = function(id, name) {
    ch.bean.Object.apply(this, arguments);

    var body = Matter.Bodies.rectangle(100, 100, ch.d.body_width, ch.d.body_height, {collisionFilter : {group : ch.bean.group['5']}})
    this.bodies.add_body(body);
    this.bodies.body = body;

    var sprite = body.layaSprite;
    sprite.paint = function() {
        sprite.graphics.drawRect(
            - ch.d.body_width / 2,
            - ch.d.body_height / 2,
            ch.d.body_width / 3 * 2,
            ch.d.body_height,
            body.render.fillStyle,
            body.render.strokeStyle,
            body.render.lineWidth);
        sprite.graphics.drawRect(
            - ch.d.body_width / 6,
            - ch.d.body_height / 4,
            ch.d.body_width / 3 * 2,
            ch.d.body_height / 2,
            body.render.fillStyle,
            body.render.strokeStyle,
            body.render.lineWidth);
    };

    this.bodies.paint();
    this.bodies.body.move = function(force) {
        if (!force) return;

        this.force.x = force.x || this.force.x;
        this.force.y = force.y || this.force.y;
    };
};

ch.bean.Me = function() {
    ch.bean.Human.apply(this, arguments);
    
    this.bodies.body.look = {x : Laya.stage.width / 2, y : 1e9, angle : Math.PI / 2};
};

ch.bean.Item = function() {
    ch.bean.Object.apply(this, arguments);
};

ch.event = {};

ch.event.ME_MOVE = 'me move';
ch.event.ME_LOOK = 'me look';
ch.event.EventDispatcher = function() {
    var up      = false;
    var down    = false;
    var left    = false;
    var right   = false;
    var debug1 = ch.debug.open('物体');
    var debug2 = ch.debug.open('精灵');
    Laya.stage.on(Laya.Event.KEY_DOWN, this, function(e) {
        switch (e.keyCode) {
        case Laya.Keyboard.W: up    = true; break;
        case Laya.Keyboard.S: down  = true; break;
        case Laya.Keyboard.A: left  = true; break;
        case Laya.Keyboard.D: right = true; break;
        }
    });
    Laya.stage.on(Laya.Event.KEY_UP, this, function(e) {
        switch (e.keyCode) {
        case Laya.Keyboard.W: up    = false; break;
        case Laya.Keyboard.S: down  = false; break;
        case Laya.Keyboard.A: left  = false; break;
        case Laya.Keyboard.D: right = false; break;
        }
    });
    var debug3 = ch.debug.open('鼠标');
    var debug4 = ch.debug.open('角度');
    Laya.stage.on(Laya.Event.MOUSE_MOVE, this, function(e) {
        var body = ch.bean.me.bodies.body;
        body.look.x = e.stageX;
        body.look.y = e.stageY;
        body.look.angle = Math.atan2(body.look.y - body.position.y, body.look.x - body.position.x);
        body.layaSprite.event(ch.event.ME_LOOK);
    });
    this.open = function() {
        Laya.timer.frameLoop(1, this, function() {
            var body = ch.bean.me.bodies.body;
            debug1.text = body.position.x.toFixed(4) + ' : ' + body.position.y.toFixed(4);
            debug2.text = body.layaSprite.x.toFixed(4) + ' : ' + body.layaSprite.y.toFixed(4);
            debug3.text = body.look.x + ' : ' + body.look.y;
            debug4.text = body.look.angle.toFixed(4);

            if (up || down || left || right) {
                body.layaSprite.event(ch.event.ME_MOVE, [{
                    up      : up,
                    down    : down,
                    left    : left,
                    right   : right
                }]);

                body.look.angle = Math.atan2(body.look.y - body.position.y, body.look.x - body.position.x);
                body.layaSprite.event(ch.event.ME_LOOK);
            }
        });
    };
};

ch.debug = {};

ch.debug.open = function(label) {
    if (!label) return;

    if (!ch.debug.debugers) ch.debug.debugers = {};
    var length = 0;
    for (var k in ch.debug.debugers) length++;

    var l1 = new Laya.Text();
    l1.fontSize = 16;
    l1.color    = '#ffffff';
    l1.text     = label;
    l1.pos(5, 5 + length * 20);
    var l2 = new Laya.Text();
    l2.fontSize = 16;
    l2.color    = '#ffffff';
    l2.pos(50, 5 + length * 20);

    ch.debug.debugers[label] = l2;

    Laya.stage.addChild(l1);
    Laya.stage.addChild(l2);

    return l2;
}

ch.ui = {};
