package com.qweuio.chat.websocket.exception;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(String userId) {
    super("User with id " + userId + " not found");
  }
}
