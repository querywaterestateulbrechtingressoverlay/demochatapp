package com.qweuio.chat.websocket.dto;

public record UnprocessedMessageDTO(
  String message,
  String chatroomId
) {
}
