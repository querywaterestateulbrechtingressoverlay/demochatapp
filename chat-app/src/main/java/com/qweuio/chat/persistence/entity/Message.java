package com.qweuio.chat.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("messages")
public record Message(
  @Id
  UUID id,
  UUID senderId,
  UUID chatroomId,
  Instant sentAt,
  String contents
) {
}