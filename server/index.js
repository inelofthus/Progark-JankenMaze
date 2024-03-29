var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = [];

server.listen(8080, function(){
    console.log("Server is now running...")
});

io.on('connection', function(socket){
    console.log('player connected');
    socket.emit('socketID', { id: socket.id }); //Gives new client a unique id
    socket.broadcast.emit('newPlayer', { id: socket.id });
    socket.emit('getPlayers', players);
    
    socket.on('playerMoved', function(data){
        data.id = socket.id;
        socket.broadcast.emit('playerMoved'. data);

        console.log("playerMoved: " +
            'ID: ' + data.id +
            'X: ' + data.x +
            'Y: ' + data.y 
        );

        for (let i = 0; i < players.length; i++){
            if (players[i].id == data.id){
                players[i].x = data.x;
                players[i].y = data.y;
            }
        }
    });
    
    socket.on('disconnect', function(){
        console.log("player disconnected");
        socket.broadcast.emit('playerDisconnected', { id: socket.id });
        for (let i = 0; i < players.length; i++) {
            if (players[i] == socket.id){
                players.splice(i, 1); //Remove player from array
            }    
        }
    });

    players.push(new player(socket.id, 0, 0));
});

function player(id, x, y){
    this.id = id;
    this.x = x;
    this.y = y;
}