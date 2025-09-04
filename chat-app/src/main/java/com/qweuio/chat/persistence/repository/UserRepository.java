package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.User;
import com.qweuio.chat.persistence.entity.Chatroom;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatUserRepository extends CrudRepository<User, UUID> {
  Optional<User> findByName(String name);
  @Query("""
    SELECT chat_users.* FROM chatroom_users
    JOIN chat_users ON chatroom_users.chatroom_id = chatrooms.id
    WHERE chatroom_users.user_id = :chatroomId""")
  List<Chatroom> findUsersByChatroomId(UUID chatroomId);
}
