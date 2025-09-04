package com.qweuio.chat.websocket.dto.outbound;

import com.qweuio.chat.websocket.dto.UserShortInfoDTO;

import java.util.List;
import java.util.UUID;

public record UserListUpdateDTO(
  List<UUID> updateReceivingUserId,
  UUID chatroomId,
  List<UserShortInfoDTO> updatedUserIdList,
  Operation operation
) {
  public enum Operation {
    ADD, REMOVE
  }
}
