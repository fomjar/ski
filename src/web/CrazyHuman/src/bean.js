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
    Matter.Body.setPosition(this.body, {x : Laya.stage.width / 2, y : Laya.stage.height / 2});

    var me = this;
    this.register_event = function() {
        /***************** define event *****************/
        (function() {
        var up      = false;
        var down    = false;
        var left    = false;
        var right   = false;
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
        Laya.stage.on(Laya.Event.MOUSE_MOVE, this, function(e) {
            var body = me.body;
            body.look.x = e.stageX;
            body.look.y = e.stageY;
            body.look.angle = Math.atan2(body.look.y - body.position.y, body.look.x - body.position.x);
            body.layaSprite.event(ch.event.ME_LOOK);
        });

        Laya.timer.frameLoop(1, this, function() {
            if (up || down || left || right) {
                me.body.layaSprite.event(ch.event.ME_MOVE, [{
                    up      : up,
                    down    : down,
                    left    : left,
                    right   : right
                }]);

                me.body.look.angle = Math.atan2(me.body.look.y - me.body.position.y, me.body.look.x - me.body.position.x);
                me.body.layaSprite.event(ch.event.ME_LOOK);
            }
        });
        }) ();

        /***************** apply event *****************/
        (function() {
        // move
        me.body.layaSprite.on(ch.event.ME_MOVE, this, function(e) {
            if (e.up)           me.body.move({y : - ch.d.human.force_move});
            else if (e.down)    me.body.move({y : ch.d.human.force_move});
            else                me.body.move({y : 0});
            if (e.left)         me.body.move({x : - ch.d.human.force_move});
            else if (e.right)   me.body.move({x : ch.d.human.force_move});
            else                me.body.move({x : 0});
        });
        // look
        me.body.layaSprite.on(ch.event.ME_LOOK, this, function(e) {
            Matter.Body.setAngle(me.body, me.body.look.angle);   // 默认向右
        });
        // view port
        var max_width   = Laya.stage.width;
        var max_height  = Laya.stage.height;
        Laya.timer.frameLoop(1, this, function() {
            var delta = {x : Laya.stage.width / 2 - me.body.position.x, y : Laya.stage.height / 2 - me.body.position.y};
            var move_map = {x : delta.x, y : delta.y};
            move_map.x *= Math.abs(move_map.x) / max_width / ch.d.map.slop_factor;
            move_map.y *= Math.abs(move_map.y) / max_height / ch.d.map.slop_factor;
            var move_me = {x : - delta.x + move_map.x, y : - delta.y + move_map.y};
            Matter.Body.setPosition(me.body, {x : Laya.stage.width / 2 + move_me.x, y : Laya.stage.height / 2 + move_me.y});
            if (ch.go.map) {
                var map = ch.go.map;
                Matter.Body.setPosition(map.body, {x : map.body.position.x + move_map.x, y : map.body.position.y + move_map.y});
            }
        });
        }) ();
    };
};

ch.go.Map = function() {
    ch.go.GO.apply(this, arguments);

    var map = this;
    var build_region = function(p) {
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
                this.graphics.drawPoly(0, 0, point_array, this.region.color);
                break;
            }
        };
    };
    this.clear = function() {
    };
    this.rebuild = function(cb) {
        this.clear();
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
                    build_region(p);
                }
            }
            map.body.repaint();
            if (cb) cb();
        }));
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
