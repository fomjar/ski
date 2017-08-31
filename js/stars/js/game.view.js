define(['game.data', 'tween', 'pixi'], function (data, tween, PIXI) {
    
    'use strict';
    
    var view = data;
    
    view.View = function () {
        this.__proto__  = new PIXI.Graphics();
        
        this.data   = new data.Data();
        this.interactive    = true;
//        this.buttonMode     = true;
        this.state  = 'default';
        this.draw = function () {};
        
        this.auto_scale = function (s) {
            if (!s) s = 1.05;
            
            this.on('pointerover',      function () {
                this.data.tween('scale', s);
                this.state = 'over';
            });
            this.on('pointerout',       function () {
                this.data.tween('scale', 1);
                this.state = 'default';
            });
            this.on('pointerdown',      function () {
                this.data.tween('scale', 1);
                this.state = 'down';
            });
            this.on('pointerup',        function () {
                this.data.tween('scale', s);
                this.state = 'over';
            });
            this.on('pointerupoutside', function () {
                this.data.tween('scale', 1);
                this.state = 'default'
            });
        };
    };
    
    
    view.VPane = function () {
        this.__proto__ = new view.View();
        
        this.data   = new data.DPane();
        this.draw   = function () {};
    };
    
    
    view.VPaneAsset = function () {
        this.__proto__ = new view.VPane();
        
        this.data   = new data.DPaneAsset();
        this.draw   = function () {
            var bg  = 0x9999ff;
            var bd  = 0x999999;
            var alpha   = 0.6;

            this.beginFill(bg, alpha);
            this.lineStyle(2, bd, alpha);
            this.drawRect(this.data.x - this.data.width / 2 * this.data.scale,
                          this.data.y - this.data.height / 2 * this.data.scale,
                          this.data.width * this.data.scale,
                          this.data.height * this.data.scale);
            this.endFill();
        };
    }
    
    
    view.VButton = function () {
        this.__proto__ = new view.View();
        
        this.data   = new data.DButton();
        this.click  = function (action) {
            this.on('pointerup', action);
        };
        
        this.auto_scale();
    };
    
    
    view.VButtonPrimary = function (text, action) {
        this.__proto__ = new view.VButton();
        
        this.data.width     = 60;
        this.data.height    = 24;
        this.draw = function () {
            var bg  = 0xff9999;
            var bd  = 0xcccccc;
            switch (this.state) {
            case 'over':
                bg  = 0xffaaaa;
                bd  = 0xdddddd;
                break;
            case 'down':
                bg  = 0xff8888;
                bd  = 0xbbbbbb;
                break;
            }

            this.beginFill(bg, this.data.alpha);
            this.lineStyle(1, bd, this.data.alpha);
            this.drawRoundedRect(this.data.x - this.data.width / 2 * this.data.scale,
                          this.data.y - this.data.height / 2 * this.data.scale,
                          this.data.width * this.data.scale,
                          this.data.height * this.data.scale, 6);
            this.endFill();
        }
        
        if (text)   this.data.text = text;
        if (action) this.click(action);
    }
    

    
    view.VStar = function (meta) {
        this.__proto__  = new view.View();
        
        this.data   = new data.DStar(meta);
        this.draw   = function () {view.draw.star[meta](this, this.data);};
        
        this.auto_scale();
    };
    
    
    view.draw = {
        star    : {
            home    : function (g, d) {
                var bg  = 0xffff99;
                var bd  = 0x999999;
                
                g.beginFill(bg, 1);
                g.lineStyle(2, bd, 1);
                g.drawCircle(d.x, d.y, d.radius * d.scale);
                g.endFill();
            },
        },
    };
    
    return view;
});