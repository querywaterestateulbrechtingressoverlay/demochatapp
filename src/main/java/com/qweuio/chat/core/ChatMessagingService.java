package com.qweuio.chat.core;

public interface ChatMessagingService<T, U> {
  void addUserToChatroom(T userId, U chatroomId);
  void removeUserFromChatroom(T userId, U chatroomId);
  void createChatroom(T creatorId, String chatroomName);
  void deleteChatroom(T deletingUserId, U chatroomId);
  void addUserToChatroom(T invitingUserId, U chatroomId, T inviteeId);
  void removeUserFromChatroom(T removingUserId, U chatroomId, T removeeId);
}
