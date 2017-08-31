define(['tween'], function (tween) {
    
    'use strict';
    
    var data = {};
    
    
    data.Meta = function (type) {
        this.type   = type;
    };
    
    data.MStar = function (meta) {
        this.__proto__ = new data.Meta(meta);
        
        this.weight = 0;
    };
    
    
    data.Data = function () {
        this.x  = 0;
        this.y  = 0;
        this.scale  = 1;
        this.meta   = new data.Meta();
        this.tick   = function () {};
        this.tween  = function (dp, to, fn, tm) {
            if (!tm) tm = 160;
            if (!fn) fn = tween.Circ.easeOut;

            var self    = this;
            var from    = self[dp];
            var time    = 0;
            var begin   = new Date().getTime();

            if (self.tweener) {
                document.app.ticker.remove(self.tweener);
                delete self.tweener;
            }
            self.tweener = function (delta) {
                time = new Date().getTime() - begin;
                self[dp] = fn(time, from, to - from, tm);
                if (time >= tm) {
                    self[dp] = to;
                    document.app.ticker.remove(self.tweener);
                    delete self.tweener;
                }
            };
            document.app.ticker.add(self.tweener);
        };
    };
    
    
    data.DPane = function () {
        this.__proto__ = new data.Data();
        
        this.width  = 0;
        this.height = 0;
    };
    
    data.DPaneAsset = function () {
        this.__proto__ = new data.DPane();
        
        this.x      = 150;
        this.y      = 20;
        this.width  = this.x * 2;
        this.height = this.y * 2;
    };
    
    
    
    data.DButton = function (text) {
        this.__proto__ = new data.Data();
        
        this.width  = 0;
        this.height = 0;
        this.text   = text || '';
    }
    
    
    
    
    data.DStar = function (meta) {
        this.__proto__  = new data.Data();
        
        this.radius = Math.random() * 10 + 15;
        this.meta   = new data['MStar_' + meta]();
    };
        
    data.MStar_home = function () {
        this.__proto__ = new data.MStar('home');
        
        this.weight = 30;
    };
    
    return data;
});