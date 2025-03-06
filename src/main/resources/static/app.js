var currentChatroom;
const chatrooms = new Map();
const cachedMessages = new Map();
var jwttoken;
const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/websocket'
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/user/' + username + '/messages', (messageDTO) => {
      showMessage(JSON.parse(messageDTO.body));
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

function connect(csrf) {
    stompClient.connectionHeaders = {
      "X-CSRF-TOKEN": csrf,
      JWT: jwttoken
    }
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

async function showChatrooms() {
  const availableChatrooms = await (await fetch("http://localhost:8080/mychatrooms", {
    headers: {
      Authorization: "Bearer " + token
    }
  }
  )).json();
  for (const chatroom of availableChatrooms) {
    chatrooms.set(chatroom.id, chatroom.name);
    $("#chatroom-list").append("<tr><td>" + chatroom.name + "</td></tr>");
  }
}

function showMessage(message) {
  if (cachedMessages.has(message.chatroomId)) {
    cachedMessages.get(message.chatroomId).push({message: message.message, sender: message.senderId});
  } else {
    cachedMessages.set(message.chatroomId, [{message: message.message, sender: message.senderId}]);

  }
  if (message.chatroomId == currentChatroom) {
    $("#message-list").append("<tr><td>" + message + "</td></tr>");
  }  
}

function switchChatrooms(newChatroomId) {
  $("$message-list").empty();
  for (const message of cachedMessages.get(newChatroomId)) {
    $("#message-list").append("<tr><td>" + message + "</td></tr>");
  }
}

async function login() {
  try {
    jwttoken = (await (await fetch("http://localhost:8080/token", {
      method: "POST",
      headers: {
        Authorization: 'Basic ' + btoa($("#login-username").val() + ':' + $("#login-password").val())
      }
    })).json()).token;
    $("#login-form").empty();
    $("#login-form").append("<p>Logged in as " + $("#login").val());
    stompClient.connectHeaders = {
      Authorization: "Bearer " + jwttoken
    };
    const csrf = (await (await fetch("http://localhost:8080/csrf", {
                       headers: {
                         Authorization: 'Bearer ' + jwttoken
                       }
                     })).json()).token;
    connect(csrf);
    console.log(csrf);
  } catch (error) {
    console.error(error);
  }
}

function chat() {
  chat = $("#chat").val();
}

$(function () {
    $( "#login-submit").click(async () => await login());
    $( "#chat-submit").click(() => chat());
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#send" ).click(() => sendMessage());
});