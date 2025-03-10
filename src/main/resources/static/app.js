const websocketUrl = "ws://localhost:8080/websocket";
const backendUrl = "http://localhost:8080";
const csrfEndpoint = "/csrf";
const jwtEndpoint = "/token";
var currentChatroom;
const chatrooms = new Map();
const cachedMessages = new Map();
var jwttoken;
const stompClient = new StompJs.Client({
    brokerURL: websocketUrl
});
var userId;
const fetchHeaders = new Headers();

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

async function getCsrfToken() {
  try {
    const csrf = (await (await fetch(backendUrl + csrfEndpoint)).json());
    fetchHeaders.set(csrf.headerName, csrf.token);
  } catch (error) {
    console.error(error);
  }
}

async function login() {
  try {
    getCsrfToken();

    fetchHeaders.set("Authorization", "Basic " + btoa($("#login-username").val() + ":" + $("#login-password").val()));
    const jwt = (await (await fetch(backendUrl + jwtEndpoint, {
      method: "POST",
      headers: fetchHeaders
    })).json()).token;
    fetchHeaders.set("Authorization", "Bearer " + jwt.token);

    $("#login-form").empty();
    $("#login-form").append("<p>Logged in as " + $("#login").val());

    connect();
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