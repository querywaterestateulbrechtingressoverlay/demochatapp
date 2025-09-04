package com.qweuio.chat.websocket.dto;

import java.util.List;
import java.util.UUID;

public record ChatUserListDTO(
  UUID chatId,
  List<UserShortInfoDTO> users
) {
}
