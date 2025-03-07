package com.qweuio.chat;

import com.qweuio.chat.dto.ProcessedMessageDTO;
import com.qweuio.chat.dto.UnprocessedMessageDTO;
import com.qweuio.chat.exception.ChatroomAccessException;
import com.qweuio.chat.persistence.Chatroom;
import com.qweuio.chat.persistence.ChatroomRepository;
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
  ChatroomRepository chatroomRepo;
  @Autowired
  SimpMessagingTemplate template;
  @MessageMapping("/send")
  public void message(@Payload UnprocessedMessageDTO message, Principal principal) {
    try {
      int senderId = Integer.parseInt(principal.getName());
      Chatroom destChatroom = chatroomRepo.findById(message.chatroomId()).orElseThrow(() -> new ChatroomAccessException("chatroom with id " + message.chatroomId() + " does not exist"));
      if (!destChatroom.userIds().contains(Integer.valueOf(principal.getName()))) {
        throw new ChatroomAccessException("user " + senderId + " tried to access chatroom " + message.chatroomId() + " that they are not a member of");
      }
      logger.info(destChatroom.toString());
      for (Integer userId : destChatroom.userIds()) {
//        if (userId != senderId) {
          template.convertAndSendToUser(userId.toString(), "/messages", new ProcessedMessageDTO(senderId, message.chatroomId(), message.message()));
//        }
      }
    } catch (ChatroomAccessException e) {
      template.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    }
  }
}
