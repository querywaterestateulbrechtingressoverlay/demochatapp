package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, Integer> {
  List<ChatMessage> findTopNByChatroomId(String chatroomId, int n);
}
