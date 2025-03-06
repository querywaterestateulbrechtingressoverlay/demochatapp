var currentChatroom;
const chatrooms = new Map();
const cachedMessages = new Map();

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
  const headers = new Headers({
    Authorization: 'Basic ' + btoa($("#login-username") + ':' + $("#login-password"))
  }
  try {
    const token = (await (await fetch("localhost:8080/token", {
      method: 'POST',
      headers: headers
    })).json()).token;

  } catch (error) {
    console.error(error);
  }
  const token =
  username = $("#login").val();
  connect();
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