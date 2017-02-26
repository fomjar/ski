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
        human   : 1,
        map     : 0,
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
        scale       : 100,
    }
};

ch.go = {};

ch.go.GO = function() {
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
    ch.go.GO.apply(this, arguments);

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

    this.body.look = {x : 0, y : 0, angle : Math.PI / 2};

    this.body.layaSprite.zOrder = ch.d.zorder.human;
    this.body.layaSprite.pivot(human.head / 2, human.head * 3 / 2);
    this.body.layaSprite.paint = function() {
        this.graphics.drawRect(0, 0, human.head, human.head * 3, human.render.fillStyle, human.render.strokeStyle, human.render.lineWidth);
        this.graphics.drawRect(human.head / 3, human.head * 0.8, human.head * 1.4, human.head * 1.4, human.render.fillStyle, human.render.strokeStyle, human.render.lineWidth);
    };
    this.body.repaint();
};

ch.go.Me = function() {
    ch.go.Human.apply(this, arguments);
};

ch.go.Map = function() {
    ch.go.GO.apply(this, arguments);

    var map = this;
    this.rebuild = function(cb) {
        Laya.loader.load('map.svg', Laya.Handler.create(this, function(string) {
            var xml = Laya.Utils.parseXMLFromString(string);
            var svg = xml.getElementsByTagName('svg')[0];
            var attr = svg.attributes;
            map.width   = parseInt(attr.width.value.replace('px', '')) * ch.d.map.scale;
            map.height  = parseInt(attr.height.value.replace('px', '')) * ch.d.map.scale;
            for (var ig = 0; ig < svg.children.length; ig++) {
                var g = svg.children[ig];
                for (var ip = 0; ip < g.children.length; ip++) {
                    var p = g.children[ip];
                    map.build_region(p);
                }
            }
            map.body.repaint();

            if (cb) cb();
        }));
    };
    this.build_region = function(p) {
        var sprite = new Laya.Sprite();
        sprite.region = {
            name    : p.tagName,
            attr    : p.attributes,
            color   : p.attributes.fill.value
        };
        sprite.region.points = [];
        var points = sprite.region.attr.points.value.split(' ');
        for (var ip in points) {
            var p = points[ip];
            if (p.length == 0) continue;
            var pa = p.split(',');
            var point = {
                x   : parseFloat(pa[0]) * ch.d.map.scale,
                y   : parseFloat(pa[1]) * ch.d.map.scale
            };
            sprite.region.points.push(point);
        }
        map.body.layaSprite.addChild(sprite);

        var point_array = [];
        for (var ip in sprite.region.points) {
            point_array.push(sprite.region.points[ip].x);
            point_array.push(sprite.region.points[ip].y);
        }
        sprite.paint = function() {
            switch (this.region.name) {
            case 'polygon':
                // this.graphics.drawPoly(0, 0, point_array, null, this.region.color, 1);
                this.graphics.drawPoly(0, 0, point_array, this.region.color);
                break;
            }
        };
    };

    Matter.Body.setStatic(this.body, true);
    this.body.layaSprite.zOrder = ch.d.zorder.map;
    this.body.layaSprite.paint = function() {
        for (var ic in map.body.layaSprite._childs) {
            var sprite = map.body.layaSprite._childs[ic];
            sprite.paint();
        }
    };
};

ch.go.Item = function() {
    ch.go.GO.apply(this, arguments);
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
        if (!ch.go.me) return;

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
