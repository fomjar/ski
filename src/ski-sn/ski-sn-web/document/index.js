
(function($) {

fomjar.framework.phase.append('dom', init_animate);
fomjar.framework.phase.append('ren', load_data);

function init_animate() {
    bg_animate();
}

function load_data() {
}

function bg_animate() {
    // revert
    $('.sn .bg >img').css('width', '100%');
    $('.sn .bg >img').css('height', '100%');
    $('.sn .bg >img').css(        'filter', 'none');
    $('.sn .bg >img').css('-webkit-filter', 'none');
    
    $('.sn .bg >div').css('opacity', '0');
    
    $('.sn .head').css('top', '-3em');
    $('.sn .foot').css('bottom', '-3em');
    
    // animate
    $('.sn .bg >img').bind('load', function() {
        $('.sn .bg >img').css('width',  '105%');
        $('.sn .bg >img').css('height', '105%');
        
        setTimeout(function() {
            $('.sn .bg >img').css(        'filter', 'blur(.3em)');
            $('.sn .bg >img').css('-webkit-filter', 'blur(.3em)');
            
            $('.sn .bg >div').css('opacity', '.3');
            
            $('.sn .head').css('top', '0');
            $('.sn .foot').css('bottom', '0');
        }, 3000)
    });
}

})(jQuery)

