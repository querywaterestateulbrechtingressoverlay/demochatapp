package com.qweuio.chat.websocket.dto.messaging;

import java.util.UUID;

public record ChatroomShortInfoDTO(
  UUID id,
  String name
) {
}
