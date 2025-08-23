package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;

@Service
public class WebSocketUserManagerService {
  Set<String> connectedUsers = new HashSet<>();
  Map<String, Set<String>> chatrooms = new HashMap<>();

  @Autowired
  private ChatUserRepository chatUserRepo;

  Logger logger = LoggerFactory.getLogger(WebSocketUserManagerService.class);

  public void addUserToChatroom(String chatroomId, String userId) {
    chatrooms.compute(chatroomId, (k, v) -> {
      if (v == null) {
        return new HashSet<>(Set.of(userId));
      } else {
        v.add(userId);
        return v;
      }
    });
  }

  public void removeUserFromChatroom(String chatroomId, String userId) {
    var chatroom = chatrooms.get(chatroomId);
    chatroom.remove(userId);
    if (chatroom.isEmpty()) {
      chatrooms.remove(chatroomId);
    }
  }

  @EventListener(SessionConnectEvent.class)
  void onClientConnect(SessionConnectEvent connectEvent) {
    String userId = connectEvent.getUser().getName();
    var connectedUserChatrooms = chatUserRepo
      .getChatroomsByUser(userId)
      .stream()
      .map(Chatroom::id)
      .toArray(String[]::new);
    logger.info("user " + userId + " has connected");
    connectedUsers.add(userId);
    for (String chatroomId : connectedUserChatrooms) {
      addUserToChatroom(chatroomId, userId);
    }
  }

  @EventListener(SessionDisconnectEvent.class)
  void onClientDisconnect(SessionDisconnectEvent disconnectEvent) {
    String userId = disconnectEvent.getUser().getName();
    chatUserRepo
      .getChatroomsByUser(userId)
      .stream()
      .map(Chatroom::id)
      .forEach((chatroomId) -> removeUserFromChatroom(chatroomId, userId));
    logger.info("user " + userId + " has disconnected");
    connectedUsers.remove(userId);
  }

  public Set<String> getChatroomConnectedClients(String chatroomId) {
    return chatrooms.getOrDefault(chatroomId, Collections.emptySet());
  }

  public boolean isUserConnected(String userId) {
    System.out.println(connectedUsers);
    return connectedUsers.contains(userId);
  }

  public boolean isChatroomPresent(String chatroomId) {
    return chatrooms.containsKey(chatroomId);
  }
}
