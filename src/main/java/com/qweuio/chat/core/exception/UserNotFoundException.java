package com.qweuio.chat.core.exception;

public class UserNotFoundException extends RuntimeException {
  String userId;
  public UserNotFoundException(String userId) {
    this.userId = userId;
  }
  public String getUserId() {
    return userId;
  }
}
