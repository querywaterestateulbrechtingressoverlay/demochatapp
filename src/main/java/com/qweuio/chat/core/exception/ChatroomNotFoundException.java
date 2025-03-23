package com.qweuio.chat.core.exception;

public class ChatroomNotFoundException extends ChatroomException {
  public ChatroomNotFoundException(String chatroomId) {
    super(chatroomId, "chatroom not found");
  }
}
