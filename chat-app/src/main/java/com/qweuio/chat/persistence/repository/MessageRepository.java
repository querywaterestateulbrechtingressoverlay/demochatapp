package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.Message;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends CrudRepository<Message, UUID> {
  List<Message> findTopNByChatroomId(UUID chatroomId, int n);
}
