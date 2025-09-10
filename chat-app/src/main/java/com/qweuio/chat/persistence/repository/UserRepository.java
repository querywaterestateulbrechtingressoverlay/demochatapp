package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {
  Optional<User> findByUsername(String username);
  @Query("""
    SELECT users.* FROM chatroom_users
    JOIN users ON chatroom_users.user_id = users.id
    WHERE chatroom_users.chatroom_id = :chatroomId""")
  List<User> findUsersByChatroomId(UUID chatroomId);
}
