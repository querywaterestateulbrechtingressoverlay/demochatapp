package com.qweuio.chat.dto;

public record UnprocessedMessageDTO(
  String message,
  Integer chatroomId
) {
}
