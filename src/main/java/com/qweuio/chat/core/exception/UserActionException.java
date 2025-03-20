package com.qweuio.chat.core.exception;

public class UserActionException extends RuntimeException {
  protected String causingUser;
  protected String causingAction;
  public UserActionException(String causingUser, String causingAction) {
    super("Exception caused by user " + causingUser + ", causing action = " + causingAction);
    this.causingUser = causingUser;
    this.causingAction = causingAction;
  }

  public String getCausingUser() {
    return causingUser;
  }

  public String getCausingAction() {
    return causingAction;
  }
}