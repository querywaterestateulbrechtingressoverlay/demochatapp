package com.qweuio.chat.core.exception.chatapp;

import com.qweuio.chat.core.exception.ChatAppException;

import java.util.UUID;

public class UserNameNotFoundException extends ChatAppException {
  public UserNameNotFoundException(String queriedUserId) {
    super("User " + queriedUserId + " not found");
  }
}
