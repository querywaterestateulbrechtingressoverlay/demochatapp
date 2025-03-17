package com.qweuio.chat.core.exception;

public class TooManyChatroomsException extends RuntimeException {
  private final String userId;
  public TooManyChatroomsException(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
