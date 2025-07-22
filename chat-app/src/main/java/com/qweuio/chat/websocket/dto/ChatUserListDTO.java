package com.qweuio.chat.websocket.dto;

import java.util.List;

public record ChatUserListDTO(
  String chatId,
  List<UserShortInfoDTO> users
) {
}
