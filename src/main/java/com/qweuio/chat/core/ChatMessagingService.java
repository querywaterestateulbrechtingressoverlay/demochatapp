package com.qweuio.chat.core;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.websocket.dto.MessageRequestDTO;

import java.util.List;

public interface ChatMessagingService<T, U> extends ChatService<T, U> {
  Chatroom createChatroom(T creatorId, String chatroomName);
  void deleteChatroom(T deletingUserId, U chatroomId);
  Chatroom addUserToChatroom(T invitingUserId, U chatroomId, T inviteeId);
  void removeUserFromChatroom(T removingUserId, U chatroomId, T removeeId);
  List<ChatUser> getChatroomUsers(T requestingUserId, U chatroomId);

  ChatMessage sendMessage(String senderId, String chatroomId, MessageRequestDTO message);
}
