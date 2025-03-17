package com.qweuio.chat.core.exception;

public class ChatroomAccessException extends RuntimeException {
  private final String userId;
  private final String chatroomId;
  public ChatroomAccessException(String userId, String chatroomId) {
    this.userId = userId;
    this.chatroomId = chatroomId;
  }

  public String getUserId() {
    return userId;
  }

  public String getChatroomId() {
    return chatroomId;
  }
}
