package com.qweuio.chat.websocket.dto;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;

public class Converters {
  public static UserShortInfoDTO toDTO(ChatUser chatUser) {
    return new UserShortInfoDTO(chatUser.id(), chatUser.name());
  }
  public static ChatroomShortInfoDTO toDTO(Chatroom chatroom) {
    return new ChatroomShortInfoDTO(chatroom.id(), chatroom.name());
  }

  public static ProcessedMessageDTO toDTO(ChatMessage message) {
    return new ProcessedMessageDTO(message.senderId(), message.chatroomId(), message.sentAt(), message.contents());
  }
}
