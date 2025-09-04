package com.qweuio.chat.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("chatroom_users")
public record ChatroomUsers(
  @Id
  ChatroomAndUser chatroomAndUser,
  UserRole role
) {
  public static ChatroomUsers fromData(UUID chatroomId, UUID userId, UserRole role) {
    return new ChatroomUsers(new ChatroomAndUser(chatroomId, userId), role);
  }
}
