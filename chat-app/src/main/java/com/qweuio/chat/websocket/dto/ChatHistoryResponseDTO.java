package com.qweuio.chat.websocket.dto;

import com.qweuio.chat.websocket.dto.outbound.MessageDTO;

import java.util.List;

public record ChatHistoryResponseDTO(
  String chatId,
  List<MessageDTO> messageList
) {
}
