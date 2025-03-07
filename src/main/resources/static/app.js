var currentChatroom;
const chatrooms = new Map();
const cachedMessages = new Map();
var jwttoken;
const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/websocket'
});
var userId;

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/user/' + userId + '/messages', (messageDTO) => {
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
    showChatrooms();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function sendMessage() {
    stompClient.publish({
        destination: "/chat/send",
        body: JSON.stringify({'message': $("#message").val(), 'chatroomId': currentChatroom})
    });
}

async function showChatrooms() {
  const availableChatrooms = await (await fetch("http://localhost:8080/mychatrooms", {
    headers: {
      Authorization: "Bearer " + jwttoken
    }
  }
  )).json();
  for (const chatroom of availableChatrooms) {
    chatrooms.set(chatroom.id, chatroom.name);
    $("#chatroom-list").append("<tr><td data-roomId=\"" + chatroom.id + "\" id=\"chatroom-" + chatroom.id + "\" class=\"chatroom\">" + chatroom.name + "</td></tr>");
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
  $("#message-list").empty();
  if (cachedMessages.has(newChatroomId)) {
    for (const message of cachedMessages[newChatroomId]) {
      $("#message-list").append("<tr><td>" + message + "</td></tr>");
    }
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

    stompClient.connectHeaders['Authorization'] = jwttoken;
    stompClient.connectHeaders['X-CSRF-TOKEN'] = csrf;
    connect();
    console.log(csrf);
  } catch (error) {
    console.error(error);
  }
}

function chat() {
  chat = $("#chat").val();
}

$(function () {
    $(document).on("click", ".chatroom", function() {
      currentChatroom = $(this).data("roomid");
      switchChatrooms($(this).data("roomid"));
    });
    $( "#login-submit").click(async () => await login());
    $( "#chat-submit").click(() => chat());
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#send" ).click(() => sendMessage());
});