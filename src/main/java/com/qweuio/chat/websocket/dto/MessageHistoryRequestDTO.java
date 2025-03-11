package com.qweuio.chat.websocket.dto;

public record MessageHistoryRequestDTO(
  String chatroomId,
  String beforeMessageId
) {
}
