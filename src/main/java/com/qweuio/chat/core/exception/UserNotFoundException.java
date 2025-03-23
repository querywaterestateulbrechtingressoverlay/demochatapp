package com.qweuio.chat.core.exception;

public class UserNotFoundException extends ChatroomException {
  public UserNotFoundException(String chatroomId, String queriedUserId) {
    super(chatroomId, "User " + queriedUserId + " was not found");
  }
}
