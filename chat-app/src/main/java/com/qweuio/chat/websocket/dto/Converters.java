package com.qweuio.chat.websocket.dto;

import com.qweuio.chat.persistence.entity.Message;
import com.qweuio.chat.persistence.entity.User;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.websocket.dto.messaging.ChatroomShortInfoDTO;
import com.qweuio.chat.websocket.dto.messaging.UserShortInfoDTO;
import com.qweuio.chat.websocket.dto.outbound.ChatroomListDTO;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;

import java.util.List;

public class Converters {
  public static UserShortInfoDTO toDTO(User user) {
    return new UserShortInfoDTO(user.id(), user.username());
  }

  public static ChatroomShortInfoDTO toDTO(Chatroom chatroom) {
    return new ChatroomShortInfoDTO(chatroom.id(), chatroom.name());
  }

  public static MessageDTO toDTO(Message message) {
    return new MessageDTO(message.id(), message.senderId(), message.chatroomId(), message.sentAt(), message.contents());
  }

  public static ChatroomListDTO toDTO(List<Chatroom> chatrooms) {
    return new ChatroomListDTO(chatrooms.stream().map(Converters::toDTO).toList());
  }
}
