
requirejs.config({

    baseUrl : '/',

    map     : {
        '*' : {
            css : 'js/require.css'
        }
    },

    paths   : {
        jquery          : 'js/jquery',
        jquery_cookie   : 'js/jquery.cookie',
        jquery_json     : 'js/jquery.json',
        jquery_skippr   : 'js/jquery.skippr',
        fomjar          : 'js/fomjar',
        ski             : 'js/ski'
    },
    shim    : {
        'weui'  : ['css!/css/weui']
    }

});

var bizdir = '/wechat';
var module = window.location.pathname.replace(/.*\//g, '').replace(/\..*/g, '');

define('main', [bizdir+'/'+module+'.js'], function(){});

