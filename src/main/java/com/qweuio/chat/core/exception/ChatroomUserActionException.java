package com.qweuio.chat.core.exception;

public class ChatroomUserActionException extends ChatroomException {
  protected String causingUser;
  public ChatroomUserActionException(String chatroomId, String causingUser, String message) {
    super(chatroomId, message + causingUser + ", user = " + causingUser);
    this.causingUser = causingUser;
  }

  public String getCausingUser() {
    return causingUser;
  }
}