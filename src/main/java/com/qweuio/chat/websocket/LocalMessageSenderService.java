package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LocalMessageSenderService implements MessageSenderService {
  private final String messageDest = "/messages";
  private final String messageHistoryDest = "/chatrooms/messages";
  private final String chatroomListUpdateDest = "/chatrooms";
  private final String chatroomUserListUpdDest = "/userlist";

  @Autowired
  private ChatroomRepository chatroomRepo;

  Logger logger = LoggerFactory.getLogger(LocalMessageSenderService.class);
  @Autowired
  SimpMessagingTemplate template;

  public void sendMessage(ChatMessage message, String chatroomId) {
    logger.trace("sending message to chatroom {}", message.chatroomId());
    for (String recipientId : chatroomRepo.getUsersByChatroom(chatroomId).stream().map(ChatUser::id).toList()) {
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

  public void addUserToChatroom(String chatroomId, UserShortInfoDTO user) {
    for (String recipientId : chatroomRepo.getUsersByChatroom(chatroomId).stream().map(ChatUser::id).toList()) {
      template.convertAndSendToUser(
        recipientId,
        chatroomUserListUpdDest,
        new ChatUserListDTO(chatroomId, List.of(user)),
        Map.of("operation", "add")
      );
    }
  }

  public void addUsersToChatroom(String chatroomId, List<UserShortInfoDTO> users) {
    for (String recipientId : chatroomRepo.getUsersByChatroom(chatroomId).stream().map(ChatUser::id).toList()) {
      template.convertAndSendToUser(
        recipientId,
        chatroomUserListUpdDest,
        new ChatUserListDTO(chatroomId, users),
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
