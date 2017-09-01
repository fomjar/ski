define(['tween'], function (tween) {
    
    'use strict';
    
    let data = {};
    
    data.tween  = function (ta, pr, to, fn, tm) {
        if (!tm) tm = 160;
        if (!fn) fn = tween.Circ.easeOut;

        let from    = ta[pr];
        let time    = 0;
        let begin   = new Date().getTime();

        let app = document.game.app;
        if (!ta.tweener) ta.tweener = {};
        if (ta.tweener[pr]) {
            app.ticker.remove(ta.tweener[pr]);
            delete ta.tweener[pr];
        }
        ta.tweener[pr] = function (delta) {
            time = new Date().getTime() - begin;
            ta[pr] = fn(time, from, to - from, tm);
            if (time >= tm) {
                ta[pr] = to;
                app.ticker.remove(ta.tweener[pr]);
                delete ta.tweener[pr];
            }
        };
        app.ticker.add(ta.tweener[pr]);
    };
    
    data.Data = class Data {
        constructor () {
            this.x      = 0;
            this.y      = 0;
            this.width  = 1;
            this.height = 1;
        }
        xetter (pr, fs, fg) {
            if (1 > arguments.length) throw new Error('illegal arguments count, at least 1');
            
            this['_' + pr] = this[pr];
            this.__defineSetter__(pr, (v) => {
                if (fs) fs(v);
                this['_' + pr] = v;
            });
            this.__defineGetter__(pr, ( ) => {
                let v = this['_' + pr];
                if (fg) fg(v);
                return v;
            });
            this[pr] = this['_' + pr];
        }
        assign (d) {
            Object.assign(this, d);
        }
    };
    data.DPane = class DPane extends data.Data {
        constructor () {
            super();
            this.alpha  = 0.8;
            this.round  = 6;
            this.border = 2;
            this.color_bg   = 0xcccccc;
            this.color_bd   = 0xeeeeee;
        }
    };
    data.DPaneResource = class DPaneResource extends data.DPane {
        constructor () {
            super();
            this.x      = document.game.screen.width / 4 / 2;
            this.y      = 16;
            this.width  = this.x * 2;
            this.height = this.y * 2;
            this.alpha  = 0.4;
            this.border = 2;
            this.color_bg   = 0x9999ff;
            this.color_bd   = 0x999999;
        }
    };
    data.DButton = class DButton extends data.DPane {
        constructor () {super();}
        
        style_icon_small () {
            this.width      = 16;
            this.height     = 16;
            this.border     = 1;
        }
        
        style_icon_middle () {
            this.width      = 24;
            this.height     = 24;
            this.border     = 1;
        }
        
        style_icon_large () {
            this.width      = 32;
            this.height     = 32;
            this.border     = 1;
        }
        
        style_primary () {
            this.width      = 72;
            this.height     = 24;
            this.border     = 2;
            this.color_bg   = 0xff9999;
            this.color_bd   = 0xcccccc;
        }
    };
    data.DStar = class DStar extends data.DPane {
        constructor (type) {
            super();
            this.type   = type;
            this.level  = 1;
            this.radius = 1;
            
            this.xetter('radius', (radius) => {
                this.width  = radius * 2;
                this.height = radius * 2;
            })
            this.xetter('level', (level) => {
                this.radius = 15 + level * 2;
            });
            this.xetter('type', (type) => this.style_type());
            
            this.style_type();
        }
        
        style_type () {
            switch (this.type) {
                case 'home':
                    this.border = 2;
                    this.color_bg   = 0xffff99;
                    this.color_bd   = 0x999999;
                    break;
            }
        }
    };

    return data;
});