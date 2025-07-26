package com.qweuio.chat.websocket.dto.outbound;

import java.time.Instant;

public record ErrorDTO(
  String recipientId,
  Instant timestamp,
  String errorMessage
) {
}
