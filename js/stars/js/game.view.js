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
            this.data.setter('width',   (v) => this.width  = v);
            this.data.setter('height',  (v) => this.height = v);
            this.data.setter('scale',   (v) => {this.scale.x = v; this.scale.y = v;});

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
            
            let icon_pu = new view.VButton('钚').style_icon_small(); // 238
            icon_pu.data.x = - this.data.width / 5 * 0.5;
            icon_pu.data.y = 0;
            
            let icon_he = new view.VButton('氦').style_icon_small(); // 3
            icon_he.data.x = this.data.width / 5 * 0.5;
            icon_he.data.y = 0;
            
            this.addChild(icon_c);
            this.addChild(icon_pu);
            this.addChild(icon_he);
        }
    };
    
    
    view.VButton = class VButton extends view.VPane {
        constructor (text) {
            super();
            
            this.text   = new PIXI.Text(text, new PIXI.TextStyle({fontWeight : '100'}));
            this.addChild(this.text);
            
            this.data.setter('width', (v) => {
//                this.width  = v;
                this.text.pivot.x = this.text.width / 2;
                this.text.pivot.y = this.text.height / 2 - 1;
            });
            this.data.setter('height', (v) => {
//                this.height = v;
                this.text.style.fontSize    = v * 2 / 3;
                this.text.pivot.x = this.text.width / 2;
                this.text.pivot.y = this.text.height / 2 - 1;
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
                this.lineStyle(this.data.border, color_mask, 0.2);
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