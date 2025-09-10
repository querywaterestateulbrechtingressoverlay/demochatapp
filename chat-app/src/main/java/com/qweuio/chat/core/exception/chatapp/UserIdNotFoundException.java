package com.qweuio.chat.core.exception.chatapp;

import com.qweuio.chat.core.exception.ChatAppException;

import java.util.UUID;

public class UserIdNotFoundException extends ChatAppException {
  public UserIdNotFoundException(UUID queriedUserId) {
    super("User " + queriedUserId + " not found");
  }
}
