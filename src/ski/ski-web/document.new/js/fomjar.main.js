
requirejs.config({

    baseUrl : '/',

    map     : {
        '*' : {
            css : 'js/require.css'
        }
    },

    paths   : {
        'jquery'        : 'js/jquery',
        'jquery.cookie' : 'js/jquery.cookie',
        'jquery.json'   : 'js/jquery.json',
        'jquery-ui'     : 'js/jquery-ui',
        'jquery.skippr' : 'js/jquery.skippr',
        'fomjar'        : 'js/fomjar',
        'ski'           : 'js/ski'
    },
    shim    : {
        'weui'      : ['css!/css/weui'],
        'weui.ext'  : ['css!/css/weui.ext']
    }

});

var bizdir = '/wechat';
var module = window.location.pathname.replace(/.*\//g, '').replace(/\..*/g, '');

define('main', [bizdir+'/'+module+'.js']);

