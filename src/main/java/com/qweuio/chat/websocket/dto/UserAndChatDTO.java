package com.qweuio.chat.websocket.dto;

public record UserAndChatDTO(
  String userId,
  String chatroomId
) {
}
