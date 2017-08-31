define(['tween'], function (tween) {
    
    'use strict';
    
    var data = {};
    
    data.BaseData = function () {
        this.x  = 0;
        this.y  = 0;
        this.is_tween = false;
    };
    data.BaseData.prototype.tick    = function () {};
    data.BaseData.prototype.tween   = function (dp, to, tm, cb) {
        if (this.is_tween) return;
        
        if (!cb) cb = tween.Linear;
        if (!tm) tm = 6;
        
        var self = this;
        var from = this[dp];
        var time = 0;
        
        if (from == to) return;
        
        self.is_tween = true;
        var tween_tick = function (delta) {
            var curr = cb(time += delta, 0, to - from, tm);
            self[dp] += curr;
            if ((from < to && curr >= to - from) || (from > to && curr <= to - from)) {
                self.is_tween = false;
                self[dp] = to;
                document.app.ticker.remove(tween_tick);
            }
        };
        document.app.ticker.add(tween_tick);
    };
    
    data.Star = function () {
        this.color_bd   = 0;
        this.color_bg   = 0;
        this.alpha  = 1;
        this.radius = 0;
        this.scale  = 1;
        
        this.random_style();
    };
    data.Star.prototype = new data.BaseData();
    data.Star.prototype.random_style = function () {
        this.random_color();
        this.random_size();
    };
    data.Star.prototype.random_color = function () {
        this.color_bg   = Math.floor(Math.random() * 0x555555) + 0xAAAAAA;  // light
        this.color_bd   = Math.floor(Math.random() * 0x222222) + 0x666666;  // dark
    };
    data.Star.prototype.random_size = function () {
        this.radius = Math.random() * 40 + 100;
    };
    
    return data;
});