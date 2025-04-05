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

@Service
public class MessageSenderService {
  private final String messageDest = "/messages";
  private final String messageHistoryDest = "/messages/history";
  private final String chatroomListUpdateDest = "/chatrooms";
  private final String chatroomUserListUpdDest = "/chatrooms/users";
  private final String errorDest = "/errors";

  Logger logger = LoggerFactory.getLogger(MessageSenderService.class);
  @Autowired
  SimpMessagingTemplate template;

  public void sendMessage(ChatMessage message, List<String> recipients) {
    logger.trace("sending message to chatroom {}", message.chatroomId());
    for (String recipientId : recipients) {
      template.convertAndSendToUser(recipientId, messageDest, message);
    }
  }

  public void updateMessageHistory(String userId, String chatroomId, List<ChatMessage> history) {
    template.convertAndSendToUser(userId, messageHistoryDest, new ChatHistoryResponseDTO(chatroomId, history.stream().map(Converters::toDTO).toList()));
  }

  public void addChatroomToUser(String userId, ChatroomListDTO list) {
    template.convertAndSendToUser(userId, chatroomListUpdateDest, list, Map.of("operation", "add"));
  }

  public void removeChatroomFromUser(String userId, String chatroomId) {
    template.convertAndSendToUser(userId, chatroomListUpdateDest, chatroomId, Map.of("operation", "remove"));
  }

  public void addUserToChatroom(String chatroomId, List<String> recipients, UserShortInfoDTO user) {
    for (String recipientId : recipients) {
      template.convertAndSendToUser(
        recipientId,
        chatroomUserListUpdDest,
        user,
        Map.of("operation", "add")
      );
    }
  }

  public void removeUserFromChatroom(String chatroomId, String userId) {
    template.convertAndSendToUser(
      userId,
      "/chatroom",
      new ChatUserListDTO(chatroomId, List.of(new UserShortInfoDTO(userId, null))),
      Map.of("operation", "remove")
    );
  }
}
