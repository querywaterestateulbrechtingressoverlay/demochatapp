package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.config.KafkaConfiguration;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketConnectedClientManager {
  private final String CHATROOM_PREFIX = "CHATROOM_USERS_";
  private final String CHATROOM_LIST_KEY = "SUBSCRIBED_CHATROOMS";

  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Autowired
  private ChatUserRepository chatUserRepo;

  @EventListener(SessionConnectEvent.class)
  void onClientConnect(SessionConnectEvent connectEvent) {
    String userId = connectEvent.getUser().getName();
    var connectedUserChatrooms = chatUserRepo
      .getChatroomsByUser(userId)
      .stream()
      .map(Chatroom::id)
      .toArray(String[]::new);

    redisTemplate.opsForSet().add(CHATROOM_LIST_KEY, connectedUserChatrooms);

    for (String chatroomId : connectedUserChatrooms) {
      redisTemplate.opsForSet().add(CHATROOM_PREFIX + chatroomId, userId);
    }
  }

  @EventListener(SessionDisconnectEvent.class)
  void onClientDisconnect(SessionDisconnectEvent disconnectEvent) {
    String userId = disconnectEvent.getUser().getName();
    chatUserRepo
      .getChatroomsByUser(userId)
      .stream()
      .map(Chatroom::id)
      .forEach((chatroomId) -> redisTemplate.opsForSet().remove(CHATROOM_PREFIX + chatroomId, userId));
  }
}
