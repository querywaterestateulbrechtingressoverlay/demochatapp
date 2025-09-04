package com.qweuio.chat.core.exception.chatapp;

import com.qweuio.chat.core.exception.ChatAppException;

import java.util.UUID;

public class UserNotFoundException extends ChatAppException {
  public UserNotFoundException(UUID queriedUserId) {
    super("User " + queriedUserId + " not found");
  }
}
