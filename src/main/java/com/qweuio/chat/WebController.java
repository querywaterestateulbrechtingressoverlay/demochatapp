package com.qweuio.chat;

import com.qweuio.chat.persistence.Chatroom;
import com.qweuio.chat.persistence.ChatroomRepository;
import com.qweuio.chat.persistence.User;
import com.qweuio.chat.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class WebController {
  Logger logger = LoggerFactory.getLogger(WebController.class);
  @Autowired
  ChatroomRepository chatroomRepo;
  @Autowired
  UserRepository userRepo;

  @GetMapping("/ping")
  String pong() {
    return "pong";
  }
  @PostMapping("/init")
  void init() {
    for (int i = 0; i < 5; i++) {
      userRepo.insert(new User(i, "user-" + i));
    }
    for (int i = 0; i < 3; i++) {
      chatroomRepo.insert(new Chatroom(i, "chatroom-" + i, List.of(i + 1, i + 2, i + 3)));
    }
  }
}
