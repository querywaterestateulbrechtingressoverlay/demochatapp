package com.qweuio.chat.websocket.dto.outbound;

import com.qweuio.chat.websocket.dto.messaging.ChatroomShortInfoDTO;

import java.util.List;

public record ChatroomListDTO(
  List<ChatroomShortInfoDTO> chatrooms
) {
}
