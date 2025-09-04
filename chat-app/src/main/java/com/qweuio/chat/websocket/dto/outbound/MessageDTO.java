package com.qweuio.chat.websocket.dto.outbound;

import java.time.Instant;
import java.util.UUID;

public record MessageDTO(
  UUID id,
  UUID sender,
  UUID chatroom,
  Instant timestamp,
  String content
) {
}
