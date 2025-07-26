package com.qweuio.chat.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("messages")
public record ChatMessage(
  @Id
  String id,
  String senderId,
  String chatroomId,
  Instant sentAt,
  String contents
) {
}