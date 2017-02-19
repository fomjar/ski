var ch = {};

ch.d = {
    collision : {
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
    },
    zorder : {
        debug   : 5,
        ui      : 4,
    },
    stage : {
        width       : 1280,
        height      : 800,
        background  : '#333333',
    },
    human : {
        head            : 10,
        angle           : Math.PI / 2,
        density         : 0.001,
        restitution     : 0.5,
        friction        : 0.1,
        frictionAir     : 0.08,
        frictionStatic  : 0.2,
        render : {
            fillStyle   : '#999999',
            strokeStyle : '#cccccc',
            lineWidth   : 1,
        },
        force_move  : 0.0002
    },
    map : {
        slop_factor : 6,
        grid_size   : 20,
    }
};

ch.go = {};

ch.go.Base = function() {
    this.id     = 0;
    this.name   = 'object';
    this.icon   = new Laya.Sprite();
    this.body   = Matter.Body.create({parts : []});
    this.body.render.sprite.texture = true;
    this.body.layaSprite = new Laya.Sprite();
    this.body.layaSprite.paint = function() {};
    this.body.repaint = function() {
        this.layaSprite.graphics.clear();
        this.layaSprite.paint();
    };

    this.body.move = function(force) {
        if (!force) return;
        
        this.force.x = force.x || this.force.x;
        this.force.y = force.y || this.force.y;
    };
};

ch.go.Human = function() {
    ch.go.Base.apply(this, arguments);

    var human = ch.d.human;

    Matter.Body.setParts(this.body, [
        this.body.body = Matter.Bodies.rectangle(- human.head / 2, 0, human.head, human.head * 3),
    ]);

    Matter.Body.setAngle(this.body, human.angle);
    Matter.Body.setDensity(this.body, human.density);
    this.body.restitution       = human.restitution;
    this.body.friction          = human.friction;
    this.body.frictionAir       = human.frictionAir;
    this.body.frictionStatic    = human.frictionStatic;

    this.body.layaSprite.pivot(human.head / 2, human.head * 3 / 2);
    this.body.layaSprite.paint = function() {
        this.graphics.drawRect(0, 0, human.head, human.head * 3, human.render.fillStyle, human.render.strokeStyle, human.render.lineWidth);
        this.graphics.drawRect(human.head / 3, human.head * 0.8, human.head * 1.4, human.head * 1.4, human.render.fillStyle, human.render.strokeStyle, human.render.lineWidth);
    };
    this.body.repaint();
};

ch.go.Me = function() {
    ch.go.Human.apply(this, arguments);
    
    Matter.Body.setPosition(this.body, {x : Laya.stage.width / 2, y : Laya.stage.height / 2});
    this.body.look = {x : Laya.stage.width / 2, y : 1e9, angle : Math.PI / 2};
};

ch.go.Map = function() {
    ch.go.Base.apply(this, arguments);

    var width = ch.d.stage.width;
    var height = ch.d.stage.height;
    var line = 1;
    Matter.Body.setParts(this.body, [
        this.body.wall_top      = Matter.Bodies.rectangle(width / 2, 0, width, line),
        this.body.wall_bottom   = Matter.Bodies.rectangle(width / 2, height, width, line),
        this.body.wall_left     = Matter.Bodies.rectangle(0, height / 2, line, height),
        this.body.wall_right    = Matter.Bodies.rectangle(width, height / 2, line, height),
    ]);
    Matter.Body.setStatic(this.body, true);

    this.body.layaSprite.pivot(width / 2, height / 2);
    this.body.layaSprite.paint = function() {
        this.graphics.drawRect(0, 0, width, height, null, '#ffffff', line);
    };
    this.body.repaint();

    // var map = this;
    // this.reload = function() {
    //     Laya.loader.load('map.svg', Laya.Handler.create(this, function(string) {
    //         var xml = Laya.Utils.parseXMLFromString(string);
    //         var svg = xml.getElementsByTagName('svg')[0];
    //         var attr = svg.attributes;
    //         map.width   = parseInt(attr.width.value.replace('px', ''));
    //         map.height  = parseInt(attr.height.value.replace('px', ''));
    //         var gs = svg.getElementsByTagName('g');
    //         for (var i in gs) {
    //             var g = gs[i];
    //             switch (g.attributes.id.value) {
    //             case 'dirt':
    //                 break;
    //             case 'grass':
    //                 break;
    //             }
    //         }
    //     }));
    // };
};

ch.go.Item = function() {
    ch.go.Base.apply(this, arguments);
};

ch.event = {};

ch.event.ME_MOVE = 'me move';
ch.event.ME_LOOK = 'me look';
ch.event.Dispatcher = {};
ch.event.Dispatcher.open = function() {
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
        var body = ch.go.me.body;
        body.look.x = e.stageX;
        body.look.y = e.stageY;
        body.look.angle = Math.atan2(body.look.y - body.position.y, body.look.x - body.position.x);
        body.layaSprite.event(ch.event.ME_LOOK);
    });

    Laya.timer.frameLoop(1, this, function() {
        var body = ch.go.me.body;
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

ch.debug = {};

ch.debug.open = function(label) {
    if (!label) return;

    if (!ch.debug.debugers) ch.debug.debugers = {};
    var length = 0;
    for (var k in ch.debug.debugers) length++;

    var l1 = new Laya.Text();
    l1.zOrder   = ch.d.zorder.debug;
    l1.fontSize = 16;
    l1.color    = '#ffffff';
    l1.text     = label;
    l1.pos(5, 5 + length * 20);
    var l2 = new Laya.Text();
    l2.zOrder   = ch.d.zorder.debug;
    l2.fontSize = 16;
    l2.color    = '#ffffff';
    l2.pos(50, 5 + length * 20);

    ch.debug.debugers[label] = l2;

    Laya.stage.addChild(l1);
    Laya.stage.addChild(l2);

    return l2;
}

ch.ui = {};
