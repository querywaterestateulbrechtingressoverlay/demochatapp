package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.Chatroom;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ChatroomRepository extends CrudRepository<Chatroom, UUID> {
  @Query("""
    SELECT chatrooms.* FROM chatroom_users
    JOIN chatrooms ON chatroom_users.chatroom_id = chatrooms.id
    WHERE chatroom_users.user_id = :userId""")
  List<Chatroom> findChatroomsByUserId(UUID userId);
}
