
(function($) {

fomjar.framework.phase.append('dom', init_animate);
fomjar.framework.phase.append('dom', load_data);

function init_animate() {
    bg_animate();
}

function load_data() {
    setTimeout(function() {
    }, 3000);
}

function bg_animate() {
    $('.sn .bg img').bind('load', function() {
        $('.sn .bg img').css('width',  '110%');
        $('.sn .bg img').css('height', '110%');
    });
    setTimeout(function() {
        $('.sn .bg img').css('filter', 'blur(3em)');
        $('.sn .bg img').css('-webkit-filter', 'blur(3em)');
    }, 7000)
}

})(jQuery)

