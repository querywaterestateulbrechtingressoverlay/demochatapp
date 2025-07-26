package com.qweuio.chat.websocket.dto.outbound;

import com.qweuio.chat.websocket.dto.UserShortInfoDTO;

import java.util.List;

public record UserListUpdateDTO(
  List<String> updateReceivingUserId,
  String chatroomId,
  List<UserShortInfoDTO> updatedUserIdList,
  Operation operation
) {
  public enum Operation {
    ADD, REMOVE
  }
}
