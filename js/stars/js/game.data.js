define(['tween'], function (tween) {
    
    'use strict';
    
    let data = {};
    
    data.Data = class Data {
        constructor () {
            this.x      = 0;
            this.y      = 0;
            this.width  = 1;
            this.height = 1;
            this.scale  = 1;
            this.scale_view = 1;
        }
        tween   (pr, to, tm, dn)    {Data.tween (this, pr, to, tm, dn);}
        on_get  (pr, fn)            {Data.on_get(this, pr, fn);}
        on_set  (pr, fs, fg)        {Data.on_set(this, pr, fs, fg);}
        bindi   (sr, pr, fn)        {Data.bind  (this, sr, pr, fn);}
        bindo   (ta, pr, fn)        {Data.bind  (ta, this, pr, fn);}
        static tween (ta, pr, to, tm, dn) {
            if (3 > arguments.length) throw new Error('illegal arguments count, at least 3');
            
            switch (arguments.length) {
                case 4:
                    if ('function' == typeof tm) {
                        dn = tm;
                        tm = null;
                    }
                    break;
            }
            if (!tm) tm = 160;
            
            let fn      = tween.Circ.easeOut;
            let from    = ta[pr];
            let time    = 0;
            let begin   = new Date().getTime();

            let app = document.game.app;
            if (!ta._tweener) ta._tweener = {};
            if (ta._tweener[pr]) {
                app.ticker.remove(ta._tweener[pr]);
                delete ta._tweener[pr];
            }
            ta._tweener[pr] = function (delta) {
                time = new Date().getTime() - begin;
                ta[pr] = fn(time, from, to - from, tm);
                if (time >= tm) {
                    ta[pr] = to;
                    app.ticker.remove(ta._tweener[pr]);
                    delete ta._tweener[pr];
                    if (dn) dn();
                }
            };
            app.ticker.add(ta._tweener[pr]);
        }
        static on_get (ta, pr, fn) {
            if (2 > arguments.length) throw new Error('illegal arguments count, at least 2');

            ta[`_${pr}`] = ta[pr];
            if (!fn) fn = ( ) => ta[`_${pr}`];
            ta.__defineGetter__(pr, ( ) => fn());
        }
        static on_set (ta, pr, fs, fg) {
            if (2 > arguments.length) throw new Error('illegal arguments count, at least 2');

            Data.on_get(ta, pr, fg);
            
            ta.__defineSetter__(pr, (v) => {
                ta[`_${pr}`] = v;
                if (fs) fs(v);
            });
            ta[pr] = ta[`_${pr}`];
        }
        static bind (ta, sr, pr, fn) {
            if (2 > arguments.length) throw new Error('illegal arguments count, at least 2');
            
            switch (arguments.length) {
                case 3:
                    if ('function' == typeof pr) {
                        fn = pr;
                        pr = null;
                    }
                    break;
            }
            if (pr) {
                Data.on_set(sr, pr, (v) => {
                    ta[pr] = v;
                    if (fn) fn(pr, v);
                });
            } else {
                for (pr in sr) {
                    Data.on_set(sr, pr, (v) => {
                        ta[pr] = v
                        if (fn) fn(pr, v);
                    });
                }
            }
        }
    };
    data.DLabel = class DLabel extends data.Data {
        constructor () {
            super();
            this.text   = '';
            this.align  = 'center';
        }
        align_left   () {this.align = 'left';}
        align_center () {this.align = 'center';}
        align_right  () {this.align = 'right';}
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
            
            this.grid   = [];
            let n = 4;
            for (let i = 0; i < n; i++) {
                this.grid[i] = {
                    index : i,
                    name  : '',
                    value : 0,
                    position : {
                        left   : - this.width / 2 + this.width / n * i,
                        right  : - this.width / 2 + this.width / n * (i + 1),
                        center : - this.width / 2 + this.width / n * (i + 0.5)
                    }
                };
            }
        }
    };
    data.DButton = class DButton extends data.DPane {
        constructor () {
            super();
            this.text = '';
        }
        
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
            this.border = 4;
            this.thumb  = true;
            
            this.on_set('radius', (v) => {
                this.width  = v * 2;
                this.height = v * 2;
            })
            this.on_set('level', (v) => {
                let screen = document.game.screen;
                this.radius = screen.height / 5 + v * screen.height / 80;
                if ('home' == this.type) this.radius *= 1.2;
            });
            this.on_set('type', (type) => this.style_type());
        }
        
        style_type () {
            switch (this.type) {
                case 'home':
                    this.color_bg   = 0xffff99;
                    this.color_bd   = 0x999999;
                    break;
            }
        }
    };

    return data;
});