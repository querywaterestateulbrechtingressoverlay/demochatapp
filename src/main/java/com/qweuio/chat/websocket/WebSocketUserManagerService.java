package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Set;

@Service
public class WebSocketUserManagerService {
  private final String CHATROOM_PREFIX = "chatroomusers-_";
  private final String CHATROOM_LIST_KEY = "subscribedchatrooms";
  private final String USER_LIST_KEY = "connectedusers";

  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Autowired
  private ChatUserRepository chatUserRepo;

  Logger logger = LoggerFactory.getLogger(WebSocketUserManagerService.class);

  @EventListener(SessionConnectEvent.class)
  void onClientConnect(SessionConnectEvent connectEvent) {
    String userId = connectEvent.getUser().getName();
    var connectedUserChatrooms = chatUserRepo
      .getChatroomsByUser(userId)
      .stream()
      .map(Chatroom::id)
      .toArray(String[]::new);
    if (connectedUserChatrooms.length != 0) {
      redisTemplate.opsForSet().add(CHATROOM_LIST_KEY, connectedUserChatrooms);
    }

    for (String chatroomId : connectedUserChatrooms) {
      redisTemplate.opsForSet().add(CHATROOM_PREFIX + chatroomId, userId);
    }
    logger.info(" !!!!!! USER " + userId + " HAS CONNECTED !!!!!! ");
    redisTemplate.opsForSet().add(USER_LIST_KEY, userId);
  }

  @EventListener(SessionDisconnectEvent.class)
  void onClientDisconnect(SessionDisconnectEvent disconnectEvent) {
    String userId = disconnectEvent.getUser().getName();
    chatUserRepo
      .getChatroomsByUser(userId)
      .stream()
      .map(Chatroom::id)
      .forEach((chatroomId) -> {
        redisTemplate.opsForSet().remove(CHATROOM_PREFIX + chatroomId, userId);
        if (redisTemplate.opsForSet().size(CHATROOM_PREFIX + chatroomId) == 0) {
          redisTemplate.opsForSet().remove(CHATROOM_LIST_KEY, chatroomId);
        }
      });
    redisTemplate.opsForSet().remove(USER_LIST_KEY, userId);
  }

  public void addUserToChatroom(String userId, String chatroomId) {
    redisTemplate.opsForSet().add(CHATROOM_LIST_KEY, chatroomId);
    redisTemplate.opsForSet().add(CHATROOM_PREFIX + chatroomId, userId);
  }

  public void removeUserFromChatroom(String userId, String chatroomId) {
    redisTemplate.opsForSet().remove(CHATROOM_PREFIX + chatroomId, userId);
    if (redisTemplate.opsForSet().size(CHATROOM_PREFIX + chatroomId) == 0) {
      redisTemplate.opsForSet().remove(CHATROOM_LIST_KEY, chatroomId);
    }
  }

  public List<String> getChatroomConnectedClients(String chatroomId) {
    return List.copyOf(redisTemplate.opsForSet().members(CHATROOM_PREFIX + chatroomId));
  }

  public boolean isUserConnected(String userId) {
    return redisTemplate.opsForSet().isMember(USER_LIST_KEY, userId);
  }

  public boolean isChatroomPresent(String chatroomId) {
    return redisTemplate.opsForSet().isMember(CHATROOM_LIST_KEY, chatroomId);
  }
}
