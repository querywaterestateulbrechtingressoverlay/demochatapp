package com.qweuio.chat.websocket.dto.messaging;

import java.util.List;
import java.util.UUID;

public record ChatroomListUpdateDTO(
  UUID recipientId,
  List<ChatroomShortInfoDTO> chatrooms,
  Operation operation
) {
  public enum Operation {
    ADD, REMOVE
  }
}
