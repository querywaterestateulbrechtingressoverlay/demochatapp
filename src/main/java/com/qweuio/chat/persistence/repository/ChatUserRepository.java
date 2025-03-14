package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.ChatUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ChatUserRepository extends MongoRepository<ChatUser, String> {
  Optional<ChatUser> findByName(String name);
}
