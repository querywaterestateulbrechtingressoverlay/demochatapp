package com.qweuio.chat.websocket.dto;

import java.util.UUID;

public record UserShortInfoDTO(
  UUID id,
  String name
) {
}
