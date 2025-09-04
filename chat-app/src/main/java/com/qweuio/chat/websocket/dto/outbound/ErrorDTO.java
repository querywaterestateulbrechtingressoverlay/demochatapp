package com.qweuio.chat.websocket.dto.outbound;

import java.time.Instant;
import java.util.UUID;

public record ErrorDTO(
  UUID recipientId,
  Instant timestamp,
  String errorMessage
) {
}
