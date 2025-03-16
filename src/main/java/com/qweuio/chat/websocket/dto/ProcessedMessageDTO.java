package com.qweuio.chat.websocket.dto;

import java.time.Instant;

public record ProcessedMessageDTO(
  String senderId,
  String chatroomId,
  Instant sentAt,
  String message
) {
}
