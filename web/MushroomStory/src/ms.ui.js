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

ms.ui = {};
ms.ui.auto_alpha = function(c) {
    var is_down = false;
    var is_over = false;
    c.alpha = 0.9;
    c.on(Laya.Event.MOUSE_OVER, c, function() {is_over = true;  if (!is_down) c.alpha = 1;});
    c.on(Laya.Event.MOUSE_OUT,  c, function() {is_over = false; if (!is_down) c.alpha = 0.9;});
    c.on(Laya.Event.MOUSE_DOWN, c, function() {c.alpha = 0.9;});
    c.on(Laya.Event.MOUSE_UP,   c, function() {if (is_over) c.alpha = 1; else c.alpha = 0.9;});
    return c;
};
ms.ui.auto_pivot = function(c) {
    c.watch('width',    null, function(p, o, n) {this.pivotX = this.width / 2;});
    c.watch('height',   null, function(p, o, n) {this.pivotY = this.height / 2;});
};

ms.ui.Input = function() {
    var c = new Laya.Sprite();
    var i = new Laya.Input();
    i.align = 'center';
    i.fontSize = g.d.font.ui_major * 1.2;
    i.padding = [i.fontSize / 2, i.fontSize / 2, i.fontSize / 2, i.fontSize / 2];
    i.color = g.d.color.ui_fg;
    c.paint = function() {
        this.graphics.clear();
        this.graphics.drawRoundRect(0, 0, this.width, this.height, g.d.color.ui_rr, g.d.color.ui_bg, g.d.color.ui_bd, g.d.color.ui_lw);
    };
    c.watch('width',    null, function(p, o, n) {this.pivotX = this.width / 2;  i[p] = n;});
    c.watch('height',   null, function(p, o, n) {this.pivotY = this.height / 2; i[p] = n;});
    ms.ui.auto_alpha(c);
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
        var p = this.padding; // u,r,d,l
        this.width  = c.labelSize * c.label.length + p[3] + p[1];
        this.height = c.labelSize * (0 < c.label.length ? 1 : 0) + p[0] + p[2];
        this.graphics.clear();
        if (!c.disabled) this.graphics.drawRoundRect(0, 0, this.width, this.height, g.d.color.ui_rr, g.d.color.ui_bd, g.d.color.ui_bd, g.d.color.ui_lw);
        else this.graphics.drawRoundRect(0, 0, this.width, this.height, g.d.color.ui_rr, g.d.color.ui_da, g.d.color.ui_da, g.d.color.ui_lw);
    };
    ms.ui.auto_pivot(c);
    ms.ui.auto_alpha(c);
    return c;
};

})();
