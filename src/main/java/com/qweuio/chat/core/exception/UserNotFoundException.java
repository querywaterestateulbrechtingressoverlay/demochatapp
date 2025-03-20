package com.qweuio.chat.core.exception;

public class UserNotFoundException extends UserActionException {
  public UserNotFoundException(String causingUser, String queriedUserId) {
    super(causingUser, "Queried user " + queriedUserId + " couldn't be found");
  }
}
