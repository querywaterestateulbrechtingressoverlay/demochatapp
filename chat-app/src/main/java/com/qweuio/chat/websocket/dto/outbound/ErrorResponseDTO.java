package com.qweuio.chat.websocket.dto.outbound;

import java.time.Instant;
import java.util.UUID;

public record ErrorResponseDTO(
  UUID recipientId,
  Instant timestamp,
  String errorMessage
) {
}
