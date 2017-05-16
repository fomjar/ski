(function() {

Laya.Graphics.prototype.drawRoundRect = function(x, y, w, h, c, fill_color, line_color, line_width) {
    this.drawPath(x, y, [
            ["moveTo", c, 0],
            ["lineTo", w - c, 0],
            ["arcTo", w, 0, w, c, c],
            ["lineTo", w, h - c],
            ["arcTo", w, h, w - c, h, c],
            ["lineTo", c, h],
            ["arcTo", 0, h, 0, h - c, c],
            ["lineTo", 0, c],
            ["arcTo", 0, 0, c, 0, c],
            ["closePath"]
        ],
        {fillStyle : fill_color},
        {strokeStyle : line_color, lineWidth : line_width});
};

Laya.Sprite.prototype.paint = function() {};
Laya.Sprite.prototype.tween_from = function(kvs, time) {
    if (!time) time = ms.ui.DELAY_ANIMATE;
    return Laya.Tween.from(this, kvs, time);
};
Laya.Sprite.prototype.tween_to = function(kvs, time) {
    if (!time) time = ms.ui.DELAY_ANIMATE;
    return Laya.Tween.to(this, kvs, time);
};
Laya.Sprite.prototype.show = function(options) {
    if (!options) options = {};
    if (!options.parent) options.parent = Laya.stage;
    options.parent.addChild(this);
    this.paint();
    this.tween_from({alpha : 0});
    if (options.done) Laya.timer.once(ms.ui.DELAY_ANIMATE, this, options.done);
    if (options.time) Laya.timer.once(options.time, this, Laya.Sprite.prototype.hide);
};
Laya.Sprite.prototype.hide = function(options) {
    if (!options) options = {};
    if (undefined == options.remove) options.remove = true;
    var a = this.alpha;
    this.tween_to({alpha : 0});
    Laya.timer.once(ms.ui.DELAY_ANIMATE, this, function() {
        if (options.remove) {
            this.removeSelf();
            this.alpha = a;
        }
        if (options.done) options.done.call(this);
    });
};

Laya.Sprite.prototype.auto_alpha = function() {
    var time = ms.ui.DELAY_ANIMATE / 2;
    var is_down = false;
    var is_over = false;
    this.alpha = 0.9;
    this.on(Laya.Event.MOUSE_OVER, this, function() {is_over = true;  if (!is_down) this.tween_to({alpha : 0.8}, time);});
    this.on(Laya.Event.MOUSE_OUT,  this, function() {is_over = false; if (!is_down) this.tween_to({alpha : 0.9}, time);});
    this.on(Laya.Event.MOUSE_DOWN, this, function() {this.tween_to({alpha : 1}, time);});
    this.on(Laya.Event.MOUSE_UP,   this, function() {if (is_over) this.tween_to({alpha : 0.8}, time); else this.tween_to({alpha : 0.9}, time);});
    return this;
};
Laya.Sprite.prototype.auto_pivot = function() {
    this.watch('width',    null, function(p, o, n) {this.pivotX = this.width / 2;});
    this.watch('height',   null, function(p, o, n) {this.pivotY = this.height / 2;});
    return this;
};

ms.ui = {};

ms.ui.DELAY_ANIMATE = 400;
ms.ui.FONT = new Laya.BitmapFont();

ms.ui.Text = function() {
    var c = new Laya.Text();
    c.align = 'center';
    c.valign = 'middle';
    c.fontSize = g.d.font.ui_major;
    c.padding = [c.fontSize / 2, c.fontSize, c.fontSize / 2, c.fontSize];
    return c;
};
ms.ui.Input = function() {
    var c = new Laya.Sprite();
    var i = new Laya.Input();
    i.align = 'center';
    i.fontSize = g.d.font.ui_major * 1.2;
    i.padding = [i.fontSize / 2, i.fontSize, i.fontSize / 2, i.fontSize];
    i.color = g.d.color.ui_fg;
    c.paint = function() {
        this.graphics.clear();
        this.graphics.drawRoundRect(0, 0, this.width, this.height, g.d.color.ui_rr, g.d.color.ui_bg, g.d.color.ui_bd, g.d.color.ui_lw);
    };
    c.watch('width',    null, function(p, o, n) {this.pivotX = this.width / 2;  i[p] = n;});
    c.watch('height',   null, function(p, o, n) {this.pivotY = this.height / 2; i[p] = n;});
    c.auto_alpha();
    c.addChild(c.input = i);
    return c;
};
ms.ui.Button = function() {
    var c = new Laya.Button();
    c.labelSize     = g.d.font.ui_major;
    c.labelColors   = g.d.color.ui_fg + ',' + g.d.color.ui_fg + ',' + g.d.color.ui_fg + ',' + g.d.color.ui_fg;
    c.padding       = [c.labelSize / 2, c.labelSize, c.labelSize / 2, c.labelSize];
    c.labelPadding  = c.labelSize / 2 + ',' + c.labelSize + ',' + c.labelSize / 2 + ',' + c.labelSize;
    c.paint = function() {
        this.width  = this.labelSize * (this.label.length + 2);
        this.height = this.labelSize * 2;
        this.graphics.clear();
        if (!this.disabled) this.graphics.drawRoundRect(0, 0, this.width, this.height, this.height / 2, g.d.color.ui_bd, g.d.color.ui_bd, g.d.color.ui_lw);
        else this.graphics.drawRoundRect(0, 0, this.width, this.height, this.height / 2, g.d.color.ui_da, g.d.color.ui_da, g.d.color.ui_lw);
    };
    c.auto_pivot();
    c.auto_alpha();
    return c;
};
ms.ui.ProgressBar = function() {
    var c = new Laya.ProgressBar();
    c.height = g.d.font.ui_major;
    c.value = 0;
    c.paint = function() {
        this.graphics.clear();
        this.graphics.drawRoundRect(0, 0, this.width, this.height, this.height / 2, g.d.color.ui_bg, g.d.color.ui_bd, g.d.color.ui_lw);
        if (this.width * this.value >= this.height)
            this.graphics.drawRoundRect(0, 0, this.width * this.value, this.height, this.height / 2, g.d.color.ui_bd, g.d.color.ui_bd, g.d.color.ui_lw);
    }
    c.watch('value', function(p, o, n) {
        if (n >= 1) return 1;
        if (n <= 0) return 0;
        return n;
    }, function(p, o, n) {this.paint();});
    c.auto_pivot();
    return c;
};
ms.ui.Dialog = function() {
    var c = new Laya.Dialog();
    c.alpha = 0.9;
    c.pos(Laya.stage.width / 2, Laya.stage.height / 2);
    var show = Laya.Sprite.prototype.show;
    c.show = function() {
		this.dragArea = "0,0," + this.width + "," + this.height;

        this.graphics.clear();
        this.graphics.drawRoundRect(0, 0, this.width, this.height, g.d.color.ui_rr, g.d.color.ui_bg, g.d.color.ui_bd, g.d.color.ui_lw);

        show.call(this);
    };
    c.auto_pivot();
    return c;
};
ms.ui.Toast = function(text) {
    var c = new Laya.Sprite();
    c.alpha = 0.9;
    c.pos(Laya.stage.width / 2, Laya.stage.height * 2 / 3);
    c.text = function(text) {
        this.removeChildren();
        var t = new ms.ui.Text();
        t.text = text;
        t.color = g.d.color.ui_fg;
        this.size(t.fontSize * (text.length + 2), t.fontSize * 2);
        this.addChild(t);
        return this;
    };
    var show = c.show;
    c.show = function(time) {
        this.graphics.clear();
        this.graphics.drawRoundRect(0, 0, this.width, this.height, this.height / 2, g.d.color.ui_bg, g.d.color.ui_bg, 0);

        show.call(this);

        if (!time) time = 2000;
        Laya.timer.once(time, c, function() {this.hide();});
    };
    c.auto_pivot();

    if (text) c.text(text);
    return c;
}

})();
