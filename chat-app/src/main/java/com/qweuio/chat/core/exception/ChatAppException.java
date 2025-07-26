package com.qweuio.chat.core.exception;

/**
 * Parent class of the custom exceptions **/
public class ChatAppException extends RuntimeException {
  public ChatAppException(String message) {
    super(message);
  }
}
