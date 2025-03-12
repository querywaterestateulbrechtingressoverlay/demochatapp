const websocketUrl = "ws://localhost:8080/websocket";
const backendUrl = "http://localhost:8080";
const jwtEndpoint = "/token";
var currentChatroom;
const chatrooms = new Map();
const cachedUsers = new Map();
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
    stompClient.subscribe("/user/" + userId + "/userlist", (userList) => {
      updateUserList(JSON.parse(userList.body));
    });
    showChatrooms();
    switchChatrooms(chatrooms.keys().next().value());
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
    destination: "/chat/" + currentChatroom + "/send",
    body: JSON.stringify({'message': $("#message").val()})
  });
}

function requestUserListUpdate() {
  stompClient.publish({
    destination: "/chat/" + currentChatroom + "/getUsers"
  });
}

function showUserList(chatroomId) {
  $("#user-list").empty();
  const userl = $("#user-list");
  const asd = cachedUsers.get(chatroomId);
  for (const user of cachedUsers.get(chatroomId)) {
    $("#user-list").append("<tr><td>" + user.name + "</td></tr>");
  }
  
}

function updateUserList(userList) {
  cachedUsers.set(userList.chatId, userList.users);
  if (currentChatroom == userList.chatId) {
    showUserList(userList.chatId);
  }
}

async function showChatrooms() {
  const availableChatrooms = await (await fetch("http://localhost:8080/mychatrooms", {
    headers: fetchHeaders
  }
  )).json();
  for (const chatroom of availableChatrooms) {
    chatrooms.set(chatroom.id, chatroom.name);
    $("#chatroom-list").append("<tr><td data-roomId=\"" + chatroom.id + "\" id=\"chatroom-" + chatroom.id + "\" class=\"chatroom\">" + chatroom.name + "</td></tr>");
  }
}

function showMessage(message) {
  if (cachedMessages.has(message.chatroomId)) {
    cachedMessages.get(message.chatroomId).push(
      {
        message: message.message,
        senderId: message.senderId
      });
  } else {
    cachedMessages.set(message.chatroomId, [
      {
        message: message.message,
        senderId: message.senderId
      }]);
  }
  if (message.chatroomId == currentChatroom) {
    $("#message-list").append("<tr><td>" + message.senderId + ":" + message.message + "</td></tr>");
  }  
}

function switchChatrooms(newChatroomId) {
  $("#message-list").empty();
  if (cachedMessages.has(newChatroomId)) {
    for (const message of cachedMessages.get(newChatroomId)) {
      $("#message-list").append("<tr><td>" + message.senderId + ": " + message.message + "</td></tr>");
    }
  }
  if (!cachedUsers.has(newChatroomId)) {
    requestUserListUpdate();
  }
}

async function login() {
  try {
    fetchHeaders.set("Authorization", "Basic " + btoa($("#login-username").val() + ":" + $("#login-password").val()));
    const jwtResponse = (await (await fetch(backendUrl + jwtEndpoint, {
      method: "POST",
      headers: fetchHeaders
    })).json());
    userId = jwtResponse.id;
    fetchHeaders.set("Authorization", "Bearer " + jwtResponse.token);

    $("#login-form").empty();
    $("#login-form").append("<p>Logged in as " + $("#login").val());
    stompClient.connectHeaders['Authorization'] = jwtResponse.token;

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
