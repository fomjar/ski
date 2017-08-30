
function stars(canvas) {
    window.requestAnimationFrame = window.requestAnimationFrame || window.mozRequestAnimationFrame || window.webkitRequestAnimationFrame || window.msRequestAnimationFrame;
    var c = canvas.getContext('2d');
    var count = 1900;
    var stars = [];

    function init() {
        for (var i = 0; i < count; i++){
          var star = {
            x: Math.random() * canvas.width,
            y: Math.random() * canvas.height,
            z: Math.random() * canvas.width,
            o: '0.' + Math.floor(Math.random() * 99) + 1
          };
          stars.push(star);
        }
    }

    function frame() {
        move();
        draw();
        window.requestAnimationFrame(frame);
    }
    
    function move() {
        for(var i = 0; i < count; i++){
            var star = stars[i];
            star.z--;
            
            if(star.z <= 0){
                star.z = canvas.width;
            }
        }
    }
    
    function draw() {
        var radius = '0.' + Math.floor(Math.random() * 9) + 1;
        var focalLength = canvas.width / 2;
        var centerX = canvas.width / 2;
        var centerY = canvas.height / 2;
        var pixelX, pixelY, pixelRadius;
      
        c.fillStyle = "rgba(0,10,20,1)";
        c.fillRect(0,0, canvas.width, canvas.height);
        c.fillStyle = "rgba(209, 255, 255, " + radius + ")";

        for(var i = 0; i < count; i++){
            var star = stars[i];
            
            pixelX = (star.x - centerX) * (focalLength / star.z);
            pixelX += centerX;
            pixelY = (star.y - centerY) * (focalLength / star.z);
            pixelY += centerY;
            pixelRadius = 1 * (focalLength / star.z);
            
            c.fillRect(pixelX, pixelY, pixelRadius, pixelRadius);
            c.fillStyle = "rgba(209, 255, 255, " + star.o + ")";
        }
    }

    init();
    frame();
}
