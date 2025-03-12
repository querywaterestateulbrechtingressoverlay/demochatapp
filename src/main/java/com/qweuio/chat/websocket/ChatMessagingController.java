package com.qweuio.chat.websocket;

import com.qweuio.chat.messaging.KafkaService;
import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.UserRole;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import com.qweuio.chat.websocket.dto.*;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
public class ChatMessagingController {
  Logger logger = LoggerFactory.getLogger(ChatMessagingController.class);
  @Autowired
  ChatroomRepository chatroomRepo;
  @Autowired
  ChatUserRepository userRepo;
  @Autowired
  ChatMessageRepository messageRepo;
  @Autowired
  KafkaService kafkaService;
  @Autowired
  SimpMessagingTemplate template;

  boolean checkUserMembership(String userId, String chatroomId) {
    Optional<Chatroom> destChatroom = chatroomRepo.findById(chatroomId);
    if (destChatroom.isPresent()) {
      Optional<UserRole> user = destChatroom.get().users().stream().filter((ur) -> Objects.equals(ur.userId(), userId)).findFirst();
      if (user.isPresent()) {
        return true;
      }
    }
    return false;
  }

  UserShortInfoDTO toShortDTO(ChatUser user) {
    return new UserShortInfoDTO(user.id(), user.name());
  }

  @MessageMapping("/{chatId}/send")
  public void sendMessage(@Payload UnprocessedMessageDTO message,
                          @DestinationVariable String chatId,
                          Principal principal) {
    if (!checkUserMembership(principal.getName(), chatId)) {
      template.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      kafkaService.sendMessage(new ProcessedMessageDTO(principal.getName(), chatId, message.message()));
    }
  }
  @MessageMapping("/{chatId}/getRecentHistory")
  public void getRecentHistory(@Payload MessageHistoryRequestDTO messageRequest,
                               @DestinationVariable String chatId,
                               Principal principal) {
    if (!checkUserMembership(principal.getName(), chatId)) {
      template.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      List<ChatMessage> foundMessages = messageRequest.beforeMessageId() == null ?
        messageRepo.findTop10ByChatroomId(chatId) :
        messageRepo.findTop10ByChatroomIdAndIdLessThan(chatId, messageRequest.beforeMessageId());
    }
  }
  @MessageMapping("/{chatId}/getUsers")
  public void getUsers(@DestinationVariable String chatId,
                       Principal principal) {
    if (!checkUserMembership(principal.getName(), chatId)) {
      template.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      kafkaService.updateUserList(new ChatUserListDTO(chatId,
        chatroomRepo
        .findById(chatId).orElseThrow(() -> new RuntimeException("wtf"))
        .users().stream()
        .map((ur) -> userRepo.findById(ur.userId()).orElseThrow(() -> new RuntimeException("wtf")))
        .map(this::toShortDTO).toList()));
    }
  }
}
