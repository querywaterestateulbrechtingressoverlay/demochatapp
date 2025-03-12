package com.qweuio.chat.websocket;

import com.qweuio.chat.messaging.KafkaService;
import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
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
    return destChatroom.isPresent() && destChatroom.get().users().stream().anyMatch((ur) -> ur.userId() == userId);
  }

  UserShortInfoDTO toShortDTO(ChatUser user) {
    return new UserShortInfoDTO(user.id(), user.name());
  }

  @MessageMapping("/send")
  public void sendMessage(@Payload UnprocessedMessageDTO message, Principal principal) {
    if (!checkUserMembership(principal.getName(), message.chatroomId())) {
      template.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      kafkaService.sendMessage(new ProcessedMessageDTO(principal.getName(), message.chatroomId(), message.message()));
    }
  }
  @MessageMapping("/chat/{chatId}/getRecentHistory")
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
  @MessageMapping("/chat/{chatId}/getUsers")
  public void getUsers(@DestinationVariable String chatId,
                       Principal principal) {
    if (!checkUserMembership(principal.getName(), chatId)) {
      template.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      template.convertAndSendToUser(principal.getName(), "/update/chatUsers", chatroomRepo
        .findById(chatId).orElseThrow(() -> new RuntimeException("wtf"))
        .users().stream()
        .map((ur) -> userRepo.findById(ur.userId()).orElseThrow(() -> new RuntimeException("wtf")))
        .map(this::toShortDTO).toList());
    }
  }
}
