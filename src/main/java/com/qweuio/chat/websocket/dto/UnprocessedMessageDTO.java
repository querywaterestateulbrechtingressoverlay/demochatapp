package com.qweuio.chat.websocket.dto;

public record UnprocessedMessageDTO(
  String message,
  Integer chatroomId
) {
}
