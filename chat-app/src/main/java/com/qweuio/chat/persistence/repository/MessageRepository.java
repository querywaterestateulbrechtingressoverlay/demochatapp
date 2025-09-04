package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.ChatMessage;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends CrudRepository<ChatMessage, UUID> {
  List<ChatMessage> findTopNByChatroomId(UUID chatroomId, int n);
}
