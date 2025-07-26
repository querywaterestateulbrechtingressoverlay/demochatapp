package com.qweuio.chat.core.exception;

public class UserActionException extends RuntimeException {
  private String causingUserId;
  public UserActionException(String causingUserId, String message) {
    super(message);
    this.causingUserId = causingUserId;
  }
  public UserActionException(String causingUserId, String message, Throwable cause) {
    super(message, cause);
    this.causingUserId = causingUserId;
  }

  public String getCausingUserId() {
    return causingUserId;
  }
}
