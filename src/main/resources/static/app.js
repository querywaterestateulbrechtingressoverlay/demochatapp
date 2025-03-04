var username;
var chat;

const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/websocket'
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
//    stompClient.subscribe('/out/chat', (message) => {
//        showMessage(JSON.parse(message.body).message);
//    });
    stompClient.subscribe('/user/' + username + '/message', (message) => {
      showMessage(JSON.parse(message.body).message);
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function sendMessage() {
    stompClient.publish({
        destination: "/chat/send/" + chat,
        body: JSON.stringify({'message': $("#message").val()})
    });
}

function showMessage(message) {
    $("#message-list").append("<tr><td>" + message + "</td></tr>");
}

function login() {
  username = $("#login").val();
}

function chat() {
  chat = $("#chat").val();
}

$(function () {
    $( "#submit-button-login").click(() => login());
    $( "#submit-button-chat").click(() => chat());
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#send" ).click(() => sendMessage());
});