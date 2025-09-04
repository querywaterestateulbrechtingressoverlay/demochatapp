package com.qweuio.chat.websocket.dto;

import java.util.UUID;

public record ChatroomShortInfoDTO(
  UUID id,
  String name
) {
}
