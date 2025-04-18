package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.websocket.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface MessageSenderService {
  String messageDest = "/messages";
  String messageHistoryDest = "/chatrooms/messages";
  String chatroomListUpdateDest = "/chatrooms";
  String chatroomUserListUpdDest = "/userlist";
  String errorDest = "/errors";

  void sendMessage(ChatMessage message, String chatroomId);

  void updateMessageHistory(String userId, String chatroomId, List<ChatMessage> history);

  void addChatroomToUser(String userId, ChatroomListDTO list);

  void removeChatroomFromUser(String userId, String chatroomId);

  void addUserToChatroom(String chatroomId, UserShortInfoDTO user);

  void addUsersToChatroom(String chatroomId, List<UserShortInfoDTO> users);

  void removeUserFromChatroom(String chatroomId, String userId);
}
