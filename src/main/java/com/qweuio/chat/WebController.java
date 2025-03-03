package com.qweuio.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {
  Logger logger = LoggerFactory.getLogger(WebController.class);
  @Autowired
  SimpMessagingTemplate template;
  @PostMapping("/sendmsg")
  void sendMessage(@RequestBody SuperMessage message) {
    logger.info(message.toString());
    template.convertAndSendToUser(message.recipient(), "/out/chat", message.message());
  }
  @GetMapping("/ping")
  String pong() {
    return "pong";
  }
}
