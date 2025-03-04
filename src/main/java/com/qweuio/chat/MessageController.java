package com.qweuio.chat;

import com.qweuio.chat.persistence.Chatroom;
import com.qweuio.chat.persistence.ChatroomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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
  @MessageMapping("/send/{chatId}")
  public void message(@DestinationVariable Integer chatId, @Payload Message message, Principal principal) {
    try {
      Chatroom destChatroom = chatroomRepo.findById(chatId).orElseThrow(() -> new ChatroomAccessException("chatroom with id " + chatId + " does not exist"));
//      if (!destChatroom.userIds().contains(Integer.valueOf(principal.getName()))) {
//        throw new ChatroomAccessException("user " + principal + " tried to access chatroom " + chatId + " that they are not a member of");
//      }
      logger.info(destChatroom.toString());
      for (Integer userId : destChatroom.userIds()) {
//        if (userId != Integer.valueOf(principal.getName())) {
          template.convertAndSendToUser(userId.toString(), "/message", message);
//        }
      }
    } catch (ChatroomAccessException e) {
      template.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    }
  }
}
