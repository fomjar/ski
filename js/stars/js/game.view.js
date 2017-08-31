define(['game.data', 'tween', 'pixi'], function (data, tween, PIXI) {
    
    'use strict';
    
    var view = data;
    
    view.View = function () {
        this.__proto__  = new PIXI.Graphics();
        
        this.data   = new data.Data(this);
        this.interactive    = true;
        this.state  = 'default';
        this.draw   = function () {};
        
        this.auto_scale = function (s) {
            if (!s) s = 1.1;
            
            this.on('pointerover', function () {
                data.tween(this.scale, 'x', s);
                data.tween(this.scale, 'y', s);
                this.state = 'over';
            });
            this.on('pointerout', function () {
                data.tween(this.scale, 'x', 1);
                data.tween(this.scale, 'y', 1);
                this.state = 'default';
            });
            this.on('pointerdown', function () {
                data.tween(this.scale, 'x', 1);
                data.tween(this.scale, 'y', 1);
                this.state = 'down';
            });
            this.on('pointerup', function () {
                data.tween(this.scale, 'x', s);
                data.tween(this.scale, 'y', s);
                this.state = 'over';
            });
            this.on('pointerupoutside', function () {
                data.tween(this.scale, 'x', 1);
                data.tween(this.scale, 'y', 1);
                this.state = 'default'
            });
        };
    };
    
    
    view.VPane = function () {
        this.__proto__ = new view.View();
        
        this.data   = new data.DPane(this);
        this.draw   = function () {};
    };
    
    
    view.VPaneAsset = function () {
        this.__proto__ = new view.VPane();
        
        this.data   = new data.DPaneAsset(this);
        this.draw   = function () {
            var bg  = 0x9999ff;
            var bd  = 0x999999;
            var alpha   = 0.6;

            this.beginFill(bg, alpha);
            this.lineStyle(2, bd, alpha);
            this.drawRect(- this.data.width / 2,
                          - this.data.height / 2,
                          this.data.width,
                          this.data.height);
            this.endFill();
        };
    }
    
    
    view.VButton = function (text, action) {
        this.__proto__ = new view.View();
        
        this.data   = new data.DButton(this);
        this.text   = new PIXI.Text(text);
        this.text.pivot.x = this.text.width / 2;
        this.text.pivot.y = this.text.height / 2;
        this.addChild(this.text);
        this.buttonMode = true;
        
        this.click  = function (action) {
            this.on('pointerup', action);
        };
        this.click(action);
        
        this.auto_scale();
    };
    
    
    view.VButtonPrimary = function (text, action) {
        this.__proto__ = new view.VButton(text, action);
        
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
            this.lineStyle(2, bd, this.data.alpha);
            this.drawRoundedRect(- this.data.width / 2,
                                 - this.data.height / 2,
                                 this.data.width,
                                 this.data.height, 6);
            this.endFill();
        }
    }
    

    
    view.VStar = function (meta) {
        this.__proto__  = new view.View();
        
        this.data   = new data.DStar(this, meta);
        this.radius = Math.random() * 10 + 15;
        this.draw   = function () {view.draw.star[meta](this, this.data);};
        
        this.auto_scale();
    };
    
    
    view.draw = {
        star    : {
            home    : function (g) {
                var bg  = 0xffff99;
                var bd  = 0x999999;
                
                g.beginFill(bg, 1);
                g.lineStyle(2, bd, 1);
                g.drawCircle(0, 0, g.radius);
                g.endFill();
            },
        },
    };
    
    return view;
});