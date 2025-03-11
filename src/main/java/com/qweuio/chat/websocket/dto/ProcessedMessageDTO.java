package com.qweuio.chat.websocket.dto;

public record ProcessedMessageDTO(
  String senderId,
  String chatroomId,
  String message
) {
}
