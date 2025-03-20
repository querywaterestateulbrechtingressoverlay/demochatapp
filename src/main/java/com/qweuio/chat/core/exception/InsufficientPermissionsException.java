package com.qweuio.chat.core.exception;

public class InsufficientPermissionsException extends UserActionException {
  private String chatroomId;
  private String action;
  public InsufficientPermissionsException(String userId, String chatroomId, String action) {
    super(userId, "Insufficient permissions to perform action \"" + action + "\" in chatroom " + chatroomId);
    this.chatroomId = chatroomId;
    this.action = action;
  }

  public String getChatroomId() {
    return chatroomId;
  }

  public String getAction() {
    return action;
  }
}
