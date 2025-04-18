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
  private final String CHATROOM_PREFIX = "CHATROOM_";
  private final String CHATROOM_LIST_KEY = "SUBSCRIBED_CHATROOMS";

  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Autowired
  private ChatUserRepository chatUserRepo;

  @EventListener(SessionConnectEvent.class)
  void onClientConnect(SessionConnectEvent connectEvent) {
    var connectedUserChatrooms = chatUserRepo.getChatroomsByUser(connectEvent.getUser().getName()).stream().map(Chatroom::id).toList();
    var diff = redisTemplate.opsForSet().difference(CHATROOM_LIST_KEY, connectedUserChatrooms);
    if (!diff.isEmpty()) {
      redisTemplate.opsForSet().add(CHATROOM_LIST_KEY, diff.toArray(String[]::new));
    }
    for (String chatroomId : connectedUserChatrooms) {
      redisTemplate.opsForSet().add(CHATROOM_PREFIX + chatroomId, connectEvent.getUser().getName());
    }
  }

  @EventListener(SessionDisconnectEvent.class)
  void onClientDisconnect() {

  }
}
