package com.qweuio.chat.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("chat_messages")
public record ChatMessage(
  @Id
  UUID id,
  UUID senderId,
  UUID chatroomId,
  Instant sentAt,
  String contents
) {
}