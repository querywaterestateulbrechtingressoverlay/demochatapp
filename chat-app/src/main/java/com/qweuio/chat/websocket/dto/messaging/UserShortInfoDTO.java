package com.qweuio.chat.websocket.dto.messaging;

import java.util.UUID;

public record UserShortInfoDTO(
  UUID id,
  String name
) {
}
