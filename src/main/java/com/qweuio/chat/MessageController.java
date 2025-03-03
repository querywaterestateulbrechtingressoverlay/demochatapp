package com.qweuio.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class MessageController {
  Logger logger = LoggerFactory.getLogger(MessageController.class);
  @MessageMapping("/in")
//  @SendTo("/out/chat")
  @SendToUser("/out/chat")
  public Message message(Message message, Principal p) {
    logger.info("message " + message.message().toUpperCase() + " from user " + p.getName());
    return new Message("message " + message.message().toUpperCase() + " from user " + p.getName());
  }
}
