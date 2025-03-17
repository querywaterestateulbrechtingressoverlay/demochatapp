package com.qweuio.chat.core.exception;

public class InsufficientPermissionsException extends RuntimeException {
  private final String userId;
  public InsufficientPermissionsException(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
