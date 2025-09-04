package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.persistence.repository.UserRepository;
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
  Set<UUID> connectedUsers = new HashSet<>();
  Map<UUID, Set<UUID>> chatrooms = new HashMap<>();

  @Autowired
  private UserRepository userRepo;

  @Autowired
  private ChatroomRepository chatroomRepo;

  Logger logger = LoggerFactory.getLogger(WebSocketUserManagerService.class);

  public void addUserToChatroom(UUID chatroomId, UUID userId) {
    chatrooms.compute(chatroomId, (k, v) -> {
      if (v == null) {
        return new HashSet<>(Set.of(userId));
      } else {
        v.add(userId);
        return v;
      }
    });
  }

  public void removeUserFromChatroom(UUID chatroomId, UUID userId) {
    var chatroom = chatrooms.get(chatroomId);
    chatroom.remove(userId);
    if (chatroom.isEmpty()) {
      chatrooms.remove(chatroomId);
    }
  }

  @EventListener(SessionConnectEvent.class)
  void onClientConnect(SessionConnectEvent connectEvent) {
    UUID userId = UUID.fromString(connectEvent.getUser().getName());
    logger.debug("user {} has connected", userId);
    connectedUsers.add(userId);
    chatroomRepo
      .findChatroomsByUserId(userId)
      .forEach((cr) -> addUserToChatroom(cr.id(), userId));
  }

  @EventListener(SessionDisconnectEvent.class)
  void onClientDisconnect(SessionDisconnectEvent disconnectEvent) {
    UUID userId = UUID.fromString(disconnectEvent.getUser().getName());
    logger.debug("user {} has disconnected", userId);
    connectedUsers.remove(userId);
    chatroomRepo
      .findChatroomsByUserId(userId)
      .forEach((cr) -> removeUserFromChatroom(cr.id(), userId));
  }

  public Set<UUID> getChatroomConnectedClients(UUID chatroomId) {
    return chatrooms.getOrDefault(chatroomId, Collections.emptySet());
  }

  public boolean isUserConnected(UUID userId) {
    System.out.println(connectedUsers);
    return connectedUsers.contains(userId);
  }

  public boolean isChatroomPresent(UUID chatroomId) {
    return chatrooms.containsKey(chatroomId);
  }
}
