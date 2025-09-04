package com.qweuio.chat.persistence.entity;

import org.springframework.data.annotation.Id;

import java.util.UUID;

public record ChatroomUser(
  @Id
  ChatroomAndUser chatroomAndUser,
  UserRole role
) {
  public static ChatroomUser fromData(UUID chatroomId, UUID userId, UserRole role) {
    return new ChatroomUser(new ChatroomAndUser(chatroomId, userId), role);
  }
}
