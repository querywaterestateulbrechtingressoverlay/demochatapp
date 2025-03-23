package com.qweuio.chat.core.exception;

public class InsufficientPermissionsException extends ChatroomUserActionException {
  public InsufficientPermissionsException(String userId, String chatroomId, String action) {
    super(chatroomId, userId, "Insufficient permissions for action " + action);
  }
}
