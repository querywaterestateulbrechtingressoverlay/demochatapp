const websocketUrl = "ws:///websocket";
const backendUrl = "/ws";
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
    receiveNewMessage(JSON.parse(messageDTO.body));
  });
  stompClient.subscribe("/user/" + userId + "/userlist", (userList) => {
    receiveUserListUpdate(JSON.parse(userList.body));
  });
  stompClient.subscribe("/user/" + userId + "/chatrooms", (chatroomList) => {
    receiveChatroomListUpdate(JSON.parse(chatroomList.body).chatrooms);
  });
  stompClient.subscribe("/user/" + userId + "/chatrooms/messages", (chatroomHistory) => {
    receieveChatHistoryUpdate(JSON.parse(chatroomHistory.body).chatId, JSON.parse(chatroomHistory.body).messageList);
  });
  requestChatroomListUpdate();
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
  } else {
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

function requestChatUserListUpdate() {
  stompClient.publish({
    destination: "/chat/" + currentChatroom + "/getUsers"
  });
}

function requestChatroomListUpdate() {
  stompClient.publish({
    destination: "/chat/getAvailableChatrooms"
  });
}

function showUserList(chatroomId) {
  $("#user-list").empty();
  const userl = $("#user-list");
  for (const user of cachedUsers.get(chatroomId)) {
    $("#user-list").append("<tr><td>" + user.name + " <button class='kick-user' data-userid='" + user.id + "'>Kick</button></td></tr>");
  }
}

function receiveUserListUpdate(userList) {
  cachedUsers.set(userList.chatId, userList.users);
  if (currentChatroom == userList.chatId) {
    showUserList(userList.chatId);
  }
}

function receiveChatroomListUpdate(chatroomList) {
  $("#chatroom-list").empty();
  chatrooms.clear();
  for (const chatroom of chatroomList) {
    chatrooms.set(chatroom.id, chatroom.name);
    $("#chatroom-list")
      .append("<tr><td data-roomId=\"" + chatroom.id + "\" id=\"chatroom-"
      + chatroom.id + "\" class=\"chatroom\">" + chatroom.name + "</td></tr>");
  }
  if (currentChatroom == null || !chatrooms.has(currentChatroom)) {
    if (!chatrooms.has(currentChatroom)) {
      cachedMessages.delete(currentChatroom);
      cachedUsers.delete(currentChatroom);
    }
    currentChatroom = chatrooms.keys().next().value;
    switchChatrooms(currentChatroom);
  }
}

function receiveNewMessage(message) {
  if (!cachedMessages.has(message.chatroomId)) {
    cachedMessages.set(message.chatroomId, []);
  }
  cachedMessages.get(message.chatroomId).push({
    message: message.message,
    senderId: message.senderId
  });
  if (message.chatroomId == currentChatroom) {
    $("#message-list").append("<tr><td>" + message.senderId + " at " + message.sentAt + ":" + message.message + "</td></tr>");
  }
}

function showCachedMessages(newChatroomId) {
  $("#message-list").empty();
  for (const message of cachedMessages.get(newChatroomId)) {
    $("#message-list").append("<tr><td>" + message.senderId + ":" + message.message + "</td></tr>");
  }
}

function requestChatHistoryUpdate(chatroomId) {
  stompClient.publish({
    destination: "/chat/" + chatroomId + "/getRecentHistory",
    body: JSON.stringify({'beforeMessageId': null})
  });
}

function receieveChatHistoryUpdate(chatroomId, messageList) {
  if (!cachedMessages.has(chatroomId)) {
    cachedMessages.set(chatroomId, []);
  }
  cachedMessages.get(chatroomId).push(...messageList);
  if (chatroomId == currentChatroom) {
    messageList.forEach((message) => {
      $("#message-list").append("<tr><td>" + message.senderId + " at " + message.sentAt + ":" + message.message + "</td></tr>");
    });
  }
}

function switchChatrooms(chatroomId) {
  if (!cachedMessages.has(chatroomId)) {
    requestChatHistoryUpdate(chatroomId);
  }
  if (!cachedUsers.has(chatroomId)) {
    requestChatUserListUpdate();
  }
  showCachedMessages();
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

    (await (await fetch("/ipaddress"))).text().then((ip) => {
      stompClient.brokerURL = "ws/websocket";
    });


    $("#login-form").empty();
    $("#login-form").append("<p>Logged in as " + $("#login").val());
    stompClient.connectHeaders['Authorization'] = jwtResponse.token;

    connect();
  } catch (error) {
    console.error(error);
  }
}

function inviteUser() {
  const userIdToInvite = $("#invite-user-id").val();
  stompClient.publish({
    destination: "/chat/" + currentChatroom + "/invite",
    body: JSON.stringify({'userId': userIdToInvite})
  });
}

function kickUser(userId) {
  stompClient.publish({
    destination: "/chat/" + currentChatroom + "/kick",
    body: JSON.stringify({'userId': userId})
  });
}

$(function () {
  $(document).on("click", ".chatroom", function() {
    currentChatroom = $(this).data("roomid");
    switchChatrooms($(this).data("roomid"));
  });
  $("#login-submit").click(async () => await login());
  $("#invite-submit").click(() => inviteUser());
  $(document).on("click", ".kick-user", function() {
    const userId = $(this).data("userid");
    kickUser(userId);
  });
  $("form").on('submit', (e) => e.preventDefault());
  $("#connect").click(() => connect());
  $("#disconnect").click(() => disconnect());
  $("#send").click(() => sendMessage());
});
