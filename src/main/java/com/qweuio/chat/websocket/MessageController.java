package com.qweuio.chat.websocket;

import com.qweuio.chat.messaging.KafkaService;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import com.qweuio.chat.websocket.dto.UnprocessedMessageDTO;
import com.qweuio.chat.websocket.exception.ChatroomAccessException;
import com.qweuio.chat.persistence.entity.ChatRoom;
import com.qweuio.chat.persistence.repository.ChatRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class MessageController {
  Logger logger = LoggerFactory.getLogger(MessageController.class);
  @Autowired
  ChatRoomRepository chatroomRepo;
  @Autowired
  KafkaService kafkaService;
  @Autowired
  SimpMessagingTemplate template;
  @MessageMapping("/send")
  public void message(@Payload UnprocessedMessageDTO message, Principal principal) {
    try {
      int senderId = Integer.parseInt(principal.getName());
      ChatRoom destChatroom = chatroomRepo.findById(message.chatroomId()).orElseThrow(() -> new ChatroomAccessException("chatroom with id " + message.chatroomId() + " does not exist"));
      if (!destChatroom.userIds().contains(Integer.valueOf(principal.getName()))) {
        throw new ChatroomAccessException("user " + senderId + " tried to access chatroom " + message.chatroomId() + " that they are not a member of");
      }
      for (Integer userId : destChatroom.userIds()) {
        kafkaService.sendMessage(new ProcessedMessageDTO(senderId, userId, message.chatroomId(), message.message()));
      }
    } catch (ChatroomAccessException e) {
      template.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    }
  }
}
