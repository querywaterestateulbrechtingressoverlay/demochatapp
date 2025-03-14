package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.entity.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ChatroomRepository extends MongoRepository<Chatroom, String> {
  @Query(value = "{ id = ?0 }", fields = "{ users: { $elemMatch: { userId: ?1 } } }")
  Optional<UserRole> getUserRoleFromChatroomById(String chatroomId, String userId);
}
