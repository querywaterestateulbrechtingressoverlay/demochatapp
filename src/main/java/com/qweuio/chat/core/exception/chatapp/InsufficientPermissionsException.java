package com.qweuio.chat.core.exception.chatapp;

import com.qweuio.chat.core.exception.ChatAppException;

public class InsufficientPermissionsException extends ChatAppException {
  public InsufficientPermissionsException(String operation) {
    super("Not enough permissions for the operation: " + operation);
  }
}
