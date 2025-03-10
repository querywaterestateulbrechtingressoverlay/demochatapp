package com.qweuio.chat.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("messages")
public record ChatMessage(
  @Id
  Integer id,
  Integer senderId,
  Integer chatroomId,
  Instant sentAt,
  String contents
) {
}
