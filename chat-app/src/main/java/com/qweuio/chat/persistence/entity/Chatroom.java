package com.qweuio.chat.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("chatrooms")
public record Chatroom(
    @Id
    UUID id,
    String name
) {
}
