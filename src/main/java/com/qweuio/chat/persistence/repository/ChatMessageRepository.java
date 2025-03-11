package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, Integer> {
  List<ChatMessage> findTop10ByChatroomId(String chatroomId);
  List<ChatMessage> findTop10ByChatroomIdAndIdLessThan(String chatroomId, String id);
}
