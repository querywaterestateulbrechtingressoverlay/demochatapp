package com.qweuio.chat.core.exception.chatapp;

import com.qweuio.chat.core.exception.ChatAppException;

public class TooManyUsersException extends ChatAppException {
  public TooManyUsersException(String userId, String chatroomId) {
    super("Couldn't add user " + userId + " to chatroom " + chatroomId + " because it has reached the maximum amount of users");
  }
}
