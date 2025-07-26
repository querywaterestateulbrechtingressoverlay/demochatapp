package com.qweuio.chat.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("chatrooms")
public record Chatroom(
    @Id
    String id,
    String name,
    List<UserWithRoleEntity> users,
    List<String> messageIds
) {
}
