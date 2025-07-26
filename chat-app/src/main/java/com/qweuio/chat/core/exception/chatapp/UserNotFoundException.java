package com.qweuio.chat.core.exception.chatapp;

import com.qweuio.chat.core.exception.ChatAppException;

public class UserNotFoundException extends ChatAppException {
  public UserNotFoundException(String queriedUserId) {
    super("User " + queriedUserId + " not found");
  }
}
