package com.qweuio.chat.core.exception.chatapp;

import com.qweuio.chat.core.exception.ChatAppException;

import java.util.UUID;

public class TooManyUsersException extends ChatAppException {
  public TooManyUsersException(UUID userId, UUID chatroomId) {
    super("Couldn't add user " + userId + " to chatroom " + chatroomId + " because it has reached the maximum amount of users");
  }
}
