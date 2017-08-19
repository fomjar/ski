(function () {
    'use strict';
    
    var http    = require('http'),
        fs      = require('fs');

    function serve(request, response) {
        console.info(request.url);

        response.writeHead(200, {'Content-Type' : 'text/plain'});
        fs.readFile('.' + request.url, function (error, data) {
            if (error) {
                console.error(error.message);
                response.write(error.message);
            } else if (data) {
                response.write(data);
            }
            response.end();
        });
    }

    http.createServer(serve).listen(8888);

}());
