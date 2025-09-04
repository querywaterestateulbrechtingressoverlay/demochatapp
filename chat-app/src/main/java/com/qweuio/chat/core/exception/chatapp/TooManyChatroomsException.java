package com.qweuio.chat.core.exception.chatapp;

import com.qweuio.chat.core.exception.ChatAppException;

import java.util.UUID;

public class TooManyChatroomsException extends ChatAppException {
  public TooManyChatroomsException(UUID userId, UUID chatroomId) {
    super("Couldn't add user " + userId + " to chatroom " + (chatroomId == null ? "null" : chatroomId) + " because they have reached the maximum amount of chatrooms");
  }
}
