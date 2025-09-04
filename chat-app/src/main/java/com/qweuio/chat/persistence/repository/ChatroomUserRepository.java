package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.ChatroomAndUser;
import com.qweuio.chat.persistence.entity.ChatroomUsers;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ChatroomUserRepository extends CrudRepository<ChatroomUsers, ChatroomAndUser>, InsertRepository<ChatroomUsers> {
  @Query("""
      SELECT * FROM chatroom_users
      WHERE user_id = :userId""")
  List<ChatroomUsers> findByUserId(UUID userId);

  @Query("""
      SELECT * FROM chatroom_users
      WHERE chatroom_id = :chatroomId""")
  List<ChatroomUsers> findByChatroomId(UUID chatroomId);

  @Query("""
      SELECT COUNT(*) FROM chatroom_users
      WHERE user_id = :userId""")
  Integer userChatroomCount(UUID userId);

  @Query("""
      SELECT COUNT(*) FROM chatroom_users
      WHERE chatroom_id = :chatroomId""")
  Integer chatroomUserCount(UUID chatroomId);
}
