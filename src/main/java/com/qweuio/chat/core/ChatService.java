package com.qweuio.chat.core;

import com.qweuio.chat.persistence.entity.ChatUser;

import java.util.List;

public interface ChatService<T, U> {
  ChatUser getUserInfo(T userId);
  U createChatroom(String chatroomName);
  void deleteChatroom(U chatroomId);
  void addUserToChatroom(U chatroomId, T userId);
  void removeUserFromChatroom(U chatroomId, T userId);
  List<ChatUser> getChatroomUsers(U chatroomId);
}
