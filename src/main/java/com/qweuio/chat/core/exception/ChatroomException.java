package com.qweuio.chat.core.exception;

public class ChatroomException extends ChatAppException {
  protected String chatroomId;

  public ChatroomException(String chatroomId, String message) {
    super(message + ", chatroom id " + chatroomId);
    this.chatroomId = chatroomId;
  }

  public ChatroomException(String chatroomId, String message, Throwable cause) {
    super(message + ", chatroom id " + chatroomId, cause);
    this.chatroomId = chatroomId;
  }

  public String getChatroomId() {
    return chatroomId;
  }
}
