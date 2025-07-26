package com.qweuio.chat.websocket.dto;

import java.util.List;

public record ChatroomListDTO(
  List<ChatroomShortInfoDTO> chatrooms
) {
}
