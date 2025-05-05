package com.qweuio.chat.persistence;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.List;

@EnableScheduling
@Service
public class MessagePersistingService {
  private final int RECENT_MESSAGE_COUNT = 10;
  private final Logger logger = LoggerFactory.getLogger(MessagePersistingService.class);
  @Autowired
  ChatMessageRepository messageRepository;
  @Autowired
  RedisChatMessageCache redisChatMessageCache;

  public ChatMessage persistMessage(MessageDTO message) {
    ChatMessage messageEntity = persistMessageInDB(message);
    redisChatMessageCache.cacheMessage(messageEntity);
    return messageEntity;
  }

  public List<ChatMessage> getRecentMessages(String chatroomId) {
    if (redisChatMessageCache.isCachePresent(chatroomId)) {
      return redisChatMessageCache.getMessagesFromCache(chatroomId, RECENT_MESSAGE_COUNT);
    } else {
      List<ChatMessage> persistedMessages = getMessagesFromDB(chatroomId);
      persistedMessages.forEach(msg -> redisChatMessageCache.cacheMessage(msg));
      return persistedMessages;
    }
  }

  public ChatMessage persistMessageInDB(MessageDTO message) {
    return messageRepository.save(new ChatMessage(null, message.sender(), message.chatroom(), message.timestamp(), message.content()));
  }

  public List<ChatMessage> getMessagesFromDB(String chatroomId) {
    return messageRepository.findTopNByChatroomId(chatroomId, RECENT_MESSAGE_COUNT)
      .stream()
      .toList();
  }
}
