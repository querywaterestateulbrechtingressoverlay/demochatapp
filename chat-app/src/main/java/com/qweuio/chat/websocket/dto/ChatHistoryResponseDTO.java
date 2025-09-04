package com.qweuio.chat.websocket.dto;

import com.qweuio.chat.websocket.dto.outbound.MessageDTO;

import java.util.List;
import java.util.UUID;

public record ChatHistoryResponseDTO(
  UUID chatId,
  List<MessageDTO> messageList
) {
}
