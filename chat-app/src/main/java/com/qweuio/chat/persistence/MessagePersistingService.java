package com.qweuio.chat.persistence;

import com.qweuio.chat.persistence.entity.Message;
import com.qweuio.chat.persistence.repository.MessageRepository;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@EnableScheduling
@Service
public class MessagePersistingService {
  private final int RECENT_MESSAGE_COUNT = 10;
  private final Logger logger = LoggerFactory.getLogger(MessagePersistingService.class);
  @Autowired
  MessageRepository messageRepository;
  @Autowired
  RedisChatMessageCache redisChatMessageCache;

  public Message persistMessage(MessageDTO message) {
    Message messageEntity = persistMessageInDB(message);
    logger.info(messageEntity.toString());
    redisChatMessageCache.cacheMessage(messageEntity);
    return messageEntity;
  }

  public List<Message> getRecentMessages(UUID chatroomId) {
    if (redisChatMessageCache.isCachePresent(chatroomId)) {
      return redisChatMessageCache.getMessagesFromCache(chatroomId, RECENT_MESSAGE_COUNT);
    } else {
      List<Message> persistedMessages = getMessagesFromDB(chatroomId);
      persistedMessages.forEach(msg -> redisChatMessageCache.cacheMessage(msg));
      return persistedMessages;
    }
  }

  public Message persistMessageInDB(MessageDTO message) {
    return messageRepository.save(new Message(null, message.sender(), message.chatroom(), message.timestamp(), message.content()));
  }

  public List<Message> getMessagesFromDB(UUID chatroomId) {
    return messageRepository.findTopNByChatroomId(chatroomId, RECENT_MESSAGE_COUNT)
      .stream()
      .toList();
  }
}
