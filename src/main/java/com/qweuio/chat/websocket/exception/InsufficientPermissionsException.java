package com.qweuio.chat.websocket.exception;

public class InsufficientPermissionsException extends RuntimeException {
  InsufficientPermissionsException(String message) {
    super(message);
  }
}
