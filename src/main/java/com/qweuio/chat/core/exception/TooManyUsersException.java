package com.qweuio.chat.core.exception;

public class TooManyUsersException extends RuntimeException {
  private final String chatroomId;
  public TooManyUsersException(String chatroomId) {
    this.chatroomId = chatroomId;
  }

  public String getChatroomId() {
    return chatroomId;
  }
}
