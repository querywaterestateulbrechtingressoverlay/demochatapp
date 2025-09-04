package com.qweuio.chat.core.exception.chatapp;

import com.qweuio.chat.core.exception.ChatAppException;

import java.util.UUID;

public class ChatroomNotFoundException extends ChatAppException {
  public ChatroomNotFoundException(UUID chatroomId) {
    super("Chatroom " + chatroomId + " not found");
  }
}
