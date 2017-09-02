define(['game.data', 'pixi'], function (data, PIXI) {
    
    'use strict';
    
    let view = data;
    
    view.View = class View extends PIXI.Graphics {
        constructor () {
            super();
            
            let find_data = (proto) => {
                let name = `D${proto.constructor.name.substring(1)}`;
                if (data[name]) this.data = new data[name]();
                else {
                    if (proto.__proto__) find_data(proto.__proto__);
                    else this.data = new data.Data();
                }
            };
            find_data(this.__proto__);
            
            this.data.setter('x',       (v) => this.position.x = v);
            this.data.setter('y',       (v) => this.position.y = v);
//            this.data.setter('width',   (v) => this.width  = v);
//            this.data.setter('height',  (v) => this.height = v);
            this.data.setter('scale0',   (v) => {
                this.scale.x = this.data.scale0 * this.data.scale;
                this.scale.y = this.data.scale0 * this.data.scale;
            });
            this.data.setter('scale',   (v) => {
                this.scale.x = this.data.scale0 * this.data.scale;
                this.scale.y = this.data.scale0 * this.data.scale;
            });

            this.state  = 'default';
        }
        
        draw () {}

        auto_interactive (s) {
            this.interactive    = true;
            
            this.removeAllListeners('pointerover');
            this.removeAllListeners('pointerout');
            this.removeAllListeners('pointerdown');
            this.removeAllListeners('pointerup');
            this.removeAllListeners('pointerupoutside');
            
            this.on('pointerover', () => {
                if (s) this.data.tween('scale', s);
                this.state = 'over';
            });
            this.on('pointerout', () => {
                if (s) this.data.tween('scale', 1);
                this.state = 'default';
            });
            this.on('pointerdown', () => {
                if (s) this.data.tween('scale', 1);
                this.state = 'down';
            });
            this.on('pointerup', () => {
                if (s) this.data.tween('scale', s);
                this.state = 'over';
            });
            this.on('pointerupoutside', () => {
                if (s) this.data.tween('scale', 1);
                this.state = 'default'
            });
        }
    };
    
    view.VLabel = class VLabel extends view.View {
        constructor (text) {
            super();
            
            this.text = text || '';
            this.view = new PIXI.Text(this.text, new PIXI.TextStyle({fontWeight : '100'}));
            this.addChild(this.view);
            
            data.Data.bind(this.view, this, 'text', ( ) => this.update());
        }
        
        update () {
            this.view.pivot.x = this.view.width  / 2;
            this.view.pivot.y = this.view.height / 2 - 1;
        }
    }
    
    view.VPane = class VPane extends view.View {
        constructor () {super();}
        
        draw () {
            this.beginFill(this.data.color_bg, this.data.alpha);
            this.lineStyle(this.data.border, this.data.color_bd, this.data.alpha);
            this.drawRoundedRect(- this.data.width / 2, - this.data.height / 2, this.data.width, this.data.height, this.data.round);
            this.endFill();
        }
    };
    
    
    view.VPaneResource = class VPaneResource extends view.VPane {
        constructor () {
            super();
            
            let icon_c = new view.VButton('炭').style_icon_small(); // 14
            icon_c.data.x = - this.data.width / 5 * 1.5;
            icon_c.data.y = 0;
            
            let icon_ti = new view.VButton('钛').style_icon_small();
            icon_ti.data.x = - this.data.width / 5 * 0.5;
            icon_ti.data.y = 0;
            
            let icon_pu = new view.VButton('钚').style_icon_small(); // 238
            icon_pu.data.x = this.data.width / 5 * 0.5;
            icon_pu.data.y = 0;
            
            let icon_he = new view.VButton('氦').style_icon_small(); // 3
            icon_he.data.x = this.data.width / 5 * 1.5;
            icon_he.data.y = 0;
            
            this.addChild(icon_c);
            this.addChild(icon_ti);
            this.addChild(icon_pu);
            this.addChild(icon_he);
        }
    };
    
    
    view.VButton = class VButton extends view.VPane {
        constructor (text) {
            super();
            
            this.text   = text;
            this.label  = new view.VLabel();
            this.addChild(this.label);
            
            data.Data.bind(this.label, this, 'text');
            this.data.setter('width', (v) => {
//                this.width  = v;
                this.label.update();
            });
            this.data.setter('height', (v) => {
//                this.height = v;
                this.label.view.style.fontSize    = v * 2 / 3;
                this.label.update();
            });
        }
        
        style_icon_small () {
            this.buttonMode = false;
            this.data.style_icon_small();
            this.auto_interactive();
            return this;
        }
        style_icon_middle () {
            this.buttonMode = false;
            this.data.style_icon_middle();
            this.auto_interactive();
            return this;
        }
        style_icon_large () {
            this.buttonMode = false;
            this.data.style_icon_large();
            this.auto_interactive();
            return this;
        }
        style_primary () {
            this.buttonMode = true;
            this.data.style_primary();
            this.auto_interactive(1.05);
            return this;
        }

        click (action) {
            this.on('pointerup', action);
            return this;
        }
        
        draw () {
            super.draw();
            
            let color_mask = undefined;
            switch (this.state) {
                case 'over':
                    color_mask = 0xffffff;
                    break;
                case 'down':
                    color_mask = 0x000000;
                    break;
            }
            if (undefined != color_mask) {
                this.beginFill(color_mask, 0.2);
                this.lineStyle(0);
                this.drawRoundedRect(- this.data.width / 2, - this.data.height / 2, this.data.width, this.data.height, this.data.round);
                this.endFill();
            }
        }
    };
    
    view.VStar = class VStar extends view.View {
        constructor (type) {
            super();
            this.auto_interactive(1.2);
            this.data.type = type;
            
            this.data.setter('thumb', (v) => {
                if (v)  this.data.tween('scale0', 0.12);
                else    this.data.tween('scale0', 1);
            })
            
            this.click(( ) => this.data.thumb = !this.data.thumb);
        }

        click (action) {
            this.on('pointerup', action);
            return this;
        }
        
        draw () {
            switch (this.state) {
                case 'over':
                case 'down':
                    this.lineStyle(this.data.border, this.data.color_bd, 1);
                    break;
                default:
                    this.lineStyle(0);
                    break;
            }
            this.beginFill(this.data.color_bg, 1);
            this.drawCircle(0, 0, this.data.radius);
            this.endFill();
            
            switch (this.data.type) {
                case 'home':
                    break;
            }
        }
    };
    
    
    return view;
});