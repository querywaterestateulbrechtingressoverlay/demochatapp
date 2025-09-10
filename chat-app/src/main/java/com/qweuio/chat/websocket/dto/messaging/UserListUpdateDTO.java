package com.qweuio.chat.websocket.dto.messaging;

import java.util.List;
import java.util.UUID;

public record UserListUpdateDTO(
  List<UUID> updateReceivingUserId,
  UUID chatroomId,
  List<UserShortInfoDTO> userListUpdate,
  Operation operation
) {
  public enum Operation {
    ADD, REMOVE
  }
}
