package com.qweuio.chat.websocket.dto;

public record ProcessedMessageDTO(
  Integer senderId,
  Integer recipientId,
  Integer chatroomId,
  String message
) {
}
