package com.qweuio.chat.websocket.dto.outbound;

import com.qweuio.chat.websocket.dto.ChatroomShortInfoDTO;

import java.util.List;

public record ChatroomListUpdateDTO(
  String recipientId,
  List<ChatroomShortInfoDTO> chatrooms,
  Operation operation
) {
  public enum Operation {
    ADD, REMOVE
  }
}
