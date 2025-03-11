package com.qweuio.chat.websocket;

import com.qweuio.chat.messaging.KafkaService;
import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.websocket.dto.MessageHistoryRequestDTO;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import com.qweuio.chat.websocket.dto.UnprocessedMessageDTO;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class MessageController {
  Logger logger = LoggerFactory.getLogger(MessageController.class);
  @Autowired
  ChatroomRepository chatroomRepo;
  @Autowired
  ChatMessageRepository messageRepo;
  @Autowired
  KafkaService kafkaService;
  @Autowired
  SimpMessagingTemplate template;

  boolean checkUserMembership(String userId, String chatroomId) {
    int intId = Integer.parseInt(userId);
    Optional<Chatroom> destChatroom = chatroomRepo.findById(chatroomId);
    if (destChatroom.isEmpty() || !destChatroom.get().userIds().contains(intId)) {
      return false;
    }
    return true;
  }

  @MessageMapping("/send")
  public void sendMessage(@Payload UnprocessedMessageDTO message, Principal principal) {
    if (!checkUserMembership(principal.getName(), message.chatroomId())) {
      template.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      kafkaService.sendMessage(new ProcessedMessageDTO(principal.getName(), message.chatroomId(), message.message()));
    }
  }
  @MessageMapping("/getRecentHistory")
  public void getRecentHistory(@Payload MessageHistoryRequestDTO messageRequest, Principal principal) {
    if (!checkUserMembership(principal.getName(), messageRequest.chatroomId())) {
      template.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      List<ChatMessage> foundMessages = messageRequest.beforeMessageId() == null ?
        messageRepo.findTop10ByChatroomId(messageRequest.chatroomId()) :
        messageRepo.findTop10ByChatroomIdAndIdLessThan(messageRequest.chatroomId(), messageRequest.beforeMessageId());
//      kafkaService.sendMessage
    }
  }
}
