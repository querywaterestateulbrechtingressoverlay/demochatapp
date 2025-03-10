package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<ChatMessage, Integer> {
}
