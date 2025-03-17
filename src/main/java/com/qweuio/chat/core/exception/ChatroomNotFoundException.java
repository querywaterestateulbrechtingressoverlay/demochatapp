package com.qweuio.chat.core.exception;

public class ChatroomNotFoundException extends RuntimeException {
  private final String chatroomId;
  public ChatroomNotFoundException(String chatroomId) {
    this.chatroomId = chatroomId;
  }

  public String getChatroomId() {
    return chatroomId;
  }
}
