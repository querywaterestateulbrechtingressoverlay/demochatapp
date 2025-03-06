package com.qweuio.chat.dto;

public record ProcessedMessageDTO(
  Integer senderId,
  Integer chatroomId,
  String message
) {
}
