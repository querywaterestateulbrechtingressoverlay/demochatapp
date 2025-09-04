package com.qweuio.chat.core.exception;

import java.util.UUID;

public class UserActionException extends RuntimeException {
  private final UUID causingUserId;
  public UserActionException(UUID causingUserId, String message) {
    super(message);
    this.causingUserId = causingUserId;
  }
  public UserActionException(UUID causingUserId, String message, Throwable cause) {
    super(message, cause);
    this.causingUserId = causingUserId;
  }

  public UUID getCausingUserId() {
    return causingUserId;
  }
}
