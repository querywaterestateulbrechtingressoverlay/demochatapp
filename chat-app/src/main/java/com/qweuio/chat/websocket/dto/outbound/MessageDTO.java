package com.qweuio.chat.websocket.dto.outbound;

import java.time.Instant;

public record MessageDTO(
  String id,
  String sender,
  String chatroom,
  Instant timestamp,
  String content
) {
}
