package com.qweuio.chat.core.exception;

public class UserNotFoundException extends ChatAppException {
  public UserNotFoundException(String queriedUserId) {
    super("User " + queriedUserId + " was not found");
  }
}
