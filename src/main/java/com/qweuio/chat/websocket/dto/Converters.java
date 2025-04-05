package com.qweuio.chat.websocket.dto;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;

import java.util.List;

public class Converters {
  public static UserShortInfoDTO toDTO(ChatUser chatUser) {
    return new UserShortInfoDTO(chatUser.id(), chatUser.name());
  }

  public static ChatroomShortInfoDTO toDTO(Chatroom chatroom) {
    return new ChatroomShortInfoDTO(chatroom.id(), chatroom.name());
  }

  public static MessageDTO toDTO(ChatMessage message) {
    return new MessageDTO(message.id(), message.senderId(), message.chatroomId(), message.sentAt(), message.contents());
  }

  public static ChatroomListDTO toDTO(List<Chatroom> chatrooms) {
    return new ChatroomListDTO(chatrooms.stream().map(Converters::toDTO).toList());
  }
}
