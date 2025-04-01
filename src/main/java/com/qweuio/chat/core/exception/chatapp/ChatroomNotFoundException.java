package com.qweuio.chat.core.exception.chatapp;

import com.qweuio.chat.core.exception.ChatAppException;

public class ChatroomNotFoundException extends ChatAppException {
  public ChatroomNotFoundException(String chatroomId) {
    super("Chatroom " + chatroomId + " not found");
  }
}
