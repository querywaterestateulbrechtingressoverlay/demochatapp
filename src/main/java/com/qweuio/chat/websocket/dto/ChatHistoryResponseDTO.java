package com.qweuio.chat.websocket.dto;

import com.qweuio.chat.persistence.entity.ChatMessage;

import java.util.List;

public record ChatHistoryResponseDTO(
  String chatId,
  List<ProcessedMessageDTO> messageList
) {
}
