define(['tween'], function (tween) {
    
    'use strict';
    
    var data = {};
    
    data.tween  = function (ta, pr, to, fn, tm) {
        if (!tm) tm = 160;
        if (!fn) fn = tween.Circ.easeOut;

        var from    = ta[pr];
        var time    = 0;
        var begin   = new Date().getTime();

        if (!ta.tweener) ta.tweener = {};
        if (ta.tweener[pr]) {
            document.app.ticker.remove(ta.tweener[pr]);
            delete ta.tweener[pr];
        }
        ta.tweener[pr] = function (delta) {
            time = new Date().getTime() - begin;
            ta[pr] = fn(time, from, to - from, tm);
            if (time >= tm) {
                ta[pr] = to;
                document.app.ticker.remove(ta.tweener[pr]);
                delete ta.tweener[pr];
            }
        };
        document.app.ticker.add(ta.tweener[pr]);
    };

    
    data.Meta = function (type) {
        this.type   = type;
    };
    
    data.MStar = function (meta) {
        this.__proto__ = new data.Meta(meta);
        
        this.weight = 0;
    };
    
    
    data.Data = function (view) {
        this.view   = view;
        this.position   = {x : 0, y : 0};
        this.__defineGetter__('x', function (x) {return this.position.x; });
        this.__defineSetter__('x', function (x) {
            this.position.x = x;
            if (this.view) this.view.position.x = x;
        });
        this.__defineGetter__('y', function (y) {return this.position.y; });
        this.__defineSetter__('y', function (y) {
            this.position.y = y;
            if (this.view) this.view.position.y = y;
        });
        this.meta   = new data.Meta();
        this.tick   = function () {};        
    };
    
    
    data.DPane = function (view) {
        this.__proto__ = new data.Data(view);
        
        this.width  = 0;
        this.height = 0;
    };
    
    data.DPaneAsset = function (view) {
        this.__proto__ = new data.DPane(view);
        
        this.x      = 150;
        this.y      = 20;
        this.width  = this.x * 2;
        this.height = this.y * 2;
    };
    
    
    
    data.DButton = function (view) {
        this.__proto__ = new data.Data(view);
        
        this.width  = 0;
        this.height = 0;
    }
    
    
    
    
    data.DStar = function (view, meta) {
        this.__proto__  = new data.Data(view);
        
        this.meta   = new data['MStar_' + meta](view);
    };
        
    data.MStar_home = function () {
        this.__proto__ = new data.MStar('home');
        
        this.weight = 30;
    };
    
    return data;
});