package com.qweuio.chat.websocket.dto.inbound;

public record MessageHistoryRequestDTO(
  String beforeMessageId
) {
}
