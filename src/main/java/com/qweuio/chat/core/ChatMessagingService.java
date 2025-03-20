package com.qweuio.chat.core;

import com.qweuio.chat.persistence.entity.ChatUser;

import java.util.List;

public interface ChatMessagingService<T, U> {
  U createChatroom(T creatorId, String chatroomName);
  void deleteChatroom(T deletingUserId, U chatroomId);
  void addUserToChatroom(T invitingUserId, U chatroomId, T inviteeId);
  void removeUserFromChatroom(T removingUserId, U chatroomId, T removeeId);
  List<ChatUser> getChatroomUsers(T requestingUserId, U chatroomId);
  List<ChatUser> getChatroomUsers(U chatroomId);
}
